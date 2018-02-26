package dynamictest

sealed class QueryParameterTest(
        val queryParameter: String
) {
    class PossibleDangerousQueryParameter(val matchingDictionaryEntries: List<String>, queryParameter: String): QueryParameterTest(queryParameter)
    class PassedQueryParameterTest(queryParameter: String) : QueryParameterTest(queryParameter)
}