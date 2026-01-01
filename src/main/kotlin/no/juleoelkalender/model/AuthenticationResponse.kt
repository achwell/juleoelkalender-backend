package no.juleoelkalender.model

data class AuthenticationResponse(val token: String?, val user: User?, val message: String?)
