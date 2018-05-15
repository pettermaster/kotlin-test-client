package validation

import api.mock.MockBuggedAccessLevelRepository
import com.beust.klaxon.Klaxon
import domain.ApiSpecification
import domain.GetFieldTest
import domain.UserLevelTestResult
import dynamic.DynamicApiModelTestExecutor
import klaxonutil.ApiFieldConverter
import org.junit.BeforeClass
import org.junit.Test
import java.io.File

class BuggedAccessLevelTest {

    companion object {
        lateinit var apiSpecification: ApiSpecification
        @BeforeClass
        @JvmStatic
        fun setup() {
            val apiModelFile = File("/Users/petteriversen/Documents/master/kotlin-client/src/buggedAccessLevelApiModel.json")
            val apiModelString = apiModelFile.readText()
            apiSpecification = Klaxon()
                    .converter(ApiFieldConverter())
                    .parse<ApiSpecification>(
                            apiModelString
                    )!!
        }
    }

    @Test
    fun `parses valid apimodel`() {
        val expectedApiRootUrl = "https://api.quizapp.com/v1"
        assert(apiSpecification.rootUrl == expectedApiRootUrl, { "Expected $expectedApiRootUrl, got ${apiSpecification.rootUrl}" })
    }

    @Test
    fun `test model fields` () {
        val testResult = DynamicApiModelTestExecutor(apiSpecification, MockBuggedAccessLevelRepository()).executeDynamicApiModelTest()
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
}