package no.juleoelkalender.model

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.security.core.GrantedAuthority
import java.util.UUID

@Schema(name = "Role")
data class Role(
    var id: UUID,
    @field:Schema(implementation = RoleName::class) var name: RoleName,
    var authorities: Set<Authority>
) : GrantedAuthority {
    override fun getAuthority(): String {
        return name.name
    }
}
