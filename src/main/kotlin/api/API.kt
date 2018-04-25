package api

import com.beust.klaxon.JsonObject
import domain.ApiResponse
import domain.JWT

interface API {
    fun get(relativePath: String, jwt: JWT): ApiResponse
    fun post(relativePath: String, jwt: JWT, requestBody: JsonObject): ApiResponse
}