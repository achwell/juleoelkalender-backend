package no.juleoelkalender.repository

import no.juleoelkalender.entity.RoleEntity
import no.juleoelkalender.entity.RoleNameEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface RoleRepository : JpaRepository<RoleEntity, UUID> {
    fun findRoleEntityByName(name: RoleNameEntity): RoleEntity?
}
