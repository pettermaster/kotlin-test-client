package dynamictest

import domain.EndpointMethod

data class EndpointMethodTest(
        val endpointMethod: EndpointMethod,
        val queryParameterTests: List<QueryParameterTest>
)