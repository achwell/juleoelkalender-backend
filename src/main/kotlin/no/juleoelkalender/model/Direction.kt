package no.juleoelkalender.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "Direction", description = "Retningen en kalenderplassering skal flyttes.", enumAsRef = true)
enum class Direction {
    UP, DOWN
}
