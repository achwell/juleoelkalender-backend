package no.juleoelkalender.service.impl

import no.juleoelkalender.model.DashboardData
import no.juleoelkalender.service.*
import org.springframework.boot.actuate.info.InfoEndpoint
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint
import org.springframework.boot.health.application.DiskSpaceHealthIndicator
import org.springframework.boot.health.contributor.HealthIndicator
import org.springframework.boot.micrometer.metrics.actuate.endpoint.MetricsEndpoint
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

@Service
class DashboardServiceImpl(
        private val healthIndicators: MutableSet<HealthIndicator>,
        private val healthEndpoint: HealthEndpoint, private val beerService: BeerService,
        private val calendarService: CalendarService, private val deviceService: DeviceService,
        private val userService: UserService, private val infoEndpoint: InfoEndpoint,
        private val metricsEndpoint: MetricsEndpoint
) : DashboardService {

    override val dashboardData: DashboardData
        get() {
            var backendVersion = ""
            var backendBuildTime: ZonedDateTime? = null
            val info = infoEndpoint.info()
            if (info.containsKey("build")) {
                val build = info["build"] as MutableMap<*, *>
                if (build.containsKey("version")) {
                    backendVersion = build["version"] as String
                }
                if (build.containsKey("time")) {
                    backendBuildTime = (build["time"] as Instant).atZone(ZoneId.systemDefault())
                }
            }
            val diskSpaceHealthIndicators = healthIndicators.filter { DiskSpaceHealthIndicator::class.java.isInstance(it) }
            if (diskSpaceHealthIndicators.isEmpty()) {
                RuntimeException("Health check for disk space not activated")
            }
            val diskDetails = diskSpaceHealthIndicators.firstOrNull()?.health(true)?.details.orEmpty()
            val dbStatus = healthEndpoint.healthForPath("db")?.status?.code
            val emailStatus = healthEndpoint.healthForPath("email")?.status?.code
            val backendStatus = healthEndpoint.health().status.code
            val totalDisk = diskDetails["total"] as Long
            val freeDisk = diskDetails["free"] as Long

            val totalMemory = getGetMeasurement("jvm.memory.max")
            val usedMemory = getGetMeasurement("jvm.memory.used")
            val cpuLoad = getGetMeasurement("system.cpu.usage")
            val processUptime = getGetMeasurement("process.uptime")

            val users = userService.all
            val newestUser = users.maxByOrNull { it.createdDate }
            val activeUsers = users.filter { user -> !user.calendarToken.none { it.active } }
            val newestActiveUser = activeUsers.maxByOrNull { it.createdDate }
            val beers = beerService.all.filter { !it.archived }
            val year = LocalDate.now().year
            val placedBeers = beerService.getBeersWithCalendar(null, null)
                    .filter { it.calendar?.year == year }
            val newestBeer = beers.maxByOrNull { it.createdDate }
            val activeCalendars = calendarService.all.filter { !it.archived && it.year == year }
            val devices = deviceService.all.toMutableSet()
            return DashboardData(
                    backendVersion,
                    backendBuildTime!!,
                    cpuLoad,
                    freeDisk / 1024 / 1024 / 1024,
                    (freeDisk * 100) / totalDisk,
                    (totalMemory - usedMemory) / 1024 / 1024 / 1024,
                    processUptime,
                    devices,
                    dbStatus!!,
                    emailStatus!!,
                    backendStatus,
                    users.size,
                    newestUser?.userWithoutChildren!!,
                    activeUsers.size,
                    newestActiveUser?.userWithoutChildren!!,
                    beers.size,
                    beers.size - placedBeers.size,
                    (activeCalendars.size * 24) - placedBeers.size,
                    newestBeer!!
            )
        }

    private fun getGetMeasurement(measurement: String): Double {
        val metricDescriptor = metricsEndpoint.metric(measurement, listOf())
        return metricDescriptor?.measurements?.first()?.value ?: 0.0
    }
}