package no.juleoelkalender.mappers

import no.juleoelkalender.entity.DeviceEntity
import no.juleoelkalender.model.Device
import org.springframework.stereotype.Component
import java.time.ZonedDateTime

@Component
class DeviceMapper(private val userWithoutChildrenMapper: UserWithoutChildrenMapper) : BaseMapper<Device, DeviceEntity> {
    override fun entityToModel(entity: DeviceEntity): Device = Device(
            entity.id, entity.mobileVendor,
            entity.mobileModel, entity.mobile, entity.osName,
            entity.osVersion, entity.browserName,
            entity.browserVersion,
            userWithoutChildrenMapper.entityToModel(entity.user)
    )

    override fun modelToEntity(model: Device): DeviceEntity = DeviceEntity(
            model.id, model.mobileVendor, model.mobileModel,
            model.isMobile, model.osName,
            model.osVersion, model.browserName, model.browserVersion,
            userWithoutChildrenMapper.modelToEntity(model.user), ZonedDateTime.now(), null
    )
}
