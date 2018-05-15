package api.mock

import api.API
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import domain.HttpMethod
import domain.JWT
import domain.ApiResponse
import domain.ResponseCode
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import java.util.*

class MockBuggedAccessLevelRepository: API {

    companion object {
        val secret = "secret"
        val quizzes: MutableList<Quiz> = mutableListOf(
                Quiz(
                        "Easy quiz",
                        listOf(
                                MultipleChoiceQuestion(
                                        "What is 2+2?",
                                        listOf(
                                                QuestionAlternative("2", false),
                                                QuestionAlternative("4", true),
                                                QuestionAlternative("6", false)
                                        )
                                ),
                                MultipleChoiceQuestion(
                                        "What is the capital of Norway?",
                                        listOf(
                                                QuestionAlternative("Oslo", true),
                                                QuestionAlternative("Bergen", false),
                                                QuestionAlternative("Trondheim", false)
                                        )
                                )
                        )
                )
        )

        val users = mutableListOf(
                User("Petter", "petter@gmail.com", "12345678"),
                User("Edvard", "edvard@gmail.com", "23456781")
        )

        fun login(isAdmin: Boolean): JWT {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.HOUR_OF_DAY, 1)

            val accessToken = Jwts.builder()
                    .addClaims(mapOf(
                            Pair("isAdmin", isAdmin),
                            Pair("email", "petter@gmail.com"),
                            Pair("phoneNumber", "12345678")
                    ))
                    .setExpiration(calendar.time)
                    .setIssuedAt(Date())
                    .signWith(SignatureAlgorithm.HS512, secret)
                    .compact()

            calendar.add(Calendar.DAY_OF_YEAR, 60)

            val refreshToken = Jwts.builder()
                    .setExpiration(calendar.time)
                    .setIssuedAt(Date())
                    .signWith(SignatureAlgorithm.HS512, secret)
                    .compact()

            return JWT(accessToken, refreshToken)
        }
    }

    override fun get(relativePath: String, jwt: JWT): ApiResponse {
        if(!validJwt(jwt)) {
            return ApiResponse.Error(HttpMethod.GET, ResponseCode.UNAUTHORIZED, "Invalid token")
        }
        return when (relativePath) {
            "quizzes" -> ApiResponse.Success(HttpMethod.GET, Klaxon().toJsonString(quizzes))
            "users" -> ApiResponse.Success(HttpMethod.GET, Klaxon().toJsonString(users))
            else -> ApiResponse.Error(HttpMethod.GET, ResponseCode.NOT_FOUND, "Endpoint not found in MockRepository")
        }
    }

    override fun post(relativePath: String, jwt: JWT, requestBody: JsonObject): ApiResponse {
        if(!validJwt(jwt)) {
            return ApiResponse.Error(HttpMethod.POST, ResponseCode.UNAUTHORIZED, "Invalid token")
        }
        return when (relativePath) {
            "chats" -> ApiResponse.Success(HttpMethod.POST, Klaxon().toJsonString(requestBody))
            else -> ApiResponse.Error(HttpMethod.POST, ResponseCode.NOT_FOUND, "Endpoint not found in MockRepository")
        }
    }

    private fun validJwt(jwt: JWT): Boolean {
        // TODO: Implement me
        return true
    }

    private fun validAdmin(jwt: JWT): Boolean {
        // TODO: Implement me
        return true
    }
}

data class Quiz(
        val name: String,
        val questions: List<MultipleChoiceQuestion>
)

data class MultipleChoiceQuestion(
        val questionText: String,
        val options: List<QuestionAlternative>
)

data class QuestionAlternative (
        val optionText: String,
        val isCorrect: Boolean
)