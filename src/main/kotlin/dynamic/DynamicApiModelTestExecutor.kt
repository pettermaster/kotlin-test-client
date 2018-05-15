package dynamic

import api.API
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import domain.*

class DynamicApiModelTestExecutor(val apiSpecification: ApiSpecification, val api: API) {

    fun executeDynamicApiModelTest(): FieldTestResult {
        val testResults = apiSpecification.endpoints.map {
            executeEndpointTest(it, apiSpecification.userLevels)
        }
        return FieldTestResult(testResults)
    }

    private fun executeEndpointTest(endpoint: Endpoint, userLevels: Set<UserLevel>): EndpointTestResult {
        val testResults = endpoint.endpointMethods.map {
            executeEndpointMethodTest(endpoint.relativePath, it, endpoint.fields, userLevels)
        }
        return EndpointTestResult(endpoint.relativePath, testResults)
    }

    private fun executeEndpointMethodTest(relativePath: String, endpointMethod: EndpointMethod, fields: Set<Field>, userLevels: Set<UserLevel>): EndpointMethodTestResult {
        val testResults = userLevels.map {
            executeAccessLevelTest(relativePath, it, fields, endpointMethod.httpMethod)
        }
        return EndpointMethodTestResult(endpointMethod.httpMethod, testResults)
    }

    private fun executeAccessLevelTest(relativePath: String, userLevel: UserLevel, endpointFields: Set<Field>, httpMethod: HttpMethod): UserLevelTestResult {
        val requestBody = generateRequestBody(endpointFields)
        val response = when(httpMethod) {
            HttpMethod.GET -> api.get(relativePath, userLevel.jwt)
            HttpMethod.POST -> api.post(relativePath, userLevel.jwt, requestBody)
        }
        return when(response) {
            is ApiResponse.Success -> parseResponse(userLevel.name, response.httpMethod, response.jsonString, endpointFields, requestBody) //todo requestbody should not need to be passed for GET, but gets the job done for now..
            is ApiResponse.Error -> UserLevelTestResult.ServerError(userLevel.name, response)
        }
    }

    private fun parseResponse(userLevelName: String, httpMethod: HttpMethod, serverJsonResponse: String, endpointFields: Set<Field>, requestBody: JsonObject): UserLevelTestResult {
        val parser = Parser()
        val parsedServerResponse = parser.parse(StringBuilder(serverJsonResponse))

        val serverResponseObject = when(parsedServerResponse) {
            is JsonObject -> parsedServerResponse
            is JsonArray<*> -> parsedServerResponse[0] as JsonObject // If the server returns an array, compare the first element in the array (should ideally compare all and summarize...)
            else -> null
        }

        return when(httpMethod) {
            HttpMethod.GET -> parseGetResponse(userLevelName, serverResponseObject, endpointFields)
            HttpMethod.POST -> parsePostResponse(userLevelName, serverResponseObject, requestBody)
        }
    }

    private fun parseGetResponse(userLevelName: String, parsedServerResponse: JsonObject?, endpointFields: Set<Field>): UserLevelTestResult {
        if(parsedServerResponse == null) return UserLevelTestResult.ServerError(userLevelName, ApiResponse.Error(HttpMethod.GET, ResponseCode.UNKNOWN_ERROR, "Parsed server response was empty, but with a positive response code"))
        val getFieldTests = parsedServerResponse.keys.map { key ->
            val responseValue = parsedServerResponse.map[key]
            GetFieldTest(key, responseValue != null)
        }
        return UserLevelTestResult.GetSuccess(userLevelName, getFieldTests)
    }

    private fun parsePostResponse(userLevelName: String, parsedServerResponse: JsonObject?, requestBody: JsonObject): UserLevelTestResult {
        if(parsedServerResponse == null) return UserLevelTestResult.ServerError(userLevelName, ApiResponse.Error(HttpMethod.POST, ResponseCode.UNKNOWN_ERROR, "Parsed server response was empty, but with a positive response code"))
        val postFieldTests = requestBody.map { currentEndpointField ->
            val responseValue = parsedServerResponse.map[currentEndpointField.key]
            val isFieldWriteable = responseValue == currentEndpointField.value
            PostFieldTest(currentEndpointField.key, isFieldWriteable)
        }
        return UserLevelTestResult.PostSuccess(userLevelName, postFieldTests)
    }

    private fun generateRequestBody(endpointFields: Set<Field>): JsonObject {
        val requestData = mutableMapOf<String, Any>()
        endpointFields.forEach {
            when(it) {
                is Field.SimpleField -> requestData[it.name] = generateField(it.fieldType)
                is Field.ArrayField -> requestData[it.name] = generateArrayField(it.fieldType)
            }
        }
        return JsonObject(requestData)
    }

    private fun generateArrayField (fieldType: FieldType): List<Any> {
        val fields = mutableListOf<Any>()
        for(i in 1..5) {
            fields.add(generateField(fieldType))
        }
        return fields
    }

    // fixme implement real generator
    private fun generateField (fieldType: FieldType): Any {
        return when(fieldType) {
            FieldType.STRING -> "Very random string"
            FieldType.NUMBER -> 42
            FieldType.BOOLEAN -> true
        }
    }

}
