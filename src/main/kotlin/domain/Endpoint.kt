package domain

data class Endpoint(
        val relativePath: String,
        val endpointMethods: List<EndpointMethod>,
        val fields: List<Field>
)