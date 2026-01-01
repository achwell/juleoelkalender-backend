package no.juleoelkalender.model

import java.time.ZonedDateTime

data class DashboardData(
        val backendVersion: String, val backendBuildTime: ZonedDateTime,
        val cpuLoad: Double, val freeDiskSpace: Long,
        val diskUsage: Long, val freeMemory: Double,
        val processUptime: Double, val devices: Set<Device>,
        val dbStatus: String, val emailStatus: String,
        val backendStatus: String, val numberOfUsers: Int,
        val newestUser: UserWithoutChildren,
        val numberOfActiveUsers: Int,
        val newestActiveUser: UserWithoutChildren,
        val numberOfBeers: Int,
        val numberOfBeersToPlace: Int,
        val numberOfVacantCalendarDays: Int, val newestBeer: Beer?
)
