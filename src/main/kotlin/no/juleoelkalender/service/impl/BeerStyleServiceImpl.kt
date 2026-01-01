package no.juleoelkalender.service.impl

import no.juleoelkalender.entity.BeerStyleEntity
import no.juleoelkalender.mappers.BeerStyleMapper
import no.juleoelkalender.model.BeerStyle
import no.juleoelkalender.repository.BeerStyleRepository
import no.juleoelkalender.service.BeerStyleService
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class BeerStyleServiceImpl(repository: BeerStyleRepository, mapper: BeerStyleMapper) : BaseServiceImpl<UUID, BeerStyle, BeerStyleEntity>(repository, mapper), BeerStyleService {
    override fun mapModelToEntity(model: BeerStyle, entity: BeerStyleEntity) {
        entity.name = model.name
    }
}