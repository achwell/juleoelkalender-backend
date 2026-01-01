package no.juleoelkalender.mappers

import no.juleoelkalender.entity.BeerEntity
import no.juleoelkalender.entity.ReviewEntity
import no.juleoelkalender.model.Beer
import no.juleoelkalender.model.ReviewWithoutChildren
import org.springframework.stereotype.Component

@Component
class BeerMapper(private val userWithoutChildrenMapper: UserWithoutChildrenMapper) : BaseMapper<Beer, BeerEntity> {
    override fun entityToModel(entity: BeerEntity): Beer = Beer(
            entity.id, entity.name, entity.style,
            entity.description, entity.abv, entity.ibu, entity.ebc,
            entity.recipe, entity.untapped, entity.brewedDate,
            entity.bottleDate, entity.archived,
            userWithoutChildrenMapper.entityToModel(entity.user),
            entity.reviews.map { this.mapReview(it) }.toSet(),
            entity.createdDate,
            entity.desiredDate
    )

    override fun modelToEntity(model: Beer): BeerEntity = BeerEntity(
            model.id, model.name, model.style, model.description,
            model.abv, model.ibu, model.ebc, model.recipe, model.untapped,
            model.brewedDate, model.bottleDate, model.archived,
            userWithoutChildrenMapper.modelToEntity(model.brewer), mutableSetOf(),
            mutableSetOf(), model.createdDate, null,
            model.desiredDate
    )

    private fun mapReview(r: ReviewEntity): ReviewWithoutChildren = ReviewWithoutChildren(
            r.id, r.ratingLabel, r.ratingLooks,
            r.ratingSmell, r.ratingTaste, r.ratingFeel, r.ratingOverall,
            r.comment, r.createdAt
    )
}
