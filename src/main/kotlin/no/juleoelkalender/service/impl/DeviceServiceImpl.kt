package no.juleoelkalender.service.impl

import no.juleoelkalender.entity.DeviceEntity
import no.juleoelkalender.mappers.DeviceMapper
import no.juleoelkalender.model.Device
import no.juleoelkalender.repository.DeviceRepository
import no.juleoelkalender.repository.UserRepository
import no.juleoelkalender.service.DeviceService
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class DeviceServiceImpl(
    repository: DeviceRepository, mapper: DeviceMapper, private val userRepository: UserRepository
) : BaseServiceImpl<UUID, Device, DeviceEntity>(repository, mapper), DeviceService {

    override fun preCreate(model: Device): DeviceEntity {
        val userId = requireNotNull(model.user.id) { "A user ID is required to create a device" }
        return mapper.modelToEntity(model).apply {
            user = userRepository.getReferenceById(userId)
        }
    }

    override fun mapModelToEntity(model: Device, entity: DeviceEntity) {
        entity.mobileVendor = model.mobileVendor
        entity.mobileModel = model.mobileModel
        entity.mobile = model.isMobile
        entity.osName = model.osName
        entity.osVersion = model.osVersion
        entity.browserName = model.browserName
        entity.browserVersion = model.browserVersion
        val userId = requireNotNull(model.user.id) { "A user ID is required to update a device" }
        entity.user = userRepository.getReferenceById(userId)
    }
}