package no.juleoelkalender.utils

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.security.oauth2.jwt.JwtTimestampValidator
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.Instant
import java.util.function.Function
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

fun getSignInKey(jwtKey: String): SecretKey {
    val bytes = jwtKey.toByteArray(StandardCharsets.UTF_8)
    // Match JJWT's key interpretation and automatic HMAC algorithm selection.
    val algorithm = when {
        bytes.size >= 64 -> "HmacSHA512"
        bytes.size >= 48 -> "HmacSHA384"
        bytes.size >= 32 -> "HmacSHA256"
        else -> throw IllegalArgumentException("JWT secret must contain at least 32 UTF-8 bytes")
    }
    return SecretKeySpec(bytes, algorithm)
}

private fun macAlgorithm(key: SecretKey): MacAlgorithm = when (key.algorithm) {
    "HmacSHA512" -> MacAlgorithm.HS512
    "HmacSHA384" -> MacAlgorithm.HS384
    else -> MacAlgorithm.HS256
}

fun extractAllClaims(token: String, jwtKey: String): Jwt {
    val key = getSignInKey(jwtKey)
    val decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(macAlgorithm(key)).build()
    // JJWT used no clock skew and did not require an issuer or a typ header.
    decoder.setJwtValidator(JwtTimestampValidator(Duration.ZERO))
    return decoder.decode(token)
}

fun <T> extractClaim(token: String, jwtKey: String, claimsResolver: Function<Jwt, T>): T {
    return claimsResolver.apply(extractAllClaims(token, jwtKey))
}

fun isTokenExpired(token: String, jwtKey: String): Boolean {
    return extractAllClaims(token, jwtKey).expiresAt?.isBefore(Instant.now()) ?: false
}

fun buildToken(extraClaims: Map<String, *>, username: String, authorities: Collection<GrantedAuthority>, jwtKey: String, jwtExpiresTimeoutMs: Long): String {
    val key = getSignInKey(jwtKey)
    val algorithm = macAlgorithm(key)
    val createdDate = Instant.now()
    val claims = JwtClaimsSet.builder()
        .claims { claims -> extraClaims.forEach { (name, value) -> if (value != null) claims[name] = value } }
        .issuer("Juleølkalender")
        .subject(username)
        .claim("username", username)
        .claim("authorities", authorities.map { it.authority }.toSet().joinToString(","))
        .issuedAt(createdDate)
        .expiresAt(createdDate.plusMillis(jwtExpiresTimeoutMs))
        .build()
    val encoder = NimbusJwtEncoder.withSecretKey(key).algorithm(algorithm).build()
    return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(algorithm).build(), claims)).tokenValue
}
