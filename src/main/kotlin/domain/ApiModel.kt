package domain

data class ApiModel(
        val rootUrl: String,
        val endpoints: Set<Endpoint>,
        val userLevels: Set<UserLevel>
)

data class Endpoint(
        val relativePath: String,
        val endpointMethods: Set<EndpointMethod>,
        val fields: Set<Field>
)

data class EndpointMethod(
        val httpMethod: HttpMethod,
        val queryParameters: Set<String>
)

sealed class Field(val name: String, val fieldType: FieldType) {
    class SimpleField(name: String, fieldType: FieldType): Field(name, fieldType)
    class ArrayField(name: String, arrayType: FieldType ): Field(name, arrayType)
}

enum class FieldType(val value: String) {
    STRING("string"),
    NUMBER("number")
}

enum class HttpMethod{
    GET,
    POST,
}

data class UserLevel(
        val name: String,
        val jwt: JWT
)

data class JWT(
        val accessToken: String,
        val refreshToken: String
)