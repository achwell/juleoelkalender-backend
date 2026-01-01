package no.juleoelkalender.config

import jakarta.servlet.DispatcherType
import no.juleoelkalender.filter.JWTTokenGeneratorFilter
import no.juleoelkalender.filter.JWTTokenValidatorFilter
import no.juleoelkalender.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.*
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration

@Configuration
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
@EnableWebSecurity
class SecurityConfiguration {
    @Value($$"${app.jwt.secret_key}") private lateinit var tokenHeader: String
    @Value($$"#{'${app.cors.origins:}'.split(',')}") private lateinit var origins: MutableList<String>
    private lateinit var  jwtService: JwtService
    private lateinit var userRepository: UserRepository

    @Autowired
    fun configure(jwtService: JwtService, userRepository: UserRepository) {
        this.jwtService = jwtService
        this.userRepository = userRepository
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationManager(authenticationProvider: AuthenticationProvider): AuthenticationManager = ProviderManager(authenticationProvider)

    @Bean
    fun authenticationProvider(userDetailsService: UserDetailsService, passwordEncoder: PasswordEncoder): AuthenticationProvider {

        val authenticationProvider: DaoAuthenticationProvider = object : DaoAuthenticationProvider(userDetailsService) {
            @Throws(AuthenticationException::class)
            override fun additionalAuthenticationChecks(userDetails: UserDetails, authentication: UsernamePasswordAuthenticationToken) {
                if (authentication.credentials == null) {
                    this.logger.debug("Failed to authenticate since no credentials provided")
                    throw BadCredentialsException(this.messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"))
                }
                val presentedPassword: String = authentication.credentials.toString()
                if (presentedPassword != userDetails.password && !passwordEncoder.matches(presentedPassword, userDetails.password)) {
                    this.logger.debug("Failed to authenticate since password does not match stored value")
                    throw BadCredentialsException(this.messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"))
                }
            }
        }
        authenticationProvider.setPasswordEncoder(passwordEncoder)
        return authenticationProvider
    }

    @Bean
    fun filterChain(http: HttpSecurity, authenticationProvider: AuthenticationProvider): SecurityFilterChain {
        return http
                .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
                .cors { it.configurationSource { this.corsConfiguration } }
                .csrf { it.disable() }
                .addFilterBefore(JWTTokenValidatorFilter(tokenHeader, userRepository), BasicAuthenticationFilter::class.java)
                .addFilterAfter(JWTTokenGeneratorFilter(jwtService), BasicAuthenticationFilter::class.java)
                .authenticationProvider(authenticationProvider)
                .authorizeHttpRequests { authorize ->
                    authorize.dispatcherTypeMatchers(DispatcherType.FORWARD).permitAll()
                            .requestMatchers(*WHITE_LIST).permitAll()
                            .anyRequest().authenticated()
                }
                .headers { it.frameOptions { frameOptionsConfig -> it.frameOptions { frameOptionsConfig.sameOrigin() } } }
                .httpBasic(Customizer.withDefaults()).build()
    }

    private val corsConfiguration: CorsConfiguration
        get() {
            val config = CorsConfiguration()
            config.allowedOrigins = origins
            config.allowedMethods = mutableListOf("*")
            config.allowCredentials = true
            config.allowedHeaders = mutableListOf("*")
            config.exposedHeaders = mutableListOf(HttpHeaders.AUTHORIZATION)
            config.maxAge = 3600L
            return config
        }

    companion object {
        val WHITE_LIST: Array<String> = arrayOf("/actuator", "/actuator/**", "/error", "/locales/**", "/swagger-ui/**", "/swagger-ui.html", "/v3/**", "/api/v1/auth/addtoken", "/api/v1/auth/authenticate", "/api/v1/auth/facebookauthenticate", "/api/v1/auth/googleauthenticate", "/api/v1/auth/refresh", "/api/v1/auth/register", "/api/v1/auth/userExist", "/api/v1/auth/userExist/", "/api/v1/auth/userExist/**", "/api/v1/log", "/api/v1/log/*", "/api/v1/passwordchange", "/api/v1/passwordchange*", "/api/v1/passwordchange**/**")
    }
}
