package no.juleoelkalender.repository

import no.juleoelkalender.entity.CalendarTokenEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CalendarTokenRepository : JpaRepository<CalendarTokenEntity, UUID> {
    fun findCalendarTokenByToken(token: String): CalendarTokenEntity?

    fun findAllByActive(active: Boolean): Collection<CalendarTokenEntity>
}
