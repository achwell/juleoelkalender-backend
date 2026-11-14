package no.juleoelkalender.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import java.util.UUID

@Schema(name = "Device", description = "En registrert enhet og tilhørende bruker.")
data class Device(
    @field:NotNull var id: UUID, @field:NotNull var mobileVendor: String, @field:NotNull var mobileModel: String,
    @field:NotNull var isMobile: Boolean, @field:NotNull var osName: String, @field:NotNull var osVersion: String,
    @field:NotNull var browserName: String, @field:NotNull var browserVersion: String,
    @field:NotNull var user: UserWithoutChildren
)
