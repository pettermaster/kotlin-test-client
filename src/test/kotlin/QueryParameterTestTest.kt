import api.QueryParameterTestExecutor
import mock.MockApiRepository
import org.junit.Test

class QueryParameterTestTest {

    @Test
    fun `notifies about dangerous query parameter`() {
        val dictionary = listOf("id", "password")
        val unharmfulQueryParemeterTest = QueryParameterTestExecutor.executeQueryParameterTest(dictionary.first(), dictionary)
        assert(unharmfulQueryParemeterTest is dynamictest.QueryParameterTest.PossibleDangerousQueryParameter)
    }

    @Test
    fun `does not notify about query parameter not in dictionary`() {
        val dictionary = listOf("id", "password")
        val unharmfulQueryParemeterTest = QueryParameterTestExecutor.executeQueryParameterTest("unharmful", dictionary)
        assert(unharmfulQueryParemeterTest is dynamictest.QueryParameterTest.PassedQueryParameterTest)
    }

    @Test
    fun `obtain JWT`() {
        val jwt = MockApiRepository.login(false)
        System.out.print(jwt.accessToken)
    }

}