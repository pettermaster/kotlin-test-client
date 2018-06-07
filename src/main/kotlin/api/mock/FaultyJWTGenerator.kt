package api.mock

import domain.JWT
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import java.util.*

class FaultyJWTGenerator {
    companion object {
        fun createInsecureJwt(isAdmin: Boolean): JWT {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.YEAR, 30)

            val accessToken = Jwts.builder()
                    .addClaims(mapOf(
                            Pair("isAdmin", isAdmin),
                            Pair("email", "petter@gmail.com"),
                            Pair("phoneNumber", "12345678")
                    ))
                    .setExpiration(calendar.time)
                    .setIssuedAt(Date())
                    .signWith(SignatureAlgorithm.HS512, "secret")
                    .compact()

            calendar.add(Calendar.YEAR, 3000)

            val refreshToken = Jwts.builder()
                    .setExpiration(calendar.time)
                    .setIssuedAt(Date())
                    .signWith(SignatureAlgorithm.HS512, "secret")
                    .compact()

            return JWT(accessToken, refreshToken)
        }
    }
}