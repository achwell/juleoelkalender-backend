package no.juleoelkalender.mappers

import no.juleoelkalender.entity.PasswordChangeRequestEntity
import no.juleoelkalender.model.PasswordChangeRequest
import org.springframework.stereotype.Component

@Component
class PasswordChangeRequestMapper : BaseMapper<PasswordChangeRequest, PasswordChangeRequestEntity> {
    override fun entityToModel(entity: PasswordChangeRequestEntity): PasswordChangeRequest = PasswordChangeRequest(
            entity.id,
            entity.token, entity.email,
            entity.created
    )

    override fun modelToEntity(model: PasswordChangeRequest): PasswordChangeRequestEntity = PasswordChangeRequestEntity(
            model.id,
            model.token, model.email,
            model.created, null
    )
}
