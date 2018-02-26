package dynamictest

import domain.Endpoint

data class EndpointTest(
        val endpoint: Endpoint,
        val endpointMethodTests: List<EndpointMethodTest>
)