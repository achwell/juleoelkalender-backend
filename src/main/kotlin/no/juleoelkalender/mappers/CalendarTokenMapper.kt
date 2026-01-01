package no.juleoelkalender.mappers

import no.juleoelkalender.entity.CalendarTokenEntity
import no.juleoelkalender.model.CalendarToken
import org.springframework.stereotype.Component
import java.time.ZonedDateTime

@Component
class CalendarTokenMapper : BaseMapper<CalendarToken, CalendarTokenEntity> {
    override fun entityToModel(entity: CalendarTokenEntity): CalendarToken = CalendarToken(
            entity.id, entity.token,
            entity.name, entity.active
    )

    override fun modelToEntity(model: CalendarToken): CalendarTokenEntity = CalendarTokenEntity(
            model.id, model.token,
            model.name, model.active, mutableSetOf(),
            mutableSetOf(), ZonedDateTime.now(), null
    )
}
