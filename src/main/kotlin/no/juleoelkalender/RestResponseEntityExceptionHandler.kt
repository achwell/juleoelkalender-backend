package no.juleoelkalender

import jakarta.persistence.EntityNotFoundException
import jakarta.validation.ConstraintViolationException
import no.juleoelkalender.exception.InvalidTokenException
import no.juleoelkalender.exception.NotFoundException
import no.juleoelkalender.exception.UserExistException
import org.springframework.dao.DataAccessException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.InvalidDataAccessApiUsageException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice

class RestResponseEntityExceptionHandler : ResponseEntityExceptionHandler() {
    // API
    // 400
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleBadRequest(ex: ConstraintViolationException, request: WebRequest): ResponseEntity<in Any>? = handleExceptionInternal(ex, "Ikke sammenheng mellom inputdata", HttpHeaders(), HttpStatus.BAD_REQUEST, request)

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleBadRequest(ex: DataIntegrityViolationException, request: WebRequest): ResponseEntity<in Any>? = handleExceptionInternal(ex, "Kan ikke tolke inputdata", HttpHeaders(), HttpStatus.BAD_REQUEST, request)

    public override fun handleHttpMessageNotReadable(ex: HttpMessageNotReadableException, headers: HttpHeaders, status: HttpStatusCode, request: WebRequest): ResponseEntity<in Any>? =
            // ex.getCause() instanceof JsonMappingException, JsonParseException // for additional information later on
            handleExceptionInternal(ex, "Kan ikke lese innputdata", headers, HttpStatus.BAD_REQUEST, request)

    public override fun handleMethodArgumentNotValid(ex: MethodArgumentNotValidException, headers: HttpHeaders, status: HttpStatusCode, request: WebRequest): ResponseEntity<in Any>? = handleExceptionInternal(ex, "Ikke tillatt metode", headers, HttpStatus.BAD_REQUEST, request)

    // 401
    @ExceptionHandler(value = [UserExistException::class])
    fun handleUserExistException(ex: RuntimeException, request: WebRequest): ResponseEntity<in Any>? = handleExceptionInternal(ex, ex.localizedMessage, HttpHeaders(), HttpStatus.UNAUTHORIZED, request)

    @ExceptionHandler(value = [BadCredentialsException::class])
    fun handleUnauthorized(ex: RuntimeException, request: WebRequest): ResponseEntity<in Any>? = handleExceptionInternal(ex, "Feil epost eller passord", HttpHeaders(), HttpStatus.UNAUTHORIZED, request)

    @ExceptionHandler(value = [InvalidTokenException::class])
    fun handleInvalidTokenException(ex: RuntimeException, request: WebRequest): ResponseEntity<in Any>? = handleExceptionInternal(ex, ex.localizedMessage, HttpHeaders(), HttpStatus.UNAUTHORIZED, request)

    // 403
    @ExceptionHandler(value = [AuthenticationException::class])
    fun handleForbidden(ex: RuntimeException, request: WebRequest): ResponseEntity<in Any>? = handleExceptionInternal(ex, "Du har ikke tilgang til dette", HttpHeaders(), HttpStatus.FORBIDDEN, request)

    // 404
    @ExceptionHandler(value = [EntityNotFoundException::class])
    fun handleEntityNotFoundException(ex: RuntimeException, request: WebRequest): ResponseEntity<in Any>? = handleExceptionInternal(ex, "Fant ikke det du ba om", HttpHeaders(), HttpStatus.NOT_FOUND, request)

    @ExceptionHandler(value = [NotFoundException::class])
    fun handleNotFound(ex: RuntimeException, request: WebRequest): ResponseEntity<in Any>? = handleExceptionInternal(ex, "Fant ikke det du ba om", HttpHeaders(), HttpStatus.NOT_FOUND, request)

    // 409
    @ExceptionHandler(InvalidDataAccessApiUsageException::class, DataAccessException::class)
    fun handleConflict(ex: RuntimeException, request: WebRequest): ResponseEntity<in Any>? = handleExceptionInternal(ex, "Problemer med tilgang til ressurs", HttpHeaders(), HttpStatus.CONFLICT, request)

    // 412
    // 500
    @ExceptionHandler(NullPointerException::class, IllegalArgumentException::class, IllegalStateException::class)
    fun handleInternal(ex: RuntimeException, request: WebRequest): ResponseEntity<in Any>? {
        logger.error("500 Status Code", ex)
        return handleExceptionInternal(ex, "{\"data\": \"Server error\"}", HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request)
    }
}