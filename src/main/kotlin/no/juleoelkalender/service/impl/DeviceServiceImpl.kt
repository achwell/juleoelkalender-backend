package no.juleoelkalender.service.impl

import no.juleoelkalender.entity.BeerEntity
import no.juleoelkalender.entity.DeviceEntity
import no.juleoelkalender.mappers.DeviceMapper
import no.juleoelkalender.mappers.UserWithoutChildrenMapper
import no.juleoelkalender.model.Device
import no.juleoelkalender.repository.DeviceRepository
import no.juleoelkalender.service.DeviceService
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.function.Consumer

@Service
class DeviceServiceImpl(
        repository: DeviceRepository, mapper: DeviceMapper,
        private val userWithoutChildrenMapper: UserWithoutChildrenMapper
) : BaseServiceImpl<UUID, Device, DeviceEntity>(repository, mapper), DeviceService {

    override fun preCreate(model: Device): DeviceEntity {
        val deviceEntity = repository.save(mapper.modelToEntity(model))
        val userEntity = deviceEntity.user
        val beerEntityList: MutableSet<BeerEntity> = mutableSetOf()
        userEntity.beers.forEach(Consumer { beer: BeerEntity ->
            beer.user = userEntity
            beerEntityList.add(beer)
        })
        userEntity.beers = beerEntityList
        deviceEntity.user = userEntity
        return deviceEntity
    }

    override fun mapModelToEntity(model: Device, entity: DeviceEntity) {
        entity.mobileVendor = model.mobileVendor
        entity.mobileModel = model.mobileModel
        entity.mobile = model.isMobile
        entity.osName = model.osName
        entity.osVersion = model.osVersion
        entity.browserName = model.browserName
        entity.browserVersion = model.browserVersion
        entity.user = userWithoutChildrenMapper.modelToEntity(model.user)
    }
}