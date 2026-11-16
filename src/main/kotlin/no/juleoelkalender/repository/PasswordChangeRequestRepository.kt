package no.juleoelkalender.repository

import no.juleoelkalender.entity.PasswordChangeRequestEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime
import java.util.UUID

interface PasswordChangeRequestRepository : JpaRepository<PasswordChangeRequestEntity, UUID> {
    @Modifying
    @Transactional
    @Query("delete from PasswordChangeRequestEntity request where request.created < :cutoff")
    fun deleteExpiredBefore(@Param("cutoff") cutoff: ZonedDateTime): Int
}
