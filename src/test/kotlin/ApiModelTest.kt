import api.ApiTest
import klaxonutil.ApiFieldConverter
import com.beust.klaxon.Klaxon
import domain.ApiModel
import dynamictest.QueryParameterTest
import klaxonutil.AuthenticationMethodConverter
import org.junit.BeforeClass
import org.junit.Test
import java.io.File

class ApiModelTest {


    companion object {
        lateinit var apiModel: ApiModel
        @BeforeClass
        @JvmStatic
        fun setup() {
            val apiModelFile = File("/Users/petteriversen/Documents/master/kotlin-client/src/sampleApiModel.json")
            val apiModelString = apiModelFile.readText()
            apiModel = Klaxon()
                    .converter(ApiFieldConverter())
                    .converter(AuthenticationMethodConverter())
                    .parse<ApiModel>(
                            apiModelString
                    )!!
        }
    }

    @Test
    fun `parses valid apimodel`() {
        assert(apiModel.rootUrl == "https:api.mychatapp.com/v1")
    }

    @Test
    fun `it works` () {
        val testResult = ApiTest().doTest(apiModel)
        testResult.endpointTests.forEach {
            System.out.print("\n\t${it.endpoint.relativePath}")
            it.endpointMethodTests.forEach {
                System.out.print("\n\t\t${it.endpointMethod.httpMethod}")
                it.queryParameterTests.forEach {
                    System.out.print("\n\t\t\t")
                    when (it) {
                        is QueryParameterTest.PossibleDangerousQueryParameter -> {
                            System.out.print("Query parameter: ${it.queryParameter} DANGEROUS (dictionary match: ${it.matchingDictionaryEntries.first()})")
                        }
                        else -> {
                            System.out.print("Query parameter: ${it.queryParameter} not dangerous")
                        }
                    }
                }
            }
        }
    }

}

