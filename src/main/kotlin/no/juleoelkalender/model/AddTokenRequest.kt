package no.juleoelkalender.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull

@Schema(name = "AddTokenRequest", description = "Forespørsel om å legge til et kalendertoken for en bruker.")
data class AddTokenRequest(@field:NotNull var email: String, @field:NotNull var token: String)