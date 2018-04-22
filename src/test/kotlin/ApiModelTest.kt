import api.FieldTestExecutor
import klaxonutil.ApiFieldConverter
import com.beust.klaxon.Klaxon
import domain.ApiModel
import domain.GetFieldTest
import domain.PostFieldTest
import domain.UserLevelTestResult
import org.junit.BeforeClass
import org.junit.Test
import java.io.File

class ApiModelTest {


    companion object {
        lateinit var apiModel: ApiModel
        @BeforeClass
        @JvmStatic
        fun setup() {
            val apiModelFile = File("/Users/petteriversen/Documents/master/kotlin-client/src/sampleApiModel.json")
            val apiModelString = apiModelFile.readText()
            apiModel = Klaxon()
                    .converter(ApiFieldConverter())
                    .parse<ApiModel>(
                            apiModelString
                    )!!
        }
    }

    @Test
    fun `parses valid apimodel`() {
        val expectedApiRootUrl = "https://api.mychatapp.com/v1"
        assert(apiModel.rootUrl == expectedApiRootUrl, { "Expected $expectedApiRootUrl, got ${apiModel.rootUrl}" })
    }

    @Test
    fun `test query parameters` () {
        val testResult = api.QueryParameterTestExecutor().doTest(apiModel)
        testResult.endpointTests.forEach {
            System.out.print("\n\t${it.endpoint.relativePath}")
            it.endpointMethodTests.forEach {
                System.out.print("\n\t\t${it.endpointMethod.httpMethod}")
                it.queryParameterTests.forEach {
                    System.out.print("\n\t\t\t")
                    when (it) {
                        is dynamictest.QueryParameterTest.PossibleDangerousQueryParameter -> {
                            System.out.print("Query parameter: ${it.queryParameter} DANGEROUS (testSuite match: ${it.matchingDictionaryEntries.first()})")
                        }
                        else -> {
                            System.out.print("Query parameter: ${it.queryParameter} not dangerous")
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `test model fields` () {
        val testResult = FieldTestExecutor.executeTest(apiModel)
        testResult.endpointTestResults.forEach {
            print("\n${it.relativePath}")
            it.endpointMethodTestResults.forEach {
                print("\n\t${it.httpMethod}")
                it.userLevelTestResults.forEach {
                    print("\n\t\t${it.userLevelName}")
                    when(it) {
                        is UserLevelTestResult.GetSuccess -> it.fieldTests.forEach {
                            prettyPrintGetFieldTest(it)
                        }
                        is UserLevelTestResult.PostSuccess -> it.fieldTests.forEach {
                            prettyPrintPostFieldTest(it)
                        }
                        is UserLevelTestResult.ServerError -> print("Error: ${it.serverResponse.errorMessage}. ResponseCode: ${it.serverResponse.responseCode}")
                    }
                }
            }
        }
    }

    fun prettyPrintGetFieldTest(it: GetFieldTest) {
        print("\n\t\t\t")
        if(it.isReadAble) {
            print("${it.name} is readable")
        } else {
            print("${it.name} is not readable")
        }
    }

    fun prettyPrintPostFieldTest(it: PostFieldTest) {
        print("\n\t\t\t")
        if(it.isWriteAble) {
            print("${it.name} is writeable")
        } else {
            print("${it.name} is not writeable")
        }
    }

}

