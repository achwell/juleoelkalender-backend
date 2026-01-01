package no.juleoelkalender.mappers

import no.juleoelkalender.entity.UserEntity
import no.juleoelkalender.model.UserWithoutChildren
import org.springframework.stereotype.Component

@Component
class UserWithoutChildrenMapper(private val calendarTokenMapper: CalendarTokenMapper, private val roleMapper: RoleMapper) : BaseMapper<UserWithoutChildren, UserEntity> {
    override fun entityToModel(entity: UserEntity): UserWithoutChildren = UserWithoutChildren(
            entity.id, entity.firstName,
            entity.middleName, entity.lastName, entity.email,
            entity.password, entity.area,
            roleMapper.entityToModel(entity.role), entity.locked,
            entity.calendarToken.map { calendarTokenMapper.entityToModel(it) }.toSet(), entity.lastLoginDate,
            entity.createdDate, entity.facebookUserId, entity.imageUrl,
            entity.imageHeight, entity.imageWidth, entity.imageSilhouette)

    override fun modelToEntity(model: UserWithoutChildren): UserEntity = UserEntity(
            model.id, model.firstName,
            model.middleName, model.lastName,
            model.email, model.password, model.area,
            roleMapper.modelToEntity(model.role), model.locked,
            mutableSetOf(), mutableSetOf(),
            model.calendarToken.map { calendarTokenMapper.modelToEntity(it) }.toMutableSet(), mutableSetOf(), model.lastLoginDate,
            model.createdDate, null, model.facebookUserId,
            model.imageUrl, model.imageHeight,
            model.imageWidth, model.imageSilhouette)
}
