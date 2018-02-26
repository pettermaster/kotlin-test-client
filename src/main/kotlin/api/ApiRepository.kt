package api

import com.beust.klaxon.JsonArray
import domain.JWT
import dynamictest.ApiResponse

interface ApiRepository {
    fun getChats(jwt: JWT): JsonArray<Any>
    fun createChat(chatJsonString: String, jwt: JWT): ApiResponse
}