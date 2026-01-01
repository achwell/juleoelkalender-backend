package no.juleoelkalender.model

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import java.time.ZonedDateTime
import java.util.UUID

data class Beer(
        val id: UUID, val name: String, val style: String,
        val description: String?, val abv: Double, val ibu: Double,
        val ebc: Double, val recipe: String?, val untapped: String?,
        val brewedDate: ZonedDateTime, val bottleDate: ZonedDateTime,
        val archived: Boolean, val brewer: UserWithoutChildren,
        val reviews: Set<ReviewWithoutChildren>,
        val createdDate: ZonedDateTime, val desiredDate: @Min(1) @Max(24) Int?
)

