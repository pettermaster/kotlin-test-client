package mock

import api.ApiRepository
import com.beust.klaxon.JsonArray
import com.beust.klaxon.Klaxon
import com.beust.klaxon.KlaxonException
import domain.JWT
import dynamictest.ApiResponse
import dynamictest.ResponseCode
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.impl.crypto.MacProvider
import java.util.*

class MockApiRepository: ApiRepository {

    companion object {
        val key = MacProvider.generateKey()
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
                    .signWith(SignatureAlgorithm.HS256, key)
                    .compact()

            calendar.add(Calendar.DAY_OF_YEAR, 59)

            val refreshToken = Jwts.builder()
                    .setExpiration(calendar.time)
                    .signWith(SignatureAlgorithm.HS256, key)
                    .compact()

            return JWT(accessToken, refreshToken)
        }
    }

    override fun createChat(chatJsonString: String, jwt: JWT): ApiResponse {
        return try {
            val newChat = Klaxon().parse<Chat>(chatJsonString)!!
            chats.add(newChat)
            ApiResponse.Success(Klaxon().toJsonString(newChat))

        } catch (exception: KlaxonException) {
            ApiResponse.Error(ResponseCode.BAD_REQUEST, exception.toString())
        }
    }

    override fun getChats(jwt: JWT): JsonArray<Any> {
        return JsonArray(Klaxon().toJsonString(chats))
    }
}