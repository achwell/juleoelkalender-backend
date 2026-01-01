package no.juleoelkalender.mappers

import no.juleoelkalender.entity.BeerStyleEntity
import no.juleoelkalender.model.BeerStyle
import org.springframework.stereotype.Component

@Component
class BeerStyleMapper : BaseMapper<BeerStyle, BeerStyleEntity> {
    override fun entityToModel(entity: BeerStyleEntity): BeerStyle = BeerStyle(id = entity.id, name = entity.name)
    override fun modelToEntity(model: BeerStyle): BeerStyleEntity = BeerStyleEntity(model.id, model.name)
}
