package no.juleoelkalender.exception

import org.springframework.security.core.AuthenticationException

class InvalidTokenException : AuthenticationException {
    constructor(msg: String) : super(msg)
}
