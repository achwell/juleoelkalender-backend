package no.juleoelkalender.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import java.time.ZonedDateTime

@Schema(name = "DashboardData", description = "Status og nøkkeltall for administrasjonspanelet.")
data class DashboardData(
    @field:NotNull var backendVersion: String, @field:NotNull var backendBuildTime: ZonedDateTime,
    @field:NotNull var cpuLoad: Double, @field:NotNull var freeDiskSpace: Long,
    @field:NotNull var diskUsage: Long, @field:NotNull var freeMemory: Double,
    @field:NotNull var processUptime: Double, @field:NotNull var devices: Set<Device>,
    @field:NotNull var dbStatus: String, @field:NotNull var emailStatus: String,
    @field:NotNull var backendStatus: String, @field:NotNull var numberOfUsers: Int,
    @field:NotNull var newestUser: UserWithoutChildren,
    @field:NotNull var numberOfActiveUsers: Int,
    @field:NotNull var newestActiveUser: UserWithoutChildren,
    @field:NotNull var numberOfBeers: Int,
    @field:NotNull var numberOfBeersToPlace: Int,
    @field:NotNull var numberOfVacantCalendarDays: Int,
    @field:Schema(description = "Det sist opprettede ølet, hvis det finnes.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    val newestBeer: Beer?
)
