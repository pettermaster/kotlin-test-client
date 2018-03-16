package api

import domain.JWT
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import mock.MockApiRepository
import java.util.*


class JWTTestExecutor {

    companion object {

        fun testAccessTokenExpiration(accessToken: String): JWTTestResult {
            val testName = "High or missing access token expiration"
            val testDescription = "Developers are notified if JWT doesn't expire within a reasonable time frame"
            val suggestedExpirationInHours = 12

            val decodedAccessToken = com.auth0.jwt.JWT.decode(accessToken)
            val tokenExpiration = decodedAccessToken.expiresAt
            val expiresCalendar = Calendar.getInstance()
            expiresCalendar.time = tokenExpiration
            val issuedAtMilliSeconds = decodedAccessToken.issuedAt.time
            val expiresAtMilliSeconds = decodedAccessToken.expiresAt.time
            val expirationInHours = (expiresAtMilliSeconds - issuedAtMilliSeconds).toInt() / (60 * 60 * 1000)
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

        fun testAccessTokenSecret(token: String): JWTTestResult {
            val testName = "Weak access token secret"
            val testDescription = "Developers are notified if the JWT is signed using a known secret"

            return testTokenSecret(token, testName, testDescription)
        }

        fun testRefreshTokenSecret(token: String): JWTTestResult {
            val testName = "Weak refresh token secret"
            val testDescription = "Developers are notified if the JWT is signed using a known secret"

            return testTokenSecret(token, testName, testDescription)
        }

        fun testTokenSecret(token: String, testName: String, testDescription: String): JWTTestResult {
            val knownSecrets = listOf(MockApiRepository.secret)

            val decodedAccessToken = com.auth0.jwt.JWT.decode(token)
            val issuedAt = decodedAccessToken.issuedAt
            val expiresAt = decodedAccessToken.expiresAt
            val isAdmin = decodedAccessToken.getClaim("isAdmin").asBoolean()

            knownSecrets.forEach {
                val spoofedToken = Jwts.builder()
                        .addClaims(mapOf(
                                Pair("isAdmin", isAdmin)
                        ))
                        .setExpiration(expiresAt)
                        .setIssuedAt(issuedAt)
                        .signWith(SignatureAlgorithm.HS512, it)
                        .compact()

                val spoofedSignature = spoofedToken.split('.')[2]
                val actualSignature = token.split('.')[2]
                if(spoofedSignature == actualSignature) {
                    return JWTTestResult.Failed(
                            testName,
                            testDescription,
                            "The JWT is signed using the known secret $it")
                }
            }
            return JWTTestResult.Passed(
                    testName,
                    testDescription
            )
        }

        val testSuite: List<JWTTestSuiteEntry> = listOf(
                JWTTestSuiteEntry { testAccessTokenExpiration(it.accessToken) },
                JWTTestSuiteEntry { testRefreshTokenExpiration(it.refreshToken) },
                JWTTestSuiteEntry { testAccessTokenSecret(it.accessToken) },
                JWTTestSuiteEntry { testRefreshTokenSecret(it.refreshToken) }
        )

        fun executeTest(jwt: JWT): StaticJWTAnalysisResult {
            val results = testSuite.map {
                it.testExecutor(jwt)
            }
            return StaticJWTAnalysisResult(results)
        }
    }


}

data class StaticJWTAnalysisResult(val jwtTests: List<JWTTestResult>)
sealed class JWTTestResult(val name: String, val description: String) {
    class Passed(name: String, description: String) : JWTTestResult(name, description)
    class Failed(name: String, description: String, val errorMessage: String) : JWTTestResult(name, description)
}

data class JWTTestSuiteEntry(val testExecutor: (jwt: JWT) -> JWTTestResult)
data class JWTExpirationTestResult(val accessTokenTest: JWTTestResult, val refreshTokenTest: JWTTestResult)