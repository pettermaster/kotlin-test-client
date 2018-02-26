import api.ApiTest
import dynamictest.QueryParameterTest
import mock.MockApiRepository
import org.junit.Test

class ApiTestTest {

    @Test
    fun `notifies about dangerous query parameter`() {
        val dictionary = listOf("id", "password")
        val unharmfulQueryParemeterTest = ApiTest.executeQueryParameterTest(dictionary.first(), dictionary)
        assert(unharmfulQueryParemeterTest is QueryParameterTest.PossibleDangerousQueryParameter)
    }

    @Test
    fun `does not notify about query parameter not in dictionary`() {
        val dictionary = listOf("id", "password")
        val unharmfulQueryParemeterTest = ApiTest.executeQueryParameterTest("unharmful", dictionary)
        assert(unharmfulQueryParemeterTest is QueryParameterTest.PassedQueryParameterTest)
    }

    @Test
    fun `obtain JWT`() {
        val jwt = MockApiRepository.login(false)
        System.out.print(jwt.accessToken)
    }

}