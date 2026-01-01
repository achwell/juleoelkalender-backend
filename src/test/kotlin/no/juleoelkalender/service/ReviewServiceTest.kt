package no.juleoelkalender.service

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import no.juleoelkalender.*
import no.juleoelkalender.entity.BeerEntity
import no.juleoelkalender.entity.CalendarEntity
import no.juleoelkalender.entity.ReviewEntity
import no.juleoelkalender.entity.UserEntity
import no.juleoelkalender.mappers.*
import no.juleoelkalender.model.Beer
import no.juleoelkalender.model.Calendar
import no.juleoelkalender.model.Review
import no.juleoelkalender.model.UserWithoutChildren
import no.juleoelkalender.repository.BeerRepository
import no.juleoelkalender.repository.CalendarRepository
import no.juleoelkalender.repository.ReviewRepository
import no.juleoelkalender.repository.UserRepository
import no.juleoelkalender.service.impl.ReviewServiceImpl
import no.juleoelkalender.utils.ExcelGenerator
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertThrows
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.time.ZonedDateTime
import java.util.Optional
import java.util.UUID

internal class ReviewServiceTest {
    private val calendarTokenMapper = CalendarTokenMapper()
    private val userWithoutChildrenMapper = UserWithoutChildrenMapper(calendarTokenMapper, RoleMapper(AuthorityMapper()))
    private val beerMapper = BeerMapper(userWithoutChildrenMapper)
    private val calendarMapper = CalendarMapper(beerMapper, calendarTokenMapper)
    private val reviewMapper = ReviewMapper(beerMapper, calendarMapper, userWithoutChildrenMapper)

    private val reviewRepository = mockk<ReviewRepository>()
    private val userRepository = mockk<UserRepository>()
    private val beerRepository = mockk<BeerRepository>()
    private val calendarRepository = mockk<CalendarRepository>()
    private val localesService = mockk<LocalesService>()

    private lateinit var testSubject: ReviewService
    private lateinit var beer: Beer
    private lateinit var beerEntity: BeerEntity
    private lateinit var calendar: Calendar
    private lateinit var calendarEntity: CalendarEntity
    private lateinit var reviewEntity: ReviewEntity
    private lateinit var userEntity: UserEntity
    private lateinit var userWithoutChildren: UserWithoutChildren

    @BeforeEach
    fun setUp() {
        beer = getBeer()
        beerEntity = getBeerEntity()
        calendar = getCalendar()
        calendarEntity = getCalendarEntity()
        reviewEntity = getReviewEntity()
        userEntity = getUserEntity()
        userWithoutChildren = getUserWithoutChildren()
        testSubject = ReviewServiceImpl(
                reviewRepository,
                userRepository,
                beerRepository,
                calendarRepository,
                beerMapper,
                calendarMapper,
                reviewMapper,
                userWithoutChildrenMapper,
                ExcelGenerator(),
                localesService
        )
    }

    @Test
    fun testGetReviewsWithUser() {
        val authentication = mockk<Authentication>()
        val securityContext = mockk<SecurityContext>()
        every { securityContext.authentication } returns authentication
        SecurityContextHolder.setContext(securityContext)
        every { SecurityContextHolder.getContext().authentication?.principal } returns userEntity.email
        every { userRepository.findByEmailIgnoreCase(any()) } returns userEntity
        every { reviewRepository.findAll() } returns listOf(reviewEntity)

        val reviews = testSubject.reviewsWithUser

        assertAll(
                { assertNotNull(reviews) },
                { Assertions.assertEquals(1, reviews.size) }
        )
    }

    @Test
    fun testGetReviewsWithUserUserNotExist() {
        val authentication = mockk<Authentication>()
        val securityContext = mockk<SecurityContext>()
        every { securityContext.authentication } returns authentication
        SecurityContextHolder.setContext(securityContext)
        every { SecurityContextHolder.getContext().authentication?.principal } returns userEntity.email
        every { userRepository.findByEmailIgnoreCase(any()) } returns null
        every { reviewRepository.findAll() } returns listOf(reviewEntity)

        val reviews = testSubject.reviewsWithUser
        assertAll(
                { assertNotNull(reviews) },
                { Assertions.assertEquals(0, reviews.size) }
        )
    }

    @Test
    fun testGetReviewsWithUserExistingReview() {
        val authentication = mockk<Authentication>()
        val securityContext = mockk<SecurityContext>()
        every { securityContext.authentication } returns authentication
        SecurityContextHolder.setContext(securityContext)
        every { SecurityContextHolder.getContext().authentication?.principal } returns userEntity.email
        every { userRepository.findByEmailIgnoreCase(any()) } returns userEntity
        every { reviewRepository.findAll() } returns listOf(reviewEntity, reviewEntity)

        val reviews = testSubject.reviewsWithUser
        assertAll(
                { assertNotNull(reviews) },
                { Assertions.assertEquals(1, reviews.size) }
        )
    }

    @Test
    fun testGetReviewById() {
        every { reviewRepository.findById(any()) } returns Optional.of(reviewEntity)
        val review = testSubject.getById(reviewEntity.id)
        assertAll(
                { assertNotNull(review) },
                { Assertions.assertEquals(reviewEntity.id, review?.id) }
        )
    }

    @Test
    fun testGetReviewByCalendarBeerAndReviewerExist() {
        every { beerRepository.getReferenceById(any()) } returns beerEntity
        every { calendarRepository.getReferenceById(any()) } returns calendarEntity
        every { userRepository.getReferenceById(any()) } returns userEntity
        every { reviewRepository.findByBeerAndCalendarAndUser(any(), any(), any()) } returns reviewEntity

        val review = testSubject.getReviewByCalendarBeerAndReviewer(
                calendarEntity.id!!,
                beerEntity.id,
                userEntity.id!!
        )

        assertAll(
                { assertNotNull(review) },
                { assertNotNull(review.beer) },
                { assertNotNull(review.calendar) },
                { assertNotNull(review.user) },
                { Assertions.assertEquals(reviewEntity.ratingLabel, review.ratingLabel) },
                { Assertions.assertEquals(reviewEntity.ratingLooks, review.ratingLooks) },
                { Assertions.assertEquals(reviewEntity.ratingSmell, review.ratingSmell) },
                { Assertions.assertEquals(reviewEntity.ratingTaste, review.ratingTaste) },
                { Assertions.assertEquals(reviewEntity.ratingFeel, review.ratingFeel) },
                { Assertions.assertEquals(reviewEntity.ratingOverall, review.ratingOverall) },
                { Assertions.assertEquals(reviewEntity.comment, review.comment) },
                { Assertions.assertEquals(reviewEntity.createdAt, review.createdAt) }
        )
    }

    @Test
    fun testGetReviewByCalendarBeerAndReviewerDontExist() {
        every { beerRepository.getReferenceById(any()) } returns beerEntity
        every { calendarRepository.getReferenceById(any()) } returns calendarEntity
        every { userRepository.getReferenceById(any()) } returns userEntity
        every { reviewRepository.findByBeerAndCalendarAndUser(any(), any(), any()) } returns null

        val review = testSubject.getReviewByCalendarBeerAndReviewer(
                calendarEntity.id!!,
                beerEntity.id,
                userEntity.id!!
        )

        assertAll(
                { assertNotNull(review) },
                { assertNotNull(review.beer) },
                { assertNotNull(review.calendar) },
                { assertNotNull(review.user) },
                { Assertions.assertEquals(0.0, review.ratingLabel) },
                { Assertions.assertEquals(0.0, review.ratingLooks) },
                { Assertions.assertEquals(0.0, review.ratingSmell) },
                { Assertions.assertEquals(0.0, review.ratingTaste) },
                { Assertions.assertEquals(0.0, review.ratingFeel) },
                { Assertions.assertEquals(0.0, review.ratingOverall) },
                { Assertions.assertEquals("", review.comment) }
        )
    }

    @Test
    fun testCreateReview() {
        val authentication = mockk<Authentication>()
        val securityContext = mockk<SecurityContext>()
        every { securityContext.authentication } returns authentication
        SecurityContextHolder.setContext(securityContext)
        every { SecurityContextHolder.getContext().authentication?.principal } returns userEntity.email
        every { userRepository.findByEmailIgnoreCase(any()) } returns userEntity
        every { reviewRepository.save(any()) } returns reviewEntity

        val review = testSubject.create(reviewMapper.entityToModel(reviewEntity))

        assertAll(
                { assertNotNull(review) },
                { Assertions.assertEquals(reviewEntity.id, review.id) },
                { assertNotNull(review.user) }
        )
    }

    @Test
    fun testDeleteReviewExist() {
        every { reviewRepository.existsById(any()) } returns true
        every { beerRepository.findById(any()) } returns Optional.of(beerEntity)
        val authentication = mockk<Authentication>()
        val securityContext = mockk<SecurityContext>()
        every { securityContext.authentication } returns authentication
        SecurityContextHolder.setContext(securityContext)
        every { SecurityContextHolder.getContext().authentication?.principal } returns userEntity.email
        every { reviewRepository.deleteById(any()) } just Runs
        val deleted = testSubject.delete(reviewEntity.id)
        Assertions.assertTrue(deleted)
    }

    @Test
    fun testDeleteReviewExistWrongAuthority() {
        every { reviewRepository.existsById(any()) } returns true
        every { beerRepository.findById(any()) } returns Optional.of(beerEntity)
        val authentication = mockk<Authentication>()
        val securityContext = mockk<SecurityContext>()
        every { securityContext.authentication } returns authentication
        SecurityContextHolder.setContext(securityContext)
        every { SecurityContextHolder.getContext().authentication?.authorities } returns listOf(SimpleGrantedAuthority("Feil Rolle"))
        val thrown = assertThrows(InsufficientAuthenticationException::class.java) {
            testSubject.delete(
                    reviewEntity.id
            )
        }
        assertAll(
                { assertNotNull(thrown) },
                { Assertions.assertEquals("Ikke lov til å slette andres tilbakemeldinger", thrown.message) }
        )
    }

    @Test
    fun testDeleteReviewExistNoAuthorities() {
        every { reviewRepository.existsById(any()) } returns true
        every { beerRepository.findById(any()) } returns Optional.of(beerEntity)
        val authentication = mockk<Authentication>()
        val securityContext = mockk<SecurityContext>()
        every { securityContext.authentication } returns authentication
        SecurityContextHolder.setContext(securityContext)
        every { SecurityContextHolder.getContext().authentication?.authorities } returns listOf()
        val thrown = assertThrows(InsufficientAuthenticationException::class.java) {
            testSubject.delete(
                    reviewEntity.id
            )
        }
        assertAll(
                { assertNotNull(thrown) },
                { Assertions.assertEquals("Ikke lov til å slette andres tilbakemeldinger", thrown.message) }
        )
    }

    @Test
    fun testDeleteReviewDontExist() {
        every { reviewRepository.existsById(any()) } returns false
        val deleted = testSubject.delete(reviewEntity.id)
        Assertions.assertFalse(deleted)
    }

    @Test
    fun getReviewDataByBeerId() {
        every { beerRepository.findById(any()) } returns Optional.of(beerEntity)
        every { reviewRepository.findAll() } returns listOf(reviewEntity)
        every { calendarRepository.findById(any()) } returns Optional.of(calendarEntity)

        val reviewData = testSubject.getReviewDataByBeerId(beerEntity.id)

        assertAll(
                { assertNotNull(reviewData) },
                { Assertions.assertEquals(1, reviewData.size) }
        )
        val data = reviewData.first()
        assertAll(
                { assertNotNull(data) },
                { Assertions.assertEquals(1, data.reviews.size) },
                { assertNotNull(data.average) },
                { assertNotNull(data.calendar) }
        )
    }

    @Test
    fun testCalculateAverage() {
        val review1 = Review(UUID.randomUUID(), 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, null, ZonedDateTime.now(), beer, calendar, userWithoutChildren)
        val review2 = Review(UUID.randomUUID(), 3.0, 3.0, 2.0, 2.0, 3.0, 5.0, null, ZonedDateTime.now(), beer, calendar, userWithoutChildren)
        val reviews = listOf(review1, review2)
        val review = testSubject.calculateAverage(reviews, calendar, beer, userWithoutChildren)

        assertAll(
                { assertNotNull(review) },
                { Assertions.assertEquals(2.0, review.ratingFeel) },
                { Assertions.assertEquals(2.0, review.ratingLabel) },
                { Assertions.assertEquals(2.0, review.ratingLooks) },
                { Assertions.assertEquals(3.0, review.ratingOverall) },
                { Assertions.assertEquals(1.5, review.ratingSmell) },
                { Assertions.assertEquals(1.5, review.ratingTaste) }
        )
    }

    @Test
    fun testCalculateAverageEmptyInput() {
        val review = testSubject.calculateAverage(mutableListOf(), calendar, beer, userWithoutChildren)

        assertAll(
                { assertNotNull(review) },
                { Assertions.assertEquals(0.0, review.ratingFeel) },
                { Assertions.assertEquals(0.0, review.ratingLooks) },
                { Assertions.assertEquals(0.0, review.ratingLabel) },
                { Assertions.assertEquals(0.0, review.ratingOverall) },
                { Assertions.assertEquals(0.0, review.ratingSmell) },
                { Assertions.assertEquals(0.0, review.ratingTaste) }
        )
    }

    @Test
    fun testUpdate() {
        every { reviewRepository.findById(any()) } returns Optional.of(reviewEntity)
        every { reviewRepository.save(any()) } returns reviewEntity
        val updated = testSubject.update(reviewEntity.id, reviewMapper.entityToModel(reviewEntity))
        assertNotNull(updated)
    }

    @Test
    fun testGetReviewsXlsx() {
        every { localesService.getString(any(), any()) } returns "text"
        val authentication = mockk<Authentication>()
        val securityContext = mockk<SecurityContext>()
        every { securityContext.authentication } returns authentication
        SecurityContextHolder.setContext(securityContext)
        every { SecurityContextHolder.getContext().authentication?.principal } returns userEntity.email
        every { userRepository.findByEmailIgnoreCase(any()) } returns userEntity
        every { reviewRepository.findAll() } returns listOf(reviewEntity)
        val reviewsXlsx = testSubject.getReviewsXlsx("no")
        assertNotNull(reviewsXlsx)
    }
}