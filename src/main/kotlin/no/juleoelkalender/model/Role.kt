package no.juleoelkalender.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import org.springframework.security.core.GrantedAuthority
import java.util.UUID

@Schema(name = "Role", description = "En brukerrolle med tilhørende tillatelser.")
data class Role(
    var id: UUID,
    @field:NotNull @field:Schema(implementation = RoleName::class) var name: RoleName,
    @field:NotNull @field:NotEmpty var authorities: Set<Authority>
) : GrantedAuthority {
    override fun getAuthority(): String {
        return name.name
    }
}
