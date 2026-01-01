package no.juleoelkalender.mappers

import no.juleoelkalender.entity.UserEntity
import no.juleoelkalender.model.User
import org.springframework.stereotype.Component

@Component
class UserMapper(
        private val beerMapper: BeerMapper, private val calendarTokenMapper: CalendarTokenMapper,
        private val roleMapper: RoleMapper
) : BaseMapper<User, UserEntity> {
    override fun entityToModel(entity: UserEntity): User = User(
            entity.id, entity.firstName, entity.middleName,
            entity.lastName, entity.email, entity.password,
            entity.area, roleMapper.entityToModel(entity.role),
            entity.locked,
            entity.beers.map { beerMapper.entityToModel(it) }.toSet(),
            entity.calendarToken.map { calendarTokenMapper.entityToModel(it) }.toSet(),
            entity.lastLoginDate, entity.createdDate,
            entity.facebookUserId,
            entity.imageUrl, entity.imageHeight, entity.imageWidth,
            entity.imageSilhouette
    )

    override fun modelToEntity(model: User): UserEntity = UserEntity(
            model.id, model.firstName, model.middleName,
            model.lastName, model.email, model.pwd, model.area,
            roleMapper.modelToEntity(model.role), model.locked,
            model.beers.map { beerMapper.modelToEntity(it) }.toMutableSet(), mutableSetOf(),
            model.calendarToken.map { calendarTokenMapper.modelToEntity(it) }.toMutableSet(),
            mutableSetOf(), model.lastLoginDate, model.createdDate, null,
            model.facebookUserId, model.imageUrl, model.imageHeight, model.imageWidth,
            model.imageSilhouette
    )
}
