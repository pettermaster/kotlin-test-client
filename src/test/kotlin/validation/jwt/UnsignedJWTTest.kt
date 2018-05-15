import domain.ApiSpecification
import org.junit.BeforeClass
import org.junit.Test
import static.JWTTestResult
import static.StaticJWTTestConfiguration
import static.StaticJWTTestExecutor

class UnsignedJWTTest {

    companion object {
        lateinit var apiSpecification: ApiSpecification
        @BeforeClass
        @JvmStatic
        fun setup() {
            apiSpecification = parseApiModelFromPath("/Users/petteriversen/Documents/master/kotlin-client/src/sampleApiModel.json")
        }
    }

    @Test
    fun `Insecure expiration time` () {
        val testExecutor = StaticJWTTestExecutor(StaticJWTTestConfiguration())

        apiSpecification.userLevels.forEach {
            print("\n${it.name}")
            val accessTokenResult = testExecutor.testAccessTokenExpiration(it.jwt.accessToken)
            when (accessTokenResult) {
                is JWTTestResult.Failed -> logFailedJwtTest(accessTokenResult)
            }

            val refreshTokenResult = testExecutor.testRefreshTokenExpiration(it.jwt.refreshToken)
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