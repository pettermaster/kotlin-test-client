package validation

import api.mock.MockBuggedAccessLevelApi
import api.mock.User
import com.beust.klaxon.Klaxon
import domain.ApiSpecification
import domain.GetFieldTest
import domain.PostFieldTest
import domain.UserLevelTestResult
import dynamic.DynamicApiModelTestExecutor
import klaxonutil.ApiFieldConverter
import org.junit.BeforeClass
import org.junit.Test
import java.io.File

class BuggedAccessScopeTest {

    companion object {
        lateinit var apiSpecification: ApiSpecification
        @BeforeClass
        @JvmStatic
        fun setup() {
            val apiModelFile = File("/Users/petteriversen/Documents/master/kotlin-client/src/buggedAccessScopeApiModel.json")
            val apiModelString = apiModelFile.readText()
            apiSpecification = Klaxon()
                    .converter(ApiFieldConverter())
                    .parse<ApiSpecification>(
                            apiModelString
                    )!!
        }
    }

    @Test
    fun login() {
        val normalUser = User("Petter", "petter@gmail.com", "12345678", false)
        val adminUser = User("Edvard", "edvard@gmail.com", "23456781", true)

        val userJwt = MockBuggedAccessLevelApi.login(normalUser)
        val adminJwt = MockBuggedAccessLevelApi.login(adminUser)

        print(userJwt)
        print("\n$adminJwt")
    }

    @Test
    fun `test readable fields` () {
        val testResult = DynamicApiModelTestExecutor(apiSpecification, MockBuggedAccessLevelApi()).executeDynamicApiModelTest()
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

    @Test
    fun `test writable fields` () {
        val testResult = DynamicApiModelTestExecutor(apiSpecification, MockBuggedAccessLevelApi()).executeDynamicApiModelTest()
        testResult.endpointTestResults.forEach {
            print("\n${it.relativePath}")
            it.endpointMethodTestResults.forEach {
                print("\n\t${it.httpMethod}")
                it.userLevelTestResults.forEach {
                    print("\n\t\t${it.userLevelName}")
                    when(it) {
                        is UserLevelTestResult.PostSuccess -> it.fieldTests.forEach {
                            prettyPrintPostFieldTest(it)
                        }
                        is UserLevelTestResult.ServerError -> print("Error: ${it.serverResponse.errorMessage}. ResponseCode: ${it.serverResponse.responseCode}")
                    }
                }
            }
        }
    }

    fun prettyPrintPostFieldTest(it: PostFieldTest) {
        print("\n\t\t\t")
        if(it.isWriteAble) {
            print("${it.name} is WRITABLE")
        } else {
            print("${it.name} is not writable")
        }
    }
}