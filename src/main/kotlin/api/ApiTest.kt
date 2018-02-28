package api

import domain.ApiModel
import domain.Endpoint
import domain.EndpointMethod
import domain.Field
import dynamictest.*
import mock.MockApiRepository

class ApiTest {

    companion object {

        fun executeQueryParameterTest(queryParameter: String, dictionary: List<String>): QueryParameterTest {
            val matchingDictionaryEntries = mutableListOf<String>()
            dictionary.forEach {dictionaryEntry ->
                if(dictionaryEntry.contains(queryParameter, true))
                    matchingDictionaryEntries.add(dictionaryEntry)
            }

            if (matchingDictionaryEntries.size > 0) {
                return QueryParameterTest.PossibleDangerousQueryParameter(
                        matchingDictionaryEntries,
                        queryParameter
                )
            } else {
                return QueryParameterTest.PassedQueryParameterTest(
                        queryParameter
                )
            }
        }

        fun executeEndpointTest(endpoint: Endpoint): EndpointTest {
            val endpointMethodTests = endpoint.endpointMethods.map {
                executeEndpointMethodTest(it)
            }
            return EndpointTest(
                    endpoint,
                    endpointMethodTests
            )
        }

        fun executeEndpointMethodTest(endpointMethod: EndpointMethod): EndpointMethodTest {
            val queryParameterDictionary = listOf(
                    "id",
                    "password"
            )
            val queryParameterTests = endpointMethod.queryParameters.map {
                executeQueryParameterTest(it, queryParameterDictionary)
            }
            return EndpointMethodTest(
                    endpointMethod,
                    queryParameterTests
            )
        }


    }

    fun doTest(apiModel: ApiModel): ApiModelTest {
        val endpointTests = apiModel.endpoints.map({
            executeEndpointTest(it)
        })
        return ApiModelTest(endpointTests)
    }

}