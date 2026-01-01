package no.juleoelkalender.service.impl

import no.juleoelkalender.entity.CalendarTokenEntity
import no.juleoelkalender.mappers.CalendarTokenMapper
import no.juleoelkalender.model.CalendarToken
import no.juleoelkalender.repository.CalendarTokenRepository
import no.juleoelkalender.service.CalendarTokenService
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.function.Consumer

@Service
class CalendarTokenServiceImpl(private val calendarTokenRepository: CalendarTokenRepository, mapper: CalendarTokenMapper) : BaseServiceImpl<UUID, CalendarToken, CalendarTokenEntity>(
        calendarTokenRepository, mapper
), CalendarTokenService {
    override fun mapModelToEntity(
            model: CalendarToken,
            entity: CalendarTokenEntity
    ) {
        entity.token = model.token
        entity.name = model.name
        entity.active = model.active
    }

    @Throws(RuntimeException::class)
    override fun preDelete(id: UUID) {
        val count = calendarTokenRepository.findAllByActive(true).count { id != it.id }
        if (count < 1) {
            throw RuntimeException("Kan ikke slette siste aktive CalendarToken")
        }
        super.preDelete(id)
    }

    override fun preUpdate(model: CalendarToken) {
        repository.findById(model.id).ifPresent(Consumer { calendarTokenEntity ->
            if (!model.active && calendarTokenEntity.active) {
                val count = calendarTokenRepository.findAllByActive(true).count { calendarTokenEntity.id != it.id }
                if (count < 1) {
                    throw RuntimeException("Kan ikke deaktivere siste aktive CalendarToken")
                }
            }
        })
        super.preUpdate(model)
    }
}