package klaxonutil

import com.beust.klaxon.Converter
import com.beust.klaxon.JsonValue
import com.beust.klaxon.Klaxon
import domain.AuthenticationMethod

class AuthenticationMethodConverter: Converter<AuthenticationMethod> {

    override fun fromJson(jv: JsonValue): AuthenticationMethod {
        val authenticationType = jv.objString("type")

        return when(authenticationType) {
            "JWT" -> parseJWT(jv)
            else -> throw Exception("Error parsing ApiModel, invalid authentication type $authenticationType")
        }
    }

    private fun parseJWT(jv: JsonValue): AuthenticationMethod {
        return AuthenticationMethod.JWT(
                jv.objString("accessToken"),
                jv.objString("refreshToken")
        )
    }

    override fun toJson(value: AuthenticationMethod): String? {
        return Klaxon().toJsonString(value)
    }
}