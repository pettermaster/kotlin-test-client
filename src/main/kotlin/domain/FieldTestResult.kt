package domain

import dynamictest.ApiResponse

data class FieldTestResult(val endpointTestResults: List<EndpointTestResult>)

data class EndpointTestResult(
        val relativePath: String,
        val endpointMethodTestResults: List<EndpointMethodTestResult>
)

data class EndpointMethodTestResult(
        val httpMethod: HttpMethod,
        val userLevelTestResults: List<UserLevelTestResult>
)

sealed class UserLevelTestResult(val userLevelName: String) {
    class GetSuccess(userLevelName: String, val fieldTests: List<GetFieldTest>) : UserLevelTestResult(userLevelName)
    class PostSuccess(userLevelName: String, val fieldTests: List<PostFieldTest>) : UserLevelTestResult(userLevelName)
    class ServerError(userLevelName: String, val serverResponse: ApiResponse.Error) : UserLevelTestResult(userLevelName)
}

data class GetFieldTest(
        val name: String,
        val isReadAble: Boolean
)

data class PostFieldTest(
        val name: String,
        val isWriteAble: Boolean
)