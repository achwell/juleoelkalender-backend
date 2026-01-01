package no.juleoelkalender.controller

import jakarta.mail.MessagingException
import jakarta.servlet.http.HttpServletRequest
import no.juleoelkalender.model.AddTokenRequest
import no.juleoelkalender.model.AuthenticationRequest
import no.juleoelkalender.model.AuthenticationResponse
import no.juleoelkalender.model.RegisterRequest
import no.juleoelkalender.model.externalauth.FacebookAuthenticationRequest
import no.juleoelkalender.model.externalauth.GoogleAuthenticationRequest
import no.juleoelkalender.service.AuthenticationService
import no.juleoelkalender.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.net.URISyntaxException

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
        private val authenticationService: AuthenticationService,
        private val userService: UserService
) {
    @PostMapping("/register")
    @PreAuthorize("isAnonymous()")
    @Throws(URISyntaxException::class, MessagingException::class)
    fun register(@RequestBody request: RegisterRequest): ResponseEntity<AuthenticationResponse> {
        return ResponseEntity.created(URI("/login")).body(authenticationService.register(request))
    }

    @PostMapping("/authenticate")
    @PreAuthorize("isAnonymous()")
    fun authenticate(
            @RequestBody request: AuthenticationRequest
    ): ResponseEntity<AuthenticationResponse> {
        return ResponseEntity.ok(authenticationService.authenticate(request))
    }

    @PostMapping("/facebookauthenticate")
    @PreAuthorize("isAnonymous()")
    fun loginWithFacebook(
            @RequestBody request: FacebookAuthenticationRequest
    ): ResponseEntity<AuthenticationResponse> {
        return ResponseEntity.ok(authenticationService.facebookAuthenticate(request))
    }

    @PostMapping("/googleauthenticate")
    @PreAuthorize("isAnonymous()")
    fun loginWithGoogle(
            @RequestBody request: GoogleAuthenticationRequest
    ): ResponseEntity<AuthenticationResponse> {
        return ResponseEntity.ok(authenticationService.googleAuthenticate(request))
    }

    @PostMapping("/addtoken")
    @PreAuthorize("isAnonymous()")
    fun addtoken(@RequestBody request: AddTokenRequest): ResponseEntity<Void> {
        authenticationService.addtoken(request)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/refresh")
    fun refresh(request: HttpServletRequest): ResponseEntity<AuthenticationResponse> {
        return try {
            ResponseEntity.ok(authenticationService.refresh(request))
        } catch (e: AuthenticationException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(AuthenticationResponse("", null, e.message))
        }
    }

    @GetMapping("/userExist/{email}")
    @PreAuthorize("isAnonymous()")
    fun userExist(@PathVariable email: String): ResponseEntity<Boolean> {
        try {
            userService.loadUserByUsername(email)
            return ResponseEntity.ok(true)
        } catch (e: UsernameNotFoundException) {
            return ResponseEntity.ok(false)
        }
    }
}
