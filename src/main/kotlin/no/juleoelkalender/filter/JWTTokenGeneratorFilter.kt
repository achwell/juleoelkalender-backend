package no.juleoelkalender.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import no.juleoelkalender.config.JwtService
import org.springframework.http.HttpHeaders
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

class JWTTokenGeneratorFilter(private val jwtService: JwtService) : OncePerRequestFilter() {
    @Throws(ServletException::class, IOException::class)
    public override fun doFilterInternal(
            request: HttpServletRequest,
            response: HttpServletResponse,
            filterChain: FilterChain
    ) {
        val authentication = SecurityContextHolder.getContext().authentication
        if (null != authentication) {
            val jwt = jwtService.generateToken(name = authentication.name, authorities = authentication.authorities)
            response.setHeader(HttpHeaders.AUTHORIZATION, jwt)
        }
        filterChain.doFilter(request, response)
    }

    public override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return request.servletPath != "/api"
    }
}
