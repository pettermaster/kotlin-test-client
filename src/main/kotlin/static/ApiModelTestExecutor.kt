package static

import domain.ApiSpecification
import domain.Endpoint
import domain.EndpointMethod
import domain.ApiModelTest
import domain.EndpointMethodTest
import domain.EndpointTest
import domain.QueryParameterTest

class ApiModelTestExecutor {

    lateinit var queryParameterDictionary: Set<String>

    fun executeTest(apiSpecification: ApiSpecification, queryParameterDictionary: Set<String>): ApiModelTest {
        this.queryParameterDictionary = queryParameterDictionary
        val endpointTests = apiSpecification.endpoints.map({
            executeEndpointTest(it)
        })
        return ApiModelTest(endpointTests)
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

        val queryParameterTests = endpointMethod.queryParameters.map {
            executeQueryParameterTest(it)
        }

        return EndpointMethodTest(
                endpointMethod,
                queryParameterTests
        )
    }

    fun executeQueryParameterTest(queryParameter: String): QueryParameterTest {
        val matchingDictionaryEntries = mutableSetOf<String>()

        queryParameterDictionary.forEach {dictionaryEntry ->
            val isDictionaryEntryInQueryParameter = queryParameter.contains(dictionaryEntry, true)
            if(isDictionaryEntryInQueryParameter) {
                matchingDictionaryEntries.add(dictionaryEntry)
            }
        }

        return when (matchingDictionaryEntries.size) {
            0 -> QueryParameterTest.PassedQueryParameterTest(queryParameter)
            else -> QueryParameterTest.PossibleDangerousQueryParameter(
                    matchingDictionaryEntries,
                    queryParameter
            )
        }
    }
}