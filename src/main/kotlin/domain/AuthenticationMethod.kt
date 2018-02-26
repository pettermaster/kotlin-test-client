package domain

sealed class AuthenticationMethod {
    data class JWT(
            val accessToken: String,
            val refreshToken: String
    ) : AuthenticationMethod()
}