package no.juleoelkalender.service

import jakarta.mail.MessagingException
import jakarta.servlet.http.HttpServletRequest
import no.juleoelkalender.model.AddTokenRequest
import no.juleoelkalender.model.AuthenticationRequest
import no.juleoelkalender.model.AuthenticationResponse
import no.juleoelkalender.model.RegisterRequest
import no.juleoelkalender.model.externalauth.FacebookAuthenticationRequest
import no.juleoelkalender.model.externalauth.GoogleAuthenticationRequest

interface AuthenticationService {
    @Throws(MessagingException::class)
    fun register(request: RegisterRequest): AuthenticationResponse

    fun authenticate(request: AuthenticationRequest): AuthenticationResponse

    fun facebookAuthenticate(request: FacebookAuthenticationRequest): AuthenticationResponse

    fun googleAuthenticate(request: GoogleAuthenticationRequest): AuthenticationResponse

    fun refresh(request: HttpServletRequest): AuthenticationResponse

    fun addtoken(request: AddTokenRequest)
}
