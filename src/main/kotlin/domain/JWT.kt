package domain

data class JWT(
        val accessToken: String,
        val refreshToken: String
)