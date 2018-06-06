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

class MockChatApi : API {

    companion object {
        val secret = "secret"
        val chats: MutableList<Chat> = mutableListOf(
                Chat("chat1", listOf("user1")),
                Chat("chat2", listOf("user1", "user2"))
        )

        val users = mutableListOf(
                User("Petter", "petter@gmail.com", "12345678", false),
                User("Edvard", "edvard@gmail.com", "23456781", true)
        )

        fun login(isAdmin: Boolean): JWT {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, 1)

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

            calendar.add(Calendar.DAY_OF_YEAR, 90)

            val refreshToken = Jwts.builder()
                    .setExpiration(calendar.time)
                    .setIssuedAt(Date())
                    .signWith(SignatureAlgorithm.HS512, secret)
                    .compact()

            return JWT(accessToken, refreshToken)
        }
    }

    override fun get(relativePath: String, accessToken: String): ApiResponse {
        if(!validAccessToken(accessToken)) {
            return ApiResponse.Error(HttpMethod.GET, ResponseCode.UNAUTHORIZED, "Invalid token")
        }
        if(!validAdmin(accessToken)) {
            return ApiResponse.Error(HttpMethod.GET, ResponseCode.FORBIDDEN, "Not an administrator")
        }
        return when (relativePath) {
            "chats" -> ApiResponse.Success(HttpMethod.GET, Klaxon().toJsonString(chats))
            "users" -> ApiResponse.Success(HttpMethod.GET, Klaxon().toJsonString(users))
            else -> ApiResponse.Error(HttpMethod.GET, ResponseCode.NOT_FOUND, "Endpoint not found in MockRepository")
        }
    }

    override fun post(relativePath: String, accessToken: String, requestBody: JsonObject): ApiResponse {
        if(!validAccessToken(accessToken)) {
            return ApiResponse.Error(HttpMethod.POST, ResponseCode.UNAUTHORIZED, "Invalid token")
        }
        return when (relativePath) {
            "chats" -> ApiResponse.Success(HttpMethod.POST, Klaxon().toJsonString(requestBody))
            else -> ApiResponse.Error(HttpMethod.POST, ResponseCode.NOT_FOUND, "Endpoint not found in MockRepository")
        }
    }

    private fun validAccessToken(accessToken: String): Boolean {
        // TODO: Implement me
        return true
    }

    private fun validAdmin(accessToken: String): Boolean {
        // TODO: Implement me
        return true
    }
}

data class Chat(
        val name: String,
        val participantIds: List<String>
)

data class User(
        val name: String,
        val email: String,
        val phoneNumber: String,
        val isAdmin: Boolean
)