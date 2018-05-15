package klaxonutil

import com.beust.klaxon.Converter
import com.beust.klaxon.JsonValue
import com.beust.klaxon.Klaxon
import domain.Field
import domain.FieldType

class ApiFieldConverter : Converter<Field> {

    override fun toJson(value: Field): String? {
        return Klaxon().toJsonString(value)
    }

    override fun fromJson(jv: JsonValue): Field {
        val fieldType = jv.objString("type")
        val fieldName = jv.objString("testName")
        val isArrayField = fieldType.startsWith('[')

        return if(isArrayField) {
            val arrayType = removeFirstAndLastCharacter(fieldType)
            when(arrayType) {
                "string" -> Field.ArrayField(fieldName, FieldType.STRING)
                "number" -> Field.ArrayField(fieldName, FieldType.NUMBER)
                FieldType.BOOLEAN.toString() -> Field.ArrayField(fieldName, FieldType.BOOLEAN)
                else -> throw Exception("Error parsing ApiSpecification, invalid array type $fieldType")
            }
        } else {
            when(fieldType) {
                "string" -> Field.SimpleField(fieldName, FieldType.STRING)
                "number" -> Field.SimpleField(fieldName, FieldType.NUMBER)
                FieldType.BOOLEAN.toString() -> Field.SimpleField(fieldName, FieldType.BOOLEAN)
                else -> throw Exception("Error parsing ApiSpecification, invalid field type $fieldType")
            }
        }
    }

    private fun removeFirstAndLastCharacter(fieldType: String): String {
        return fieldType.substring(1, fieldType.length-1)
    }
}