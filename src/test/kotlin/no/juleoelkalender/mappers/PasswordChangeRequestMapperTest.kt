package no.juleoelkalender.mappers

import no.juleoelkalender.entity.PasswordChangeRequestEntity
import no.juleoelkalender.getPasswordChangeRequest
import no.juleoelkalender.getPasswordChangeRequestEntity
import no.juleoelkalender.model.PasswordChangeRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class PasswordChangeRequestMapperTest {
    private lateinit var testSubject: PasswordChangeRequestMapper
    private lateinit var passwordChangeRequest: PasswordChangeRequest
    private lateinit var passwordChangeRequestEntity: PasswordChangeRequestEntity

    @BeforeEach
    fun setUp() {
        passwordChangeRequest = getPasswordChangeRequest()
        passwordChangeRequestEntity = getPasswordChangeRequestEntity()
        testSubject = PasswordChangeRequestMapper()
    }

    @Test
    fun testEntityToModel() {
        val model = testSubject.entityToModel(
                passwordChangeRequestEntity
        )
        assertAll(
                { assertNotNull(model) }, {
            assertEquals(
                    passwordChangeRequestEntity.id,
                    model.id
            )
        },
                { assertEquals(passwordChangeRequestEntity.email, model.email) },
                { assertEquals(passwordChangeRequestEntity.token, model.token) })
    }

    @Test
    fun testModelToEntity() {
        val entity = testSubject.modelToEntity(
                passwordChangeRequest
        )
        assertAll(
                { assertNotNull(entity) },
                { assertEquals(passwordChangeRequest.id, entity.id) },
                { assertEquals(passwordChangeRequest.email, entity.email) },
                { assertEquals(passwordChangeRequest.token, entity.token) })
    }
}
