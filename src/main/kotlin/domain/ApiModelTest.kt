package domain

data class ApiModelTest(val endpointTests: List<EndpointTest>)

data class EndpointTest(
        val endpoint: Endpoint,
        val endpointMethodTests: List<EndpointMethodTest>
)

data class EndpointMethodTest(
        val endpointMethod: EndpointMethod,
        val queryParameterTests: List<QueryParameterTest>
)

sealed class QueryParameterTest(
        val queryParameter: String
) {
    class PossibleDangerousQueryParameter(val matchingDictionaryEntries: Set<String>, queryParameter: String): QueryParameterTest(queryParameter)
    class PassedQueryParameterTest(queryParameter: String) : QueryParameterTest(queryParameter)
}

sealed class ApiResponse(val httpMethod: HttpMethod) {
    class Success(httpMethod: HttpMethod, val jsonString: String): ApiResponse(httpMethod)
    class Error(httpMethod: HttpMethod, val responseCode: ResponseCode, val errorMessage: String): ApiResponse(httpMethod)
}

enum class ResponseCode(value: Int) {
    UNKNOWN_ERROR(0),
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    NOT_FOUND(404),
    CONFLICT(409)
}