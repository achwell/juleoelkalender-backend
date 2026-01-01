package no.juleoelkalender.mappers

import no.juleoelkalender.entity.RoleEntity
import no.juleoelkalender.entity.RoleNameEntity
import no.juleoelkalender.model.Role
import no.juleoelkalender.model.RoleName
import org.springframework.stereotype.Component

@Component
class RoleMapper(private val authorityMapper: AuthorityMapper) : BaseMapper<Role, RoleEntity> {
    override fun entityToModel(entity: RoleEntity): Role = Role(
            entity.id, RoleName.valueOf(entity.name.name),
            entity.authorities.map { authorityMapper.entityToModel(it) }.toSet())

    override fun modelToEntity(model: Role): RoleEntity = RoleEntity(
            model.id, RoleNameEntity.valueOf(model.name.name),
            model.authorities.map { authorityMapper.modelToEntity(it) }.toMutableSet(), mutableSetOf())
}
