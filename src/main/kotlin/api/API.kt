package api

import com.beust.klaxon.JsonObject
import domain.ApiResponse
import domain.JWT

interface API {
    fun get(relativePath: String, accessToken: String): ApiResponse
    fun post(relativePath: String, accessToken: String, requestBody: JsonObject): ApiResponse
}