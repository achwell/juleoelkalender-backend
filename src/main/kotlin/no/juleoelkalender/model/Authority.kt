package no.juleoelkalender.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import java.util.UUID

@Schema(name = "Authority", description = "En tillatelse som kan tilordnes en rolle.")
data class Authority(@field:NotNull var id: UUID, @field:NotNull var name: String)
