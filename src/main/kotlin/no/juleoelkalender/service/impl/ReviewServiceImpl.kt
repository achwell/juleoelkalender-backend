package no.juleoelkalender.service.impl

import no.juleoelkalender.entity.CalendarEntity
import no.juleoelkalender.entity.CalendarTokenEntity
import no.juleoelkalender.entity.ReviewEntity
import no.juleoelkalender.mappers.BeerMapper
import no.juleoelkalender.mappers.CalendarMapper
import no.juleoelkalender.mappers.ReviewMapper
import no.juleoelkalender.mappers.UserWithoutChildrenMapper
import no.juleoelkalender.model.Beer
import no.juleoelkalender.model.Calendar
import no.juleoelkalender.model.Review
import no.juleoelkalender.model.ReviewData
import no.juleoelkalender.model.ReviewWithUser
import no.juleoelkalender.model.UserWithoutChildren
import no.juleoelkalender.repository.BeerRepository
import no.juleoelkalender.repository.CalendarRepository
import no.juleoelkalender.repository.ReviewRepository
import no.juleoelkalender.repository.UserRepository
import no.juleoelkalender.service.LocalesService
import no.juleoelkalender.service.ReviewService
import no.juleoelkalender.utils.ExcelGenerator
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.util.UUID

@Service
class ReviewServiceImpl(
    private val reviewRepository: ReviewRepository, private val userRepository: UserRepository,
    private val beerRepository: BeerRepository, private val calendarRepository: CalendarRepository, private val beerMapper: BeerMapper,
    private val calendarMapper: CalendarMapper, mapper: ReviewMapper,
    private val userWithoutChildrenMapper: UserWithoutChildrenMapper, private val excelGenerator: ExcelGenerator,
    private val localesService: LocalesService
) : BaseServiceImpl<UUID, Review, ReviewEntity>(reviewRepository, mapper), ReviewService {

    override val reviewsWithUser: Set<ReviewWithUser>
        get() {
            val calendarTokenId = currentUsersActiveCalendarTokenId() ?: return emptySet()
            return repository.findAll()
                .asSequence()
                .map(mapper::entityToModel)
                .filter { it.calendar.calendarToken.id == calendarTokenId }
                .groupBy { ReviewGroupKey(it.calendar.id, it.beer.id) }
                .values
                .map(::toReviewWithUser)
                .toSet()
        }

    override fun getReviewByCalendarBeerAndReviewer(calendarId: UUID, beerId: UUID, reviewerId: UUID): Review {
        val beerEntity = beerRepository.getReferenceById(beerId)
        val calendarEntity = calendarRepository.getReferenceById(calendarId)
        val userEntity = userRepository.getReferenceById(reviewerId)
        val reviewEntity = reviewRepository.findByBeerAndCalendarAndUser(beerEntity, calendarEntity, userEntity)
        return reviewEntity?.let(mapper::entityToModel)
            ?: createEmptyReview(beerEntity, calendarEntity, userEntity)
    }

    override fun preCreate(model: Review): ReviewEntity {
        val reviewEntity = mapper.modelToEntity(model)
        currentUser()?.let { user ->
            user.calendarToken = user.calendarToken.filter(CalendarTokenEntity::active).toMutableSet()
            reviewEntity.user = user
        }
        return reviewEntity
    }

    @Throws(RuntimeException::class)
    override fun preDelete(id: UUID) {
        if (!reviewRepository.existsById(id)) {
            return
        }
        val review = reviewRepository.findById(id).orElse(null) ?: return
        validateDeleteAccess(review)
    }

    override fun mapModelToEntity(model: Review, entity: ReviewEntity) {
        entity.comment = model.comment
        entity.ratingFeel = model.ratingFeel
        entity.ratingLabel = model.ratingLabel
        entity.ratingLooks = model.ratingLooks
        entity.ratingOverall = model.ratingOverall
        entity.ratingSmell = model.ratingSmell
        entity.ratingTaste = model.ratingTaste
    }

    override fun getReviewDataByBeerId(beerId: UUID): Set<ReviewData> {
        val beerEntity = beerRepository.findById(beerId).orElse(null) ?: return emptySet()
        val allReviewsByCalendar = reviewRepository.findAll().map(mapper::entityToModel).groupBy { it.calendar.id }
        val beer = beerMapper.entityToModel(beerEntity)
        val calendarCache = mutableMapOf<UUID, CalendarEntity?>()

        return beerEntity.beerCalendars.mapNotNull { beerCalendar ->
            val calendarId = beerCalendar.calendar.id ?: return@mapNotNull null
            val calendarEntity = calendarCache.getOrPut(calendarId) {
                calendarRepository.findById(calendarId).orElse(null)
            } ?: return@mapNotNull null
            val calendar = calendarMapper.entityToModel(calendarEntity)
            val reviewsByCalendar = allReviewsByCalendar[calendarId].orEmpty()
            val average = calculateAverage(reviewsByCalendar, calendar, beer, beer.brewer).apply {
                this.beer = beer
                this.calendar = calendar
            }
            val reviews = reviewsByCalendar.filter { it.beer.id == beerEntity.id }.toSet()
            ReviewData(reviews, calendar, average)
        }.toSet()
    }

    override fun calculateAverage(reviews: Collection<Review>, calendar: Calendar, beer: Beer, user: UserWithoutChildren): Review {
        val average = Review(null, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, null, ZonedDateTime.now(), beer, calendar, user)
        if (reviews.isEmpty()) {
            return average
        }

        average.ratingFeel = reviews.sumOf { it.ratingFeel } / reviews.size
        average.ratingLabel = reviews.sumOf { it.ratingLabel } / reviews.size
        average.ratingLooks = reviews.sumOf { it.ratingLooks } / reviews.size
        average.ratingOverall = reviews.sumOf { it.ratingOverall } / reviews.size
        average.ratingSmell = reviews.sumOf { it.ratingSmell } / reviews.size
        average.ratingTaste = reviews.sumOf { it.ratingTaste } / reviews.size
        return average
    }

    override fun getReviewsXlsx(locale: String): ByteArray {
        val headers = arrayOf(
            localesService.getString(locale, "pages.totalreviews.calendar.year"),
            localesService.getString(locale, "pages.totalreviews.calendar.calendar"),
            localesService.getString(locale, "pages.totalreviews.calendar.beer"),
            localesService.getString(locale, "beer.brewer"),
            localesService.getString(locale, "beer.style"),
            localesService.getString(locale, "rating.feel"),
            localesService.getString(locale, "rating.taste"),
            localesService.getString(locale, "rating.smell"),
            localesService.getString(locale, "rating.label"),
            localesService.getString(locale, "rating.looks"),
            localesService.getString(locale, "rating.overall"),
            localesService.getString(locale, "rating.total")
        )
        val rowData = reviewsWithUser.map {
            arrayOf<Any>(
                it.calendar.year, it.calendar.name,
                it.beer.name, it.beer.brewer.name(), it.beer.style,
                it.ratingFeel, it.ratingTaste, it.ratingSmell, it.ratingLabel,
                it.ratingLooks, it.ratingOverall, it.total
            )
        }.toList()
        val sheetname = localesService.getString(locale, "menu.reviews")
        return excelGenerator.generateReport(sheetname, headers, rowData)
    }

    private fun currentUsersActiveCalendarTokenId(): UUID? {
        return currentUser()?.calendarToken?.firstOrNull(CalendarTokenEntity::active)?.id
    }

    private fun currentUser() = currentPrincipalEmail()?.let(userRepository::findByEmailIgnoreCase)

    private fun currentPrincipalEmail() = SecurityContextHolder.getContext().authentication?.principal as? String

    private fun createEmptyReview(beerEntity: no.juleoelkalender.entity.BeerEntity, calendarEntity: CalendarEntity, userEntity: no.juleoelkalender.entity.UserEntity): Review {
        val beer = beerMapper.entityToModel(beerEntity)
        val calendar = calendarMapper.entityToModel(calendarEntity)
        val user = userWithoutChildrenMapper.entityToModel(userEntity)
        return Review(null, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, "", ZonedDateTime.now(), beer, calendar, user)
    }

    private fun toReviewWithUser(reviews: List<Review>): ReviewWithUser {
        val latestReview = reviews.last()
        latestReview.updateRatings(calculateAverage(reviews, latestReview.calendar, latestReview.beer, latestReview.beer.brewer))
        return toReviewWithTotal(latestReview.beer, latestReview)
    }

    private fun validateDeleteAccess(review: ReviewEntity) {
        val authentication = SecurityContextHolder.getContext().authentication
        val authorities = authentication?.authorities.orEmpty()
        val currentUserEmail = authentication?.principal as? String
        val canDeleteOthers = authorities.any { it.authority == "review:delete_other" }

        if (authorities.isEmpty() || (!canDeleteOthers && currentUserEmail != review.user.email)) {
            throw InsufficientAuthenticationException("Ikke lov til å slette andres tilbakemeldinger")
        }
    }

    private fun toReviewWithTotal(beer: Beer, review: Review): ReviewWithUser {
        val total = review.ratingSmell + review.ratingLooks + review.ratingTaste + review.ratingFeel + review.ratingOverall + review.ratingLabel
        return ReviewWithUser(
            review.id!!, review.ratingLabel, review.ratingLooks,
            review.ratingSmell, review.ratingTaste, review.ratingFeel,
            review.ratingOverall, review.comment, review.createdAt, beer,
            review.calendar, review.user, total
        )
    }

    private data class ReviewGroupKey(val calendarId: UUID?, val beerId: UUID?)
}