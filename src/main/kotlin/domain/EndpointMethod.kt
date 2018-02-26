package domain

data class EndpointMethod(
        val httpMethod: HttpMethod,
        val queryParameters: List<String>
)