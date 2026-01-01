package no.juleoelkalender.model

import org.springframework.security.core.GrantedAuthority
import java.util.UUID

data class Role(var id: UUID, var name: RoleName, var authorities: Set<Authority>) : GrantedAuthority {
    override fun getAuthority(): String {
        return name.name
    }
}
