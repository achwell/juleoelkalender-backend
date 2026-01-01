package no.juleoelkalender.repository

import no.juleoelkalender.entity.BeerEntity
import no.juleoelkalender.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface BeerRepository : JpaRepository<BeerEntity, UUID> {
    fun findBeerEntityByUser(user: UserEntity): Collection<BeerEntity>
}
