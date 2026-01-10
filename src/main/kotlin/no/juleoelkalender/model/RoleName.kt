package no.juleoelkalender.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "RoleName", enumAsRef = true)
enum class RoleName {
    ROLE_USER, ROLE_ADMIN, ROLE_MASTER
}
