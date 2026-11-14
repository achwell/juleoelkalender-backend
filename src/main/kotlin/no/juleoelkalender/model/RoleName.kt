package no.juleoelkalender.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(name = "RoleName", description = "Navn på en brukerrolle.", enumAsRef = true)
enum class RoleName {
    ROLE_USER, ROLE_ADMIN, ROLE_MASTER
}
