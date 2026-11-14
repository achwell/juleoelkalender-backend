package no.juleoelkalender.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.time.ZonedDateTime
import java.util.UUID

@Schema(name = "Beer", description = "Et øl og informasjon om brygging, brygger og anmeldelser.")
data class Beer(
    @field:NotNull var id: UUID,
    @field:NotNull var name: String,
    @field:NotNull var style: String,
    @field:Schema(description = "Beskrivelse av ølet.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    val description: String?,
    @field:Schema(description = "Alkoholinnhold i volumprosent.", example = "5.5")
    @field:NotNull var abv: Double,
    @field:Schema(description = "Ølets bitterhet målt i IBU.", example = "30")
    @field:NotNull var ibu: Double,
    @field:Schema(description = "Ølets farge målt i EBC.", example = "20")
    @field:NotNull var ebc: Double,
    @field:Schema(description = "Oppskrift på ølet.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    val recipe: String?,
    @field:Schema(description = "Referanse til ølet på Untappd.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    val untapped: String?,
    @field:NotNull var brewedDate: ZonedDateTime,
    @field:NotNull var bottleDate: ZonedDateTime,
    @field:NotNull var archived: Boolean,
    @field:NotNull var brewer: UserWithoutChildren,
    @field:NotNull var reviews: Set<ReviewWithoutChildren>,
    @field:NotNull var createdDate: ZonedDateTime,
    @field:Schema(description = "Ønsket kalenderdag fra 1 til 24.", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "24")
    val desiredDate: @Min(1) @Max(24) Int?
)
