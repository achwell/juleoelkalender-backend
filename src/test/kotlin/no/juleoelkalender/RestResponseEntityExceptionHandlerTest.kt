package no.juleoelkalender

import io.mockk.mockk
import jakarta.persistence.EntityNotFoundException
import jakarta.validation.ConstraintViolation
import jakarta.validation.ConstraintViolationException
import no.juleoelkalender.exception.InvalidTokenException
import no.juleoelkalender.exception.NotFoundException
import no.juleoelkalender.exception.UserExistException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertNotNull
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.InvalidDataAccessApiUsageException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.mock.http.client.MockClientHttpResponse
import org.springframework.security.authentication.AccountExpiredException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.context.request.WebRequest
import kotlin.test.assertEquals

internal class RestResponseEntityExceptionHandlerTest {
    private lateinit var testSubject: RestResponseEntityExceptionHandler
    private val webRequest = mockk<WebRequest>()
    private val methodArgumentNotValidException = mockk<MethodArgumentNotValidException>()

    @BeforeEach
    fun setUp() {
        testSubject = RestResponseEntityExceptionHandler()
    }

    @Test
    fun testHandleBadRequestConstraintViolationException() {
        val responseEntity = testSubject.handleBadRequest(
                ConstraintViolationException(mutableSetOf<ConstraintViolation<*>?>()), webRequest
        )
        assertAll(
                { assertNotNull(responseEntity) },
                { assertEquals(HttpStatusCode.valueOf(400), responseEntity?.statusCode) }
        )
    }

    @Test
    fun testHandleBadRequestDataIntegrityViolationException() {
        val responseEntity = testSubject.handleBadRequest(
                DataIntegrityViolationException(""), webRequest
        )
        assertAll(
                { assertNotNull(responseEntity) },
                { assertEquals(HttpStatusCode.valueOf(400), responseEntity?.statusCode) }
        )
    }

    @Test
    fun testHandleHttpMessageNotReadable() {
        val responseEntity = testSubject.handleHttpMessageNotReadable(
                HttpMessageNotReadableException("", MockClientHttpResponse("".toByteArray(), 400)),
                HttpHeaders(), HttpStatusCode.valueOf(400), webRequest
        )
        assertAll(
                { assertNotNull(responseEntity) },
                { assertEquals(HttpStatusCode.valueOf(400), responseEntity?.statusCode) }
        )
    }

    @Test
    fun testHandleMethodArgumentNotValid() {
        val responseEntity = testSubject.handleMethodArgumentNotValid(
                methodArgumentNotValidException, HttpHeaders(), HttpStatusCode.valueOf(400),
                webRequest
        )
        assertAll(
                { assertNotNull(responseEntity) },
                { assertEquals(HttpStatusCode.valueOf(400), responseEntity?.statusCode) }
        )
    }

    @Test
    fun testHandleUserExistException() {
        val responseEntity = testSubject.handleUserExistException(
                UserExistException("User exist"), webRequest
        )
        assertAll(
                { assertNotNull(responseEntity) },
                { assertEquals(HttpStatusCode.valueOf(401), responseEntity?.statusCode) }
        )
    }

    @Test
    fun testHandleUnauthorized() {
        val responseEntity = testSubject.handleUnauthorized(
                BadCredentialsException(""), webRequest
        )
        assertAll(
                { assertNotNull(responseEntity) },
                { assertEquals(HttpStatusCode.valueOf(401), responseEntity?.statusCode) }
        )
    }

    @Test
    fun testHandleInvalidTokenException() {
        val responseEntity = testSubject.handleInvalidTokenException(
                InvalidTokenException(""), webRequest
        )
        assertAll(
                { assertNotNull(responseEntity) },
                { assertEquals(HttpStatusCode.valueOf(401), responseEntity?.statusCode) }
        )
    }

    @Test
    fun testHandleForbidden() {
        val responseEntity = testSubject.handleForbidden(
                AccountExpiredException(""), webRequest
        )
        assertAll(
                { assertNotNull(responseEntity) },
                { assertEquals(HttpStatusCode.valueOf(403), responseEntity?.statusCode) }
        )
    }

    @Test
    fun testHandleEntityNotFoundException() {
        val responseEntity = testSubject.handleEntityNotFoundException(
                EntityNotFoundException(""), webRequest
        )
        assertAll(
                { assertNotNull(responseEntity) },
                { assertEquals(HttpStatusCode.valueOf(404), responseEntity?.statusCode) }
        )
    }

    @Test
    fun testHandleNotFound() {
        val responseEntity = testSubject.handleNotFound(
                NotFoundException(""),
                webRequest
        )
        assertAll(
                { assertNotNull(responseEntity) },
                { assertEquals(HttpStatusCode.valueOf(404), responseEntity?.statusCode) }
        )
    }

    @Test
    fun testHandleConflict() {
        val responseEntity = testSubject.handleConflict(
                InvalidDataAccessApiUsageException(""), webRequest
        )
        assertAll(
                { assertNotNull(responseEntity) },
                { assertEquals(HttpStatusCode.valueOf(409), responseEntity?.statusCode) }
        )
    }

    @Test
    fun testHandleInternal() {
        val responseEntity = testSubject.handleInternal(
                IllegalArgumentException(""), webRequest
        )
        assertAll(
                { assertNotNull(responseEntity) },
                { assertEquals(HttpStatusCode.valueOf(500), responseEntity?.statusCode) }
        )
    }
}