package api.mock

import api.API
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import com.fasterxml.jackson.databind.ObjectMapper
import domain.HttpMethod
import domain.JWT
import domain.ApiResponse
import domain.ResponseCode
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.SignatureException
import java.util.*

class MockBuggedAccessLevelApi : API {

    companion object {
        val secret = "secret"
        val quizzes: MutableList<Quiz> = mutableListOf(
                Quiz(
                        "Easy quiz",
                        listOf(
                                MultipleChoiceQuestion(
                                        "What is 2+2?",
                                        listOf(
                                                QuestionAlternative("2", false, 1),
                                                QuestionAlternative("4", true,2),
                                                QuestionAlternative("6", false, 3)
                                        )
                                ),
                                MultipleChoiceQuestion(
                                        "What is the capital of Norway?",
                                        listOf(
                                                QuestionAlternative("Oslo", true, 4),
                                                QuestionAlternative("Bergen", false, 5),
                                                QuestionAlternative("Trondheim", false, 6)
                                        )
                                )
                        ),
                        1
                )
        )

        fun login(user: User): JWT {
            val objectMapper = ObjectMapper()
            val userMap = objectMapper.convertValue(user, Map::class.java) as Map<String, *>
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.HOUR_OF_DAY, 365)

            val accessToken = Jwts.builder()
                    .addClaims(userMap)
                    .setExpiration(calendar.time)
                    .setIssuedAt(Date())
                    .signWith(SignatureAlgorithm.HS512, secret)
                    .compact()

            calendar.add(Calendar.DAY_OF_YEAR, 6000)

            val refreshToken = Jwts.builder()
                    .setExpiration(calendar.time)
                    .setIssuedAt(Date())
                    .signWith(SignatureAlgorithm.HS512, secret)
                    .compact()

            return JWT(accessToken, refreshToken)
        }
    }

    override fun get(relativePath: String, accessToken: String): ApiResponse {
        if(!validateAccessToken(accessToken)) {
            return ApiResponse.Error(HttpMethod.GET, ResponseCode.UNAUTHORIZED, "Invalid token")
        }
        return when (relativePath) {
            "quizzes" -> ApiResponse.Success(HttpMethod.GET, Klaxon().toJsonString(quizzes))
            else -> ApiResponse.Error(HttpMethod.GET, ResponseCode.NOT_FOUND, "Endpoint not found in MockRepository")
        }
    }

    override fun post(relativePath: String, accessToken: String, requestBody: JsonObject): ApiResponse {
        if(!validateAccessToken(accessToken)) {
            return ApiResponse.Error(HttpMethod.POST, ResponseCode.UNAUTHORIZED, "Invalid token")
        }
        return when (relativePath) {
            "assignments" -> {
                val jsonString = requestBody.toJsonString()
                val parsedAssignment = Klaxon().parse<Assignment>(jsonString)!!
                val sanitizedAssignment = parsedAssignment.copy()
                ApiResponse.Success(HttpMethod.POST, Klaxon().toJsonString(sanitizedAssignment))
            }
            else -> ApiResponse.Error(HttpMethod.POST, ResponseCode.NOT_FOUND, "Endpoint not found in MockRepository")
        }
    }

    private fun validateAccessToken(accessToken: String): Boolean {
        try {
            val parsedJwt = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(accessToken)
            return true
        } catch (signatureException: SignatureException) {
            return false
        }
    }

    private fun validateAdmin(accessToken: String): Boolean {
        // TODO: Implement me
        return true
    }


}

data class Quiz(
        val name: String,
        val questions: List<MultipleChoiceQuestion>,
        val id: Int
)

data class MultipleChoiceQuestion(
        val questionText: String,
        val options: List<QuestionAlternative>
)

data class QuestionAlternative (
        val optionText: String,
        val isCorrect: Boolean,
        val id: Int
)

data class Assignment (
        val text: String,
        val assignmentStatus: Int
)