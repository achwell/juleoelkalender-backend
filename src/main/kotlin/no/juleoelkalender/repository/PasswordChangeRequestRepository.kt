package no.juleoelkalender.repository

import no.juleoelkalender.entity.PasswordChangeRequestEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PasswordChangeRequestRepository : JpaRepository<PasswordChangeRequestEntity, UUID>
