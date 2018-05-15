import org.junit.Test
import static.JWTTestResult
import static.StaticJWTTestConfiguration
import static.StaticJWTTestExecutor

class UnsignedJWTTest {

    @Test
    fun `Unsigned token` () {
        val unsignedJWT = "eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJpYXQiOjE1MjYzNjk1OTIsImV4cCI6MzAwMjYzNjk1OTIsImVtYWlsIjoicGV0dGVyaXZAc3R1ZC5udG51Lm5vIiwiaXNBZG1pbiI6dHJ1ZSwidG9rZW5UeXBlIjoiUkVGUkVTSCJ9."
        val testExecutor = StaticJWTTestExecutor(StaticJWTTestConfiguration())

        val accessTokenResult = testExecutor.testUnsignedToken(unsignedJWT, "")
        when (accessTokenResult) {
            is JWTTestResult.Failed -> logFailedJwtTest(accessTokenResult)
            else -> print("\n\t No errors")
        }
    }

    fun logFailedJwtTest(it: JWTTestResult.Failed) {
        print("\n\t${it.testName}")
        print("\n\t\tVulnerability testDescription: ${it.testDescription}")
        print("\n\t\tError: ${it.errorMessage}")
    }

}