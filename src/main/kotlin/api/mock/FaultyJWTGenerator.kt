package api.mock

import domain.JWT
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import java.util.*

class FaultyJWTGenerator {
    companion object {
        fun createInsecureJwt(isAdmin: Boolean): JWT {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, 30)

            val accessToken = Jwts.builder()
                    .addClaims(mapOf(
                            Pair("isAdmin", isAdmin),
                            Pair("email", "petter@gmail.com"),
                            Pair("phoneNumber", "12345678")
                    ))
                    .setExpiration(calendar.time)
                    .setIssuedAt(Date())
                    .signWith(SignatureAlgorithm.NONE, "")
                    .compact()

            calendar.add(Calendar.DAY_OF_YEAR, 3000)

            val refreshToken = Jwts.builder()
                    .setExpiration(calendar.time)
                    .setIssuedAt(Date())
                    .signWith(SignatureAlgorithm.NONE, "")
                    .compact()

            return JWT(accessToken, refreshToken)
        }
    }
}