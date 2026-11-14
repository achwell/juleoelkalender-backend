package no.juleoelkalender.repository

import jakarta.persistence.EntityManager
import jakarta.persistence.EntityManagerFactory
import no.juleoelkalender.entity.BeerEntity
import no.juleoelkalender.entity.CalendarEntity
import no.juleoelkalender.entity.CalendarTokenEntity
import no.juleoelkalender.entity.ReviewEntity
import no.juleoelkalender.entity.RoleEntity
import no.juleoelkalender.entity.RoleNameEntity
import no.juleoelkalender.entity.UserEntity
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import java.time.ZonedDateTime
import java.util.UUID

class ReviewRepositoryIntegrationTest {

    private lateinit var entityManager: EntityManager
    private lateinit var repository: ReviewRepository
    private lateinit var token: CalendarTokenEntity
    private lateinit var otherToken: CalendarTokenEntity
    private lateinit var beer: BeerEntity
    private lateinit var otherBeer: BeerEntity
    private lateinit var calendar: CalendarEntity
    private lateinit var secondCalendar: CalendarEntity
    private lateinit var otherCalendar: CalendarEntity
    private lateinit var reviewer: UserEntity
    private lateinit var otherReviewer: UserEntity
    private lateinit var reviews: List<ReviewEntity>
    private val now = ZonedDateTime.now()

    @BeforeEach
    fun insertReviews() {
        entityManager = entityManagerFactory.createEntityManager()
        entityManager.transaction.begin()
        repository = JpaRepositoryFactory(entityManager).getRepository(ReviewRepository::class.java)

        val role = persist(RoleEntity(UUID.randomUUID(), RoleNameEntity.ROLE_USER, mutableSetOf(), mutableSetOf()))
        reviewer = user("first@example.no", role)
        otherReviewer = user("second@example.no", role)
        token = token("first")
        otherToken = token("second")
        calendar = calendar("first", token)
        secondCalendar = calendar("second", token)
        otherCalendar = calendar("third", otherToken)
        beer = beer("first", reviewer)
        otherBeer = beer("second", reviewer)
        reviews = listOf(
            review(beer, calendar, reviewer),
            review(otherBeer, calendar, reviewer),
            review(beer, calendar, otherReviewer),
            review(beer, secondCalendar, reviewer),
            review(beer, otherCalendar, reviewer)
        )
        entityManager.flush()
        // Read persisted rows instead of relying on the fixture's in-memory entity graph.
        entityManager.clear()
    }

    @AfterEach
    fun rollback() {
        if (::entityManager.isInitialized) {
            entityManager.use { entityManager ->
                if (entityManager.transaction.isActive) entityManager.transaction.rollback()
            }
        }
    }

    @Test
    fun calendarTokenQueryIncludesAllItsCalendarsAndExcludesOtherTokens() {
        assertEquals(
            reviews.take(4).map { it.id }.toSet(),
            repository.findByCalendar_CalendarToken_Id(token.id).map { it.id }.toSet()
        )
        assertEquals(
            listOf(reviews.last().id),
            repository.findByCalendar_CalendarToken_Id(otherToken.id).map { it.id })
    }

    @Test
    fun unknownCalendarTokenReturnsNoReviews() {
        assertEquals(
            emptyList<ReviewEntity>(),
            repository.findByCalendar_CalendarToken_Id(UUID.randomUUID())
        )
    }

    @Test
    fun beerQueryIncludesAllCalendarsAndReviewersButExcludesOtherBeers() {
        assertEquals(
            reviews.filter { it.beer.id == beer.id }.map { it.id }.toSet(),
            repository.findByBeer_Id(beer.id).map { it.id }.toSet()
        )
        assertEquals(
            listOf(reviews[1].id),
            repository.findByBeer_Id(otherBeer.id).map { it.id })
    }

    @Test
    fun unknownBeerReturnsNoReviews() {
        assertEquals(emptyList<ReviewEntity>(), repository.findByBeer_Id(UUID.randomUUID()))
    }

    @Test
    fun exactQueryMatchesBeerCalendarAndReviewerTogether() {
        for (expected in reviews) {
            val actual = repository.findByBeerAndCalendarAndUser(
                entityManager.getReference(BeerEntity::class.java, expected.beer.id),
                entityManager.getReference(CalendarEntity::class.java, expected.calendar.id),
                entityManager.getReference(UserEntity::class.java, expected.user.id)
            )
            assertEquals(expected.id, actual?.id)
        }
    }

    @Test
    fun exactQueryReturnsNullForAnUnreviewedCombinationOfExistingEntities() {
        assertNull(
            repository.findByBeerAndCalendarAndUser(
                entityManager.getReference(BeerEntity::class.java, otherBeer.id),
                entityManager.getReference(CalendarEntity::class.java, secondCalendar.id),
                entityManager.getReference(UserEntity::class.java, otherReviewer.id)
            )
        )
    }

    private fun <T : Any> persist(entity: T): T {
        // Generated IDs must start as null. Several entities expose them as non-null UUIDs.
        entity.javaClass.getDeclaredField("id").apply { isAccessible = true }.set(entity, null)
        entityManager.persist(entity)
        return entity
    }

    private fun user(email: String, role: RoleEntity) = persist(
        UserEntity(
            UUID.randomUUID(), "Test", null, "Reviewer", email, "unused", null, role, false,
            mutableSetOf(), mutableSetOf(), mutableSetOf(), mutableSetOf(), null, now, now,
            null, null, null, null, false
        )
    )

    private fun token(name: String) = persist(
        CalendarTokenEntity(
            UUID.randomUUID(), name, name, true, mutableSetOf(), mutableSetOf(), now, now
        )
    )

    private fun calendar(name: String, token: CalendarTokenEntity) = persist(
        CalendarEntity(
            id = null, name = name, year = 2026, published = false, archived = false, beerCalendars = mutableSetOf(), calendarToken = token, reviews = mutableSetOf(), createdDate = now, updatedDate = now
        )
    )

    private fun beer(name: String, brewer: UserEntity) = persist(
        BeerEntity(
            id = UUID.randomUUID(), name = name, style = "Stout", description = null, abv = 5.0, ibu = 20.0, ebc = 50.0, recipe = null, untapped = null, brewedDate = now, bottleDate = now,
            archived = false, user = brewer, beerCalendars = mutableSetOf(), reviews = mutableSetOf(), createdDate = now, updatedDate = now, desiredDate = null
        )
    )

    private fun review(beer: BeerEntity, calendar: CalendarEntity, user: UserEntity) = persist(
        ReviewEntity(
            id = UUID.randomUUID(),
            ratingLabel = 3.0,
            ratingLooks = 3.0,
            ratingSmell = 3.0,
            ratingTaste = 3.0,
            ratingFeel = 3.0,
            ratingOverall = 3.0,
            comment = null,
            createdAt = now,
            updatedDate = now,
            beer = beer,
            calendar = calendar,
            user = user
        )
    )

    companion object {
        private lateinit var factoryBean: LocalContainerEntityManagerFactoryBean
        private lateinit var entityManagerFactory: EntityManagerFactory

        @BeforeAll
        @JvmStatic
        fun createDatabase() {
            factoryBean = LocalContainerEntityManagerFactoryBean().apply {
                dataSource = DriverManagerDataSource(
                    "jdbc:h2:mem:review-repository-${UUID.randomUUID()};DB_CLOSE_DELAY=-1;NON_KEYWORDS=USER", "sa", ""
                )
                setPackagesToScan("no.juleoelkalender.entity")
                jpaVendorAdapter = HibernateJpaVendorAdapter()
                setJpaPropertyMap(
                    mapOf(
                        "hibernate.hbm2ddl.auto" to "create-drop",
                        // RoleEntity has a @Length constraint on an enum; these tests exercise queries.
                        "jakarta.persistence.validation.mode" to "none"
                    )
                )
                afterPropertiesSet()
            }
            entityManagerFactory = requireNotNull(factoryBean.getObject())
        }

        @AfterAll
        @JvmStatic
        fun closeDatabase() {
            if (::factoryBean.isInitialized) factoryBean.destroy()
        }
    }
}
