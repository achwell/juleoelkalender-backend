package no.juleoelkalender.filter

import io.jsonwebtoken.Claims
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.constraints.NotNull
import no.juleoelkalender.config.SecurityConfiguration.Companion.WHITE_LIST
import no.juleoelkalender.repository.UserRepository
import no.juleoelkalender.utils.extractAllClaims
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

class JWTTokenValidatorFilter(private val jwtKey: String, private val userRepository: UserRepository) : OncePerRequestFilter() {

    @Throws(ServletException::class, IOException::class)
    public override fun doFilterInternal(
            request: @NotNull HttpServletRequest,
            response: @NotNull HttpServletResponse, filterChain: FilterChain
    ) {
        try {
            validateToken(request)
            filterChain.doFilter(request, response)
        } catch (e: BadCredentialsException) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), e.message)
        }
    }

    private fun validateToken(request: HttpServletRequest) {
        val jwt = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (null != jwt && jwt.length > 7) {
            try {
                val token = jwt.replace("Bearer ", "")
                val claims: Claims = extractAllClaims(token, jwtKey)
                val username = claims["username"]?.toString().orEmpty()
                if (userRepository.findByEmailIgnoreCase(username) == null) {
                    throw BadCredentialsException("Invalid Token received!")
                }
                val authorities: String = claims["authorities"] as String
                val auth: Authentication = UsernamePasswordAuthenticationToken(
                        username, "",
                        AuthorityUtils.commaSeparatedStringToAuthorityList(authorities)
                )
                SecurityContextHolder.getContext().authentication = auth
            } catch (e: Exception) {
                throw BadCredentialsException("Invalid Token received!", e)
            }
        }
    }

    public override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val servletPath = request.servletPath
        return WHITE_LIST.any { servletPath.contains(it) }
    }
}
