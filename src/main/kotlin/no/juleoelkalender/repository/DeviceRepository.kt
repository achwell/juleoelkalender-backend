package no.juleoelkalender.repository

import no.juleoelkalender.entity.DeviceEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface DeviceRepository : JpaRepository<DeviceEntity, UUID> {
    fun deleteByUserId(userId: UUID)
}
