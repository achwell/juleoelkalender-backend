package no.juleoelkalender.service

import io.mockk.every
import io.mockk.mockk
import no.juleoelkalender.getBeer
import no.juleoelkalender.getBeerWithCalendarAndDay
import no.juleoelkalender.getCalendar
import no.juleoelkalender.getUser
import no.juleoelkalender.model.Beer
import no.juleoelkalender.model.BeerWithCalendarAndDay
import no.juleoelkalender.model.Calendar
import no.juleoelkalender.model.User
import no.juleoelkalender.service.impl.DashboardServiceImpl
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.actuate.info.InfoEndpoint
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint
import org.springframework.boot.health.actuate.endpoint.IndicatedHealthDescriptor
import org.springframework.boot.health.application.DiskSpaceHealthIndicator
import org.springframework.boot.health.contributor.HealthIndicator
import org.springframework.boot.health.contributor.Status
import org.springframework.boot.micrometer.metrics.actuate.endpoint.MetricsEndpoint
import org.springframework.boot.micrometer.metrics.actuate.endpoint.MetricsEndpoint.MetricDescriptor
import org.springframework.util.unit.DataSize
import java.io.File
import java.time.ZonedDateTime

internal class DashboardServiceTest {
    private lateinit var testSubject: DashboardService

    private val healthIndicators = mockk<MutableSet<HealthIndicator>>()
    private val healthEndpoint = mockk<HealthEndpoint>()
    private val beerService = mockk<BeerService>()
    private val calendarService = mockk<CalendarService>()
    private val deviceService = mockk<DeviceService>()
    private val userService = mockk<UserService>()
    private val infoEndpoint = mockk<InfoEndpoint>()
    private val metricsEndpoint = mockk<MetricsEndpoint>()
    private val metricDescriptor = mockk<MetricDescriptor>()
    private val sample = mockk<MetricsEndpoint.Sample>()
    private val healthDescriptor = mockk<IndicatedHealthDescriptor>()

    private lateinit var beer: Beer
    private lateinit var beerWithCalendarAndDay: BeerWithCalendarAndDay
    private lateinit var calendar: Calendar
    private lateinit var user: User

    @BeforeEach
    fun setUp() {
        beer = getBeer()
        beerWithCalendarAndDay = getBeerWithCalendarAndDay()
        calendar = getCalendar()
        user = getUser()
        testSubject = DashboardServiceImpl(healthIndicators, healthEndpoint, beerService, calendarService, deviceService, userService, infoEndpoint, metricsEndpoint)
    }

    @Test
    fun testGetDashboardData() {
        val now = ZonedDateTime.now()
        val info: MutableMap<String, Any> = mutableMapOf()
        val build: MutableMap<String, Any> = mutableMapOf()
        build["version"] = "1"
        build["time"] = now.toInstant()
        info["build"] = build
        val diskSpaceHealthIndicator = DiskSpaceHealthIndicator(File("."), DataSize.ofBytes(1))
        val indicators: MutableCollection<HealthIndicator> = mutableSetOf(diskSpaceHealthIndicator)
        every { infoEndpoint.info() } returns info
        every { healthIndicators.filter(any()) } returns listOf(indicators.first())
        every { healthEndpoint.healthForPath(any()) } returns healthDescriptor
        every { healthEndpoint.health() } returns healthDescriptor
        every { healthDescriptor.status } returns Status.UP
        every { metricsEndpoint.metric(any(), any()) } returns metricDescriptor
        every { metricDescriptor.measurements } returns listOf(sample)
        every { sample.value } returns 1.0
        every { userService.all } returns setOf(user)
        every { beerService.all } returns setOf(beer)
        every { beerService.getBeersWithCalendar(null, null) } returns setOf(beerWithCalendarAndDay)
        every { calendarService.all } returns setOf(calendar)

        val dashboardData = testSubject.dashboardData
        assertAll(
                { assertNotNull(dashboardData) },
                { assertEquals("1", dashboardData.backendVersion) },
                { assertNotNull(dashboardData.backendBuildTime) },
                { assertEquals(now.year, dashboardData.backendBuildTime.year) }
        )
    }
}
