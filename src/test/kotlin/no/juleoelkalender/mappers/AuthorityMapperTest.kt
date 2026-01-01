package no.juleoelkalender.mappers

import no.juleoelkalender.entity.AuthorityEntity
import no.juleoelkalender.entity.UserEntity
import no.juleoelkalender.getAuthorityEntityUser
import no.juleoelkalender.getUserEntity
import no.juleoelkalender.model.Authority
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

internal class AuthorityMapperTest {
    private lateinit var testSubject: AuthorityMapper

    private val authorityEntityUser: AuthorityEntity = getAuthorityEntityUser()
    private val userEntity: UserEntity = getUserEntity()

    @BeforeEach
    fun setUp() {
        testSubject = AuthorityMapper()
    }

    @Test
    fun testEntityToModel() {
        val authority = testSubject.entityToModel(authorityEntityUser)
        assertAll(
                { assertNotNull(authority) },
                { assertEquals(authorityEntityUser.name, authority.name) },
                { assertEquals(authorityEntityUser.id, authority.id) })
    }

    @Test
    fun testModelToEntity() {
        val authority = Authority(UUID.randomUUID(), userEntity.name)
        val authorityEntity = testSubject.modelToEntity(authority)
        assertAll(
                { assertNotNull(authorityEntity) },
                { assertEquals(authority.name, authorityEntity.name) },
                { assertEquals(authority.id, authorityEntity.id) })
    }
}
