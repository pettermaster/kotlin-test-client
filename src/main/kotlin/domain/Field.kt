package domain

sealed class Field(val name: String) {
    class SimpleField(name: String, val fieldType: FieldType): Field(name)
    class ArrayField(name: String, val arrayType: FieldType): Field(name)
}