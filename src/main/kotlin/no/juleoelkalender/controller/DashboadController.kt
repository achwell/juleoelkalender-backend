package no.juleoelkalender.controller

import no.juleoelkalender.model.DashboardData
import no.juleoelkalender.model.Device
import no.juleoelkalender.service.DashboardService
import no.juleoelkalender.service.DeviceService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/v1/dashboard")
class DashboadController(
        private val dashboardService: DashboardService,
        private val deviceService: DeviceService
) {
    @get:PreAuthorize("hasAuthority('dashboard')")
    @get:GetMapping("/devices")
    val devices: ResponseEntity<Set<Device>>
        get() = ResponseEntity.ok(deviceService.all)

    @get:PreAuthorize("hasAuthority('dashboard')")
    @get:GetMapping("/dashboarddata")
    val dashboardData: ResponseEntity<DashboardData>
        get() = ResponseEntity.ok(dashboardService.dashboardData)
}
