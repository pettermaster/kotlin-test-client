import domain.ApiSpecification
import org.junit.BeforeClass
import org.junit.Test
import static.JWTTestResult
import static.StaticJWTTestConfiguration
import static.StaticJWTTestExecutor

class InsecureDurationTest {

    companion object {
        lateinit var apiSpecification: ApiSpecification
        @BeforeClass
        @JvmStatic
        fun setup() {
            apiSpecification = parseApiModelFromPath("src/unsignedTokenApiModel.json")
        }
    }

    @Test
    fun `Unsigned JWT` () {
        val testExecutor = StaticJWTTestExecutor(StaticJWTTestConfiguration())

        apiSpecification.userLevels.forEach {
            print("\n${it.name}")
            val accessTokenResult = testExecutor.testUnsignedToken(it.jwt.accessToken, "access token")
            when (accessTokenResult) {
                is JWTTestResult.Failed -> logFailedJwtTest(accessTokenResult)
            }

            val refreshTokenResult = testExecutor.testUnsignedToken(it.jwt.refreshToken, "refresh token")
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