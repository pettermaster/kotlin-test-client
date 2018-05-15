import domain.ApiSpecification
import com.beust.klaxon.Klaxon
import klaxonutil.ApiFieldConverter
import api.mock.MockChatApiRepository
import org.junit.BeforeClass
import org.junit.Test
import static.JWTTestResult
import static.StaticJWTTestConfiguration
import static.StaticJWTTestExecutor
import java.io.File

class JWTTest {

    companion object {
        lateinit var apiSpecification: ApiSpecification
        @BeforeClass
        @JvmStatic
        fun setup() {
            val apiModelFile = File("/Users/petteriversen/Documents/master/kotlin-client/src/sampleApiModel.json")
            val apiModelString = apiModelFile.readText()
            apiSpecification = Klaxon()
                    .converter(ApiFieldConverter())
                    .parse<ApiSpecification>(
                            apiModelString
                    )!!
        }
    }

    @Test
    fun `Create JWT`() {
        val adminJWT = MockChatApiRepository.login(true)
        val userJWT = MockChatApiRepository.login(false)
        print(adminJWT)
        print("\n$userJWT")
    }

    @Test
    fun `runs jwt test` () {
        val staticJWTTestExecutor = StaticJWTTestExecutor(StaticJWTTestConfiguration())
        apiSpecification.userLevels.forEach {
            print("\n${it.name.toUpperCase()} token")
            val testResult = staticJWTTestExecutor.executeStaticJWTTest(it.jwt)
            testResult.jwtTests.forEach {
                when (it) {
                    is JWTTestResult.Failed -> logFailedJwtTest(it)
                }
            }
        }
    }

    fun logFailedJwtTest(it: JWTTestResult.Failed) {
        print("\n\t${it.testName}")
        print("\n\t\t${it.testDescription}")
        print("\n\t\t${it.errorMessage}")
    }

}