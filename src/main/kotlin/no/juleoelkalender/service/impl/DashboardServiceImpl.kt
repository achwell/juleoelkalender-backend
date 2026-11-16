package no.juleoelkalender.service.impl

import no.juleoelkalender.model.DashboardData
import no.juleoelkalender.service.BeerService
import no.juleoelkalender.service.CalendarService
import no.juleoelkalender.service.DashboardService
import no.juleoelkalender.service.DeviceService
import no.juleoelkalender.service.UserService
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
            val diskSpaceHealthIndicators = healthIndicators.filterIsInstance<DiskSpaceHealthIndicator>()
            if (diskSpaceHealthIndicators.isEmpty()) {
                throw IllegalStateException("Health check for disk space not activated")
            }
            val diskDetails = diskSpaceHealthIndicators.firstOrNull()?.health(true)?.details.orEmpty()
            val dbStatus = healthEndpoint.healthForPath("db")?.status?.code
            val emailStatus = healthEndpoint.healthForPath("email")?.status?.code
            val backendStatus = healthEndpoint.health().status.code
            val totalDisk = diskDetails["total"] as? Long
                ?: throw IllegalStateException("Disk space health check did not provide total disk space")
            val freeDisk = diskDetails["free"] as? Long
                ?: throw IllegalStateException("Disk space health check did not provide free disk space")

            val totalMemory = getMeasurement("jvm.memory.max")
            val usedMemory = getMeasurement("jvm.memory.used")
            val cpuLoad = getMeasurement("system.cpu.usage")
            val processUptime = getMeasurement("process.uptime")

            val newestUser = userService.getNewestUser()
            val newestActiveUser = userService.getNewestActiveUser()
            val year = LocalDate.now().year
            val placedBeers = beerService.countPlacedBeersForYear(year)
            val activeBeers = beerService.countActiveBeers()
            val newestBeer = beerService.getNewestActiveBeer()
            val activeCalendars = calendarService.countActiveCalendarsForYear(year)
            val devices = deviceService.all.toMutableSet()
            return DashboardData(
                backendVersion,
                checkNotNull(backendBuildTime) { "Build time is missing from application info" },
                cpuLoad,
                freeDisk / 1024 / 1024 / 1024,
                (freeDisk * 100) / totalDisk,
                (totalMemory - usedMemory) / 1024 / 1024 / 1024,
                processUptime,
                devices,
                checkNotNull(dbStatus) { "Database health status is unavailable" },
                checkNotNull(emailStatus) { "Email health status is unavailable" },
                backendStatus,
                userService.countUsers().toInt(),
                checkNotNull(newestUser?.userWithoutChildren) { "No users are available for the dashboard" },
                userService.countActiveUsers().toInt(),
                checkNotNull(newestActiveUser?.userWithoutChildren) { "No active users are available for the dashboard" },
                activeBeers.toInt(),
                activeBeers.toInt() - placedBeers.toInt(),
                (activeCalendars.toInt() * 24) - placedBeers.toInt(),
                newestBeer
            )
        }

    private fun getMeasurement(measurement: String): Double {
        val metricDescriptor = metricsEndpoint.metric(measurement, listOf())
        return metricDescriptor?.measurements?.first()?.value ?: 0.0
    }
}