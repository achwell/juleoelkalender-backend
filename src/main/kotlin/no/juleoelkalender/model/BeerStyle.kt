package no.juleoelkalender.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import java.util.UUID

@Schema(name = "BeerStyle", description = "En ølstil.")
data class BeerStyle(
    @field:Schema(description = "Identifikator for ølstilen.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    val id: UUID?,
    @field:Schema(description = "Navn på ølstilen.", example = "Stout")
    @field:NotNull var name: String
)
