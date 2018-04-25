import domain.ApiModel
import com.beust.klaxon.Klaxon
import klaxonutil.ApiFieldConverter
import api.mock.MockApiRepository
import org.junit.BeforeClass
import org.junit.Test
import static.JWTTestResult
import static.executeStaticJWTTest
import java.io.File

class JWTTest {

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
    fun `Create JWT`() {
        val adminJWT = MockApiRepository.login(true)
        val userJWT = MockApiRepository.login(false)
        print(adminJWT)
        print("\n$userJWT")
    }

    @Test
    fun `runs jwt test` () {
        apiModel.userLevels.forEach {
            print("\n${it.name.toUpperCase()} token")
            val testResult = executeStaticJWTTest(it.jwt)
            testResult.jwtTests.forEach {
                when (it) {
                    is JWTTestResult.Failed -> logFailedJwtTest(it)
                }
            }
        }
    }

    fun logFailedJwtTest(it: JWTTestResult.Failed) {
        print("\n\t${it.name}")
        print("\n\t\t${it.description}")
        print("\n\t\t${it.errorMessage}")
    }

}