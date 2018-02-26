package dynamictest

sealed class ApiResponse {
    class Success(val jsonString: String): ApiResponse()
    class Error(val responseCode: ResponseCode, val errorMessage: String): ApiResponse()
}