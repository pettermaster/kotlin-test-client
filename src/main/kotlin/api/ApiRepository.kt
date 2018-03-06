package api

import com.beust.klaxon.JsonObject
import domain.JWT
import dynamictest.ApiResponse

interface ApiRepository {
    fun get(relativePath: String, jwt: JWT): ApiResponse
    fun post(relativePath: String, jwt: JWT, requestBody: JsonObject): ApiResponse
}