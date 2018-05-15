import com.beust.klaxon.Klaxon
import domain.ApiSpecification
import klaxonutil.ApiFieldConverter
import java.io.File

fun parseApiModelFromPath(filePath: String): ApiSpecification {
    val apiModelFile = File(filePath)
    val apiModelString = apiModelFile.readText()
    return Klaxon()
            .converter(ApiFieldConverter())
            .parse<ApiSpecification>(
                    apiModelString
            )!!
}