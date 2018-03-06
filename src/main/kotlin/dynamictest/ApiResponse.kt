package dynamictest

import domain.HttpMethod

sealed class ApiResponse(val httpMethod: HttpMethod) {
    class Success(httpMethod: HttpMethod, val jsonString: String): ApiResponse(httpMethod)
    class Error(httpMethod: HttpMethod, val responseCode: ResponseCode, val errorMessage: String): ApiResponse(httpMethod)
}