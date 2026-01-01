package no.juleoelkalender.mappers

import no.juleoelkalender.entity.AuthorityEntity
import no.juleoelkalender.model.Authority
import org.springframework.stereotype.Component

@Component
class AuthorityMapper : BaseMapper<Authority, AuthorityEntity> {
    override fun entityToModel(entity: AuthorityEntity): Authority = Authority(id = entity.id, name = entity.name)
    override fun modelToEntity(model: Authority): AuthorityEntity = AuthorityEntity(id = model.id, name = model.name, users = mutableSetOf())
}
