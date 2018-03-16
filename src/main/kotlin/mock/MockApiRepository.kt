package mock

import api.ApiRepository
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import domain.HttpMethod
import domain.JWT
import dynamictest.ApiResponse
import dynamictest.ResponseCode
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import java.util.*

class MockApiRepository: ApiRepository {

    companion object {
        val secret = "secret"
        val chats: MutableList<Chat> = mutableListOf(
                Chat("chat1", listOf("user1")),
                Chat("chat2", listOf("user1", "user2"))
        )

        fun login(isAdmin: Boolean): JWT {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, 1)

            val accessToken = Jwts.builder()
                    .addClaims(mapOf(
                            Pair("isAdmin", isAdmin)
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

    override fun get(relativePath: String, jwt: JWT): ApiResponse {
        if(!validJwt(jwt)) {
            return ApiResponse.Error(HttpMethod.GET, ResponseCode.UNAUTHORIZED, "Invalid token")
        }
        if(!validAdmin(jwt)) {
            return ApiResponse.Error(HttpMethod.GET, ResponseCode.FORBIDDEN, "Not an administrator")
        }
        return when (relativePath) {
            "chats" -> ApiResponse.Success(HttpMethod.GET, Klaxon().toJsonString(chats))
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