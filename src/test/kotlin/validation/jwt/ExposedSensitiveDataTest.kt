import domain.ApiSpecification
import org.junit.BeforeClass
import org.junit.Test
import static.JWTTestResult
import static.StaticJWTTestConfiguration
import static.StaticJWTTestExecutor

class ExposedSensitiveDataTest {

    companion object {
        lateinit var apiSpecification: ApiSpecification
        @BeforeClass
        @JvmStatic
        fun setup() {
            apiSpecification = parseApiModelFromPath("/Users/petteriversen/Documents/master/kotlin-client/src/sampleApiModel.json")
        }
    }

    @Test
    fun `Exposure of sensitive data test` () {
        val testConfiguration = StaticJWTTestConfiguration(sensitivePayloadFields = setOf(
                "email",
                "password",
                "name"
        ))
        val testExecutor = StaticJWTTestExecutor(testConfiguration)

        apiSpecification.userLevels.forEach {
            print("\n${it.name}")
            val accessTokenResult = testExecutor.testTokenPayload(it.jwt.accessToken, "access token")
            when (accessTokenResult) {
                is JWTTestResult.Failed -> logFailedJwtTest(accessTokenResult)
            }

            val refreshTokenResult = testExecutor.testTokenPayload(it.jwt.refreshToken, "refresh token")
            when (refreshTokenResult) {
                is JWTTestResult.Failed -> logFailedJwtTest(refreshTokenResult)
            }
        }
    }

    fun logFailedJwtTest(it: JWTTestResult.Failed) {
        print("\n\t${it.name}")
        print("\n\t\tVulnerability description: ${it.description}")
        print("\n\t\tError: ${it.errorMessage}")
    }

}