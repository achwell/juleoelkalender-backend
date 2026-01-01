package no.juleoelkalender.repository

import no.juleoelkalender.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserRepository : JpaRepository<UserEntity, UUID> {
    fun findByEmailIgnoreCase(email: String): UserEntity?

    fun findByFacebookUserId(facebookUserId: String): UserEntity?
}
