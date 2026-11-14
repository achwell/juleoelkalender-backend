package no.juleoelkalender.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import java.util.UUID

@Schema(name = "Calendar", description = "En juleølkalender.")
data class Calendar(
    @field:Schema(description = "Identifikator for kalenderen.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    val id: UUID?,
    @field:NotNull var name: String,
    @field:Schema(description = "Året kalenderen gjelder for.", example = "2026")
    @field:NotNull var year: Int,
    @field:NotNull var published: Boolean, @field:NotNull var archived: Boolean,
    @field:NotNull var beerCalendars: Set<BeerCalendar>,
    @field:NotNull var calendarToken: CalendarToken
)
