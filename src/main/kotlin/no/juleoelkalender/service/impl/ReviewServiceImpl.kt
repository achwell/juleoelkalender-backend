package no.juleoelkalender.service.impl

import no.juleoelkalender.entity.CalendarEntity
import no.juleoelkalender.entity.CalendarTokenEntity
import no.juleoelkalender.entity.ReviewEntity
import no.juleoelkalender.mappers.BeerMapper
import no.juleoelkalender.mappers.CalendarMapper
import no.juleoelkalender.mappers.ReviewMapper
import no.juleoelkalender.mappers.UserWithoutChildrenMapper
import no.juleoelkalender.model.*
import no.juleoelkalender.repository.BeerRepository
import no.juleoelkalender.repository.CalendarRepository
import no.juleoelkalender.repository.ReviewRepository
import no.juleoelkalender.repository.UserRepository
import no.juleoelkalender.service.LocalesService
import no.juleoelkalender.service.ReviewService
import no.juleoelkalender.utils.ExcelGenerator
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.util.UUID
import java.util.function.Consumer

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
            val email = SecurityContextHolder.getContext().authentication?.principal as String?
            var calendarTokenId: UUID? = null
            if (email != null) {
                val user = userRepository.findByEmailIgnoreCase(email)
                calendarTokenId = user?.calendarToken?.filter { it.active }?.map { it.id }?.firstOrNull()
            }
            val average: MutableMap<String, ReviewWithUser> = mutableMapOf()
            repository.findAll().map { mapper.entityToModel(it) }.forEach {
                val calendar = it.calendar
                val beer = it.beer
                if (calendar.calendarToken.id == calendarTokenId) {
                    val key = calendar.id.toString() + beer.id
                    val existingAverage = average[key]
                    if (existingAverage != null) {
                        val oldReview = Review(
                                existingAverage.id, existingAverage.ratingLabel,
                                existingAverage.ratingLooks, existingAverage.ratingSmell,
                                existingAverage.ratingTaste, existingAverage.ratingFeel,
                                existingAverage.ratingOverall, existingAverage.comment,
                                existingAverage.createdAt, existingAverage.beer, existingAverage.calendar,
                                existingAverage.user
                        )
                        val newAverage = calculateAverage(listOf(oldReview, it), calendar, beer, beer.brewer)
                        it.updateRatings(newAverage)
                        average[key] = getTotal(beer, it)
                    } else {
                        average[key] = getTotal(beer, it)
                    }
                }
            }
            return average.values.toSet()
        }

    override fun getReviewByCalendarBeerAndReviewer(calendarId: UUID, beerId: UUID, reviewerId: UUID): Review {
        val beerEntity = beerRepository.getReferenceById(beerId)
        val calendarEntity = calendarRepository.getReferenceById(calendarId)
        val userEntity = userRepository.getReferenceById(reviewerId)
        val reviewEntity = reviewRepository.findByBeerAndCalendarAndUser(beerEntity, calendarEntity, userEntity)
        if (reviewEntity != null) {
            return mapper.entityToModel(reviewEntity)
        }
        val beer = beerMapper.entityToModel(beerEntity)
        val calendar = calendarMapper.entityToModel(calendarEntity)
        val user = userWithoutChildrenMapper.entityToModel(userEntity)
        return Review(null, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, "", ZonedDateTime.now(), beer, calendar, user)
    }

    override fun preCreate(model: Review): ReviewEntity {
        val email = SecurityContextHolder.getContext().authentication?.principal as String?
        val reviewEntity = mapper.modelToEntity(model)
        if (email != null) {
            val user = userRepository.findByEmailIgnoreCase(email)
            if (user != null) {
                user.calendarToken = user.calendarToken.filter(CalendarTokenEntity::active).toMutableSet()
                reviewEntity.user = user
            }
        }
        return reviewEntity
    }

    @Throws(RuntimeException::class)
    override fun preDelete(id: UUID) {
        if (reviewRepository.existsById(id)) {
            beerRepository.findById(id).ifPresent { beerEntity ->
                val authentication = SecurityContextHolder.getContext().authentication
                var authorities: MutableCollection<out GrantedAuthority> = mutableListOf()
                if (authentication != null) {
                    authorities = authentication.authorities
                }
                if (authorities.isEmpty()) {
                    throw InsufficientAuthenticationException("Ikke lov til å slette andres tilbakemeldinger")
                }
                if (authorities.none { "review:delete_other" == it.authority } && (authentication?.principal != beerEntity.user.email)) {
                    throw InsufficientAuthenticationException("Ikke lov til å slette andres tilbakemeldinger")
                }

            }
        }
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
        val reviewDatas: MutableSet<ReviewData> = mutableSetOf()
        val calendarMap: MutableMap<UUID, CalendarEntity> = mutableMapOf()
        beerRepository.findById(beerId).ifPresent { beerEntity ->
            val allReviews = reviewRepository.findAll().map { mapper.entityToModel(it) }
            for (beerCalendar in beerEntity.beerCalendars) {
                val calendarId = beerCalendar.calendar.id
                var calendarEntity = calendarMap[calendarId]
                if (calendarEntity == null && calendarId != null) {
                    calendarRepository.findById(calendarId).ifPresent { calendar -> calendarMap[calendarId] = calendar }
                    calendarEntity = calendarMap[calendarId]
                }
                if (calendarEntity != null) {
                    val reviewsByCalendar = allReviews.filter { it.calendar.id === calendarId }
                    val calendar = calendarMapper.entityToModel(calendarEntity)
                    val beer = beerMapper.entityToModel(beerEntity)
                    val average = calculateAverage(reviewsByCalendar, calendar, beer, beer.brewer)
                    average.beer = beer
                    average.calendar = calendar
                    val reviews = reviewsByCalendar.filter { it.beer.id === beerEntity.id }.toSet()
                    val reviewData = ReviewData(reviews, calendar, average)
                    reviewDatas.add(reviewData)
                }
            }
        }
        return reviewDatas
    }

    override fun calculateAverage(reviews: Collection<Review>, calendar: Calendar, beer: Beer, user: UserWithoutChildren): Review {
        val average = Review(null, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, null, ZonedDateTime.now(), beer, calendar, user)
        if (reviews.isEmpty()) {
            return average
        }
        reviews.forEach(Consumer { review: Review? ->
            average.ratingFeel += review!!.ratingFeel
            average.ratingLabel += review.ratingLabel
            average.ratingLooks += review.ratingLooks
            average.ratingOverall += review.ratingOverall
            average.ratingSmell += review.ratingSmell
            average.ratingTaste += review.ratingTaste
        })
        average.ratingFeel /= reviews.size
        average.ratingLabel /= reviews.size
        average.ratingLooks /= reviews.size
        average.ratingOverall /= reviews.size
        average.ratingSmell /= reviews.size
        average.ratingTaste /= reviews.size
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

    private fun getTotal(beer: Beer, review: Review): ReviewWithUser {
        val total = (review.ratingSmell + review.ratingLooks + review.ratingTaste + review.ratingFeel
                + review.ratingOverall + review.ratingLabel)
        return ReviewWithUser(
                review.id!!, review.ratingLabel, review.ratingLooks,
                review.ratingSmell, review.ratingTaste, review.ratingFeel,
                review.ratingOverall, review.comment, review.createdAt, beer,
                review.calendar, review.user, total
        )
    }
}