package static

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import domain.JWT
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import java.util.*

val testSuite: List<JWTTestSuiteEntry> = listOf(
        JWTTestSuiteEntry { testAccessTokenExpiration(it.accessToken) },
        JWTTestSuiteEntry { testRefreshTokenExpiration(it.refreshToken) },
        JWTTestSuiteEntry { testTokenSecret(it.accessToken, "access token") },
        JWTTestSuiteEntry { testTokenSecret(it.refreshToken, "refresh token") },
        JWTTestSuiteEntry { testTokenPayload(it.accessToken, "access token" )}
)

fun testTokenPayload(token: String, tokenTypeDescription: String): JWTTestResult {
    val testName = "Dubious payload field"
    val testDescription = "JWTs are not encrypted, and should not contain sensitive information"

    val dubiousFields = listOf(
            "password",
            "email",
            "phone"
    )
    val decodedToken = com.auth0.jwt.JWT.decode(token)
    val claimsMap = createClaimMapFromPayLoad(decodedToken.payload)

    val claimsList = claimsMap.map {
        it.key
    }

    val claimsFilteredByMatchingDubiousField = claimsList.filter {claim ->
        var isClaimMatching = false
        dubiousFields.forEach {
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

fun executeStaticJWTTest(jwt: JWT): StaticJWTAnalysisResult {
    val results = testSuite.map {
        it.testExecutor(jwt)
    }
    return StaticJWTAnalysisResult(results)
}

fun testAccessTokenExpiration(accessToken: String): JWTTestResult {
    val testName = "High or missing access token expiration"
    val testDescription = "Developers are notified if JWT doesn't expire within a reasonable time frame"
    val suggestedExpirationInHours = 12

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
    val suggestedExpirationInDays = 60

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
    val knownSecrets = listOf(
            "notsecret",
            "secret",
            "TOP_SECRET"
    )

    val decodedToken = com.auth0.jwt.JWT.decode(token)
    val claimsMap = createClaimMapFromPayLoad(decodedToken.payload)

    knownSecrets.forEach {
        val spoofedToken = Jwts.builder()
                .addClaims(claimsMap)
                .signWith(SignatureAlgorithm.HS512, it)
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

fun createClaimMapFromPayLoad(payload: String?): Map<String, Object> {
    val claimsMap = mutableMapOf<String, Object>()
    payload?.apply {
        val decodedPayload = Base64.getDecoder().decode(payload)
        val payloadJsonString = String(decodedPayload)
        val json: JsonObject = Parser().parse(StringBuilder(payloadJsonString)) as JsonObject
        claimsMap.putAll(json.map as Map<String, Object>)
    }
    return claimsMap
}

data class StaticJWTAnalysisResult(val jwtTests: List<JWTTestResult>)
sealed class JWTTestResult(val name: String, val description: String) {
    class Passed(name: String, description: String) : JWTTestResult(name, description)
    class Failed(name: String, description: String, val errorMessage: String) : JWTTestResult(name, description)
}

data class JWTTestSuiteEntry(val testExecutor: (jwt: JWT) -> JWTTestResult)
data class JWTExpirationTestResult(val accessTokenTest: JWTTestResult, val refreshTokenTest: JWTTestResult)
