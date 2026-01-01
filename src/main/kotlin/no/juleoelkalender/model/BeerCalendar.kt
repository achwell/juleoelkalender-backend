package no.juleoelkalender.model

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class BeerCalendar(
        val id: UUID,
        val day: Int,
        @field:Schema(implementation = Beer::class)
        val beer: Beer,
        @field:Schema(implementation = Calendar::class)
        val calendar: Calendar
) 