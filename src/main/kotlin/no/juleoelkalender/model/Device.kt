package no.juleoelkalender.model

import java.util.UUID

data class Device(
        val id: UUID, val mobileVendor: String, val mobileModel: String,
        val isMobile: Boolean, val osName: String, val osVersion: String,
        val browserName: String, val browserVersion: String,
        val user: UserWithoutChildren
)
