package static

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import domain.JWT
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import java.util.*

data class StaticJWTTestConfiguration (
        val accessTokenExpirationInHours: Int = 1,
        val refreshTokenExpirationInDays: Int = 60,
        val cryptographicKeyDictionary: Set<String> = setOf(
                "notsecret",
                "secret",
                "TOP_SECRET"
        ),
        val sensitivePayloadFields: Set<String> = setOf(
                "password",
                "email",
                "phone"
        )
)

class StaticJWTTestExecutor(val testConfiguration: StaticJWTTestConfiguration) {

    val testSuite: List<JWTTestSuiteEntry> = listOf(
            JWTTestSuiteEntry { testAccessTokenExpiration(it.accessToken) },
            JWTTestSuiteEntry { testRefreshTokenExpiration(it.refreshToken) },
            JWTTestSuiteEntry { testTokenSecret(it.accessToken, "access token") },
            JWTTestSuiteEntry { testTokenSecret(it.refreshToken, "refresh token") },
            JWTTestSuiteEntry { testTokenPayload(it.accessToken, "access token" )}
    )

    fun executeStaticJWTTest(jwt: JWT): StaticJWTAnalysisResult {
        val results = testSuite.map {
            it.testExecutor(jwt)
        }
        return StaticJWTAnalysisResult(results)
    }

    fun testTokenPayload(token: String, tokenTypeDescription: String): JWTTestResult {
        val testName = "Dubious payload field"
        val testDescription = "JWTs are not encrypted, and should not contain sensitive information"

        val decodedToken = com.auth0.jwt.JWT.decode(token)
        val claimsMap = createClaimMapFromBase64EncodedJson(decodedToken.payload)

        val claimsList = claimsMap.map {
            it.key
        }

        val claimsFilteredByMatchingDubiousField = claimsList.filter {claim ->
            var isClaimMatching = false
            testConfiguration.sensitivePayloadFields.forEach {
                if(claim.contains(it)) {
                    isClaimMatching = true
                }
            }
            isClaimMatching
        }

        if(claimsFilteredByMatchingDubiousField.isEmpty()) {
            return JWTTestResult.Passed(
                    testName,
                    testDescription
            )
        }

        val dubiousClaims = claimsFilteredByMatchingDubiousField.reduce{acc, dubiousClaim ->
            when(acc.isEmpty()) {
                true -> dubiousClaim
                else -> StringBuilder(acc).append(", ").append(dubiousClaim).toString()
            }
        }

        return JWTTestResult.Failed(
                testName,
                testDescription,
                "$tokenTypeDescription contains the dubious field(s) '$dubiousClaims', ensure that this is intentional"
        )
    }

    fun testAccessTokenExpiration(accessToken: String): JWTTestResult {
        val testName = "High or missing access token expiration"
        val testDescription = "Developers are notified if JWT doesn't expire within a reasonable time frame"
        val suggestedExpirationInHours = testConfiguration.accessTokenExpirationInHours

        val decodedAccessToken = com.auth0.jwt.JWT.decode(accessToken)
        val issuedAtMilliSeconds = decodedAccessToken.issuedAt.time
        val expiresAtMilliSeconds = decodedAccessToken.expiresAt.time
        val expirationInHours = ((expiresAtMilliSeconds - issuedAtMilliSeconds) / (60 * 60 * 1000)).toInt()
        if(expirationInHours > suggestedExpirationInHours) {
            return JWTTestResult.Failed(
                    testName,
                    testDescription,
                    "Access token's expiration is $expirationInHours hours. Anything over $suggestedExpirationInHours hours is considered bad practice.")
        } else {
            return JWTTestResult.Passed(
                    testName,
                    testDescription
            )
        }
    }

    fun testRefreshTokenExpiration(refreshToken: String): JWTTestResult {
        val testName = "High or missing refresh token expiration"
        val testDescription = "Developers are notified if JWT doesn't expire within a reasonable time frame"
        val suggestedExpirationInDays = testConfiguration.refreshTokenExpirationInDays

        val decodedAccessToken = com.auth0.jwt.JWT.decode(refreshToken)
        val tokenExpiration = decodedAccessToken.expiresAt
        val expiresCalendar = Calendar.getInstance()
        expiresCalendar.time = tokenExpiration
        val issuedAtMilliSeconds = decodedAccessToken.issuedAt.time
        val expiresAtMilliSeconds = decodedAccessToken.expiresAt.time
        val expirationInDays = ((expiresAtMilliSeconds - issuedAtMilliSeconds) / (24 * 60 * 60 * 1000)).toInt()
        if(expirationInDays > suggestedExpirationInDays) {
            return JWTTestResult.Failed(
                    testName,
                    testDescription,
                    "Access tokens expiration date is $expirationInDays days. Anything over $suggestedExpirationInDays is considered bad practice.")
        } else {
            return JWTTestResult.Passed(
                    testName,
                    testDescription
            )
        }
    }

    fun testTokenSecret(token: String, tokenTypeDescription: String): JWTTestResult {
        val testName = "Weak $tokenTypeDescription secret"
        val testDescription = "Developers are notified if the JWT is signed using a known secret"


        val decodedToken = com.auth0.jwt.JWT.decode(token)
        val payloadClaimsMap = createClaimMapFromBase64EncodedJson(decodedToken.payload)
        val headerClaimsMap = createClaimMapFromBase64EncodedJson(decodedToken.header)

        val algorithmName = headerClaimsMap.get("alg").toString()

        testConfiguration.cryptographicKeyDictionary.forEach {
            val spoofedToken = Jwts.builder()
                    .addClaims(payloadClaimsMap)
                    .signWith(SignatureAlgorithm.forName(algorithmName), it)
                    .compact()

            val spoofedSignature = spoofedToken.split('.')[2]
            val actualSignature = token.split('.')[2]
            if(spoofedSignature == actualSignature) {
                return JWTTestResult.Failed(
                        testName,
                        testDescription,
                        "The JWT is signed using the known secret '$it'")
            }
        }
        return JWTTestResult.Passed(
                testName,
                testDescription
        )
    }

    fun createClaimMapFromBase64EncodedJson(base64EncodedJson: String?): Map<String, Object> {
        val claimsMap = mutableMapOf<String, Object>()
        base64EncodedJson?.apply {
            val decodedPayload = Base64.getDecoder().decode(base64EncodedJson)
            val payloadJsonString = String(decodedPayload)
            val json: JsonObject = Parser().parse(StringBuilder(payloadJsonString)) as JsonObject
            claimsMap.putAll(json.map as Map<String, Object>)
        }
        return claimsMap
    }
}

data class StaticJWTAnalysisResult(val jwtTests: List<JWTTestResult>)
sealed class JWTTestResult(val name: String, val description: String) {
    class Passed(name: String, description: String) : JWTTestResult(name, description)
    class Failed(name: String, description: String, val errorMessage: String) : JWTTestResult(name, description)
}

data class JWTTestSuiteEntry(val testExecutor: (jwt: JWT) -> JWTTestResult)
data class JWTExpirationTestResult(val accessTokenTest: JWTTestResult, val refreshTokenTest: JWTTestResult)
