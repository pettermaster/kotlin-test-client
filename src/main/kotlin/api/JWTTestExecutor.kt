package api

import domain.JWT
import mock.MockApiRepository


class JWTTestExecutor {

    companion object {

        fun testTokenExpiration(jwt: JWT): JWTTestResult {
            val name = "High or missing token expiration"
            val description = "Developers are notified if JWT doesn't expire within a reasonable time frame"
            return JWTTestResult.Failed(
                    name,
                    description,
                    "Access tokens expiration date is over 1 day")
        }

        fun testTokenSecret(jwt: JWT): JWTTestResult {
            val secret = MockApiRepository.secret
            val name = "Weak JWT secret"
            val description = "Developers are notified if the JWT is signed using a known secret"
            return JWTTestResult.Failed(
                    name,
                    description,
                    "The jwt is signed using the known secret '$secret'")
        }

        val dictionary: List<JWTDictionaryEntry> = listOf(
                JWTDictionaryEntry { testTokenExpiration(it) },
                JWTDictionaryEntry { testTokenSecret(it) }
        )

        fun executeTest(jwt: JWT): StaticJWTAnalysisResult {
            val results = dictionary.map {
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

data class JWTTest(val dictionary: List<JWTDictionaryEntry>, val jwt: JWT)
data class JWTDictionaryEntry(val testExecutor: (jwt: JWT) -> JWTTestResult)

data class JwtBody(val exp: String)