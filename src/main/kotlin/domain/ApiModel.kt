package domain

data class ApiModel(
        val rootUrl: String,
        val endpoints: List<Endpoint>,
        val userLevels: List<UserLevel>
)