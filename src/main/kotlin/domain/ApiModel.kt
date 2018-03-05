package domain

data class ApiModel(
        val rootUrl: String,
        val endpoints: List<Endpoint>,
        val userLevels: List<UserLevel>
)

data class Endpoint(
        val relativePath: String,
        val endpointMethods: List<EndpointMethod>,
        val fields: List<Field>
)

data class EndpointMethod(
        val httpMethod: HttpMethod,
        val queryParameters: List<String>
)

sealed class Field(val name: String) {
    class SimpleField(name: String, val fieldType: FieldType): Field(name)
    class ArrayField(name: String, val arrayType: FieldType): Field(name)
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
        val authenticationMethod: AuthenticationMethod
)

sealed class AuthenticationMethod {
    data class JWTMethod(val jwt: JWT) : AuthenticationMethod()
}

data class JWT(
        val accessToken: String,
        val refreshToken: String
)