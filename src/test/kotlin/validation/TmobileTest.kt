package validation

import com.beust.klaxon.Klaxon
import domain.ApiSpecification
import domain.QueryParameterTest
import klaxonutil.ApiFieldConverter
import org.junit.BeforeClass
import org.junit.Test
import parseApiModelFromPath
import static.ApiModelTestExecutor
import java.io.File

class TmobileTest {

    companion object {
        lateinit var apiSpecification: ApiSpecification
        @JvmStatic
        @BeforeClass
        fun setup() {
            apiSpecification = parseApiModelFromPath("/Users/petteriversen/Documents/master/kotlin-client/src/tmobileApiModel.json")
        }
    }

    @Test
    fun `TMobile test`() {
        val queryParameterDict = setOf(
                "token",
                "password"
        )

        val testResult = ApiModelTestExecutor().executeTest(apiSpecification, queryParameterDict)
        testResult.endpointTests.forEach {
            System.out.print("\n\t${it.endpoint.relativePath}")
            it.endpointMethodTests.forEach {
                System.out.print("\n\t\t${it.endpointMethod.httpMethod}")
                it.queryParameterTests.forEach {
                    System.out.print("\n\t\t\t")
                    when (it) {
                        is QueryParameterTest.PossibleDangerousQueryParameter -> {
                            System.out.print("Query parameter: ${it.queryParameter} DANGEROUS (testSuite match: ${it.matchingDictionaryEntries.first()})")
                        }
                        else -> {
                            System.out.print("Query parameter: ${it.queryParameter} not dangerous")
                        }
                    }
                }
                if (it.queryParameterTests.isEmpty()) {
                    System.out.print("\n\t\t\tNo query parameters")
                }
            }
        }
    }
}