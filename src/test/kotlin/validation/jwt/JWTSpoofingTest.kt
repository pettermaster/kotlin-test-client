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

class JWTSpoofingTest {

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
    fun `JWTSpoofing test` () {
        val testConfiguration = StaticJWTTestConfiguration()
        val testExecutor = StaticJWTTestExecutor(testConfiguration)

        apiSpecification.userLevels.forEach {
            print("\n${it.name}")
            val accessTokenResult = testExecutor.testTokenSecret(it.jwt.accessToken, "access token")
            when (accessTokenResult) {
                is JWTTestResult.Failed -> logFailedJwtTest(accessTokenResult)
            }

            val refreshTokenResult = testExecutor.testTokenSecret(it.jwt.refreshToken, "refresh token")
            when (refreshTokenResult) {
                is JWTTestResult.Failed -> logFailedJwtTest(refreshTokenResult)
            }
        }
    }

    fun logFailedJwtTest(it: JWTTestResult.Failed) {
        print("\n\t${it.testName}")
        print("\n\t\tVulnerability testDescription: ${it.testDescription}")
        print("\n\t\tError: ${it.errorMessage}")
    }

}