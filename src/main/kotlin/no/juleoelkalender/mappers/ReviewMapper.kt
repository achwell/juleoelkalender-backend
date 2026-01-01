package no.juleoelkalender.mappers

import no.juleoelkalender.entity.ReviewEntity
import no.juleoelkalender.model.Review
import org.springframework.stereotype.Component

@Component
class ReviewMapper(
        private val beerMapper: BeerMapper, private val calendarMapper: CalendarMapper,
        private val userWithoutChildrenMapper: UserWithoutChildrenMapper
) : BaseMapper<Review, ReviewEntity> {
    override fun entityToModel(entity: ReviewEntity): Review {
        return Review(
                entity.id, entity.ratingLabel,
                entity.ratingLooks,
                entity.ratingSmell, entity.ratingTaste, entity.ratingFeel,
                entity.ratingOverall, entity.comment, entity.createdAt,
                beerMapper.entityToModel(entity.beer),
                calendarMapper.entityToModel(entity.calendar),
                userWithoutChildrenMapper.entityToModel(entity.user)
        )
    }

    override fun modelToEntity(model: Review): ReviewEntity {
        return ReviewEntity(
                model.id!!, model.ratingLabel, model.ratingLooks,
                model.ratingSmell, model.ratingTaste, model.ratingFeel,
                model.ratingOverall, model.comment, model.createdAt, null,
                beerMapper.modelToEntity(model.beer),
                calendarMapper.modelToEntity(model.calendar),
                userWithoutChildrenMapper.modelToEntity(model.user)
        )
    }
}
