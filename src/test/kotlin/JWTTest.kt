import api.JWTTestExecutor
import api.JWTTestResult
import com.beust.klaxon.Klaxon
import domain.ApiModel
import klaxonutil.ApiFieldConverter
import org.junit.BeforeClass
import org.junit.Test
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
    fun `runs jwt test` () {
        apiModel.userLevels.forEach {
            val testResult = JWTTestExecutor.executeTest(it.jwt)
            testResult.jwtTests.forEach {
                when (it) {
                    is JWTTestResult.Failed -> logFailedJwtTest(it)
                }
            }
        }
    }

    fun logFailedJwtTest(it: JWTTestResult.Failed) {
        print("\n${it.name}")
        print("\n\t${it.description}")
        print("\n\t${it.errorMessage}")
    }

}