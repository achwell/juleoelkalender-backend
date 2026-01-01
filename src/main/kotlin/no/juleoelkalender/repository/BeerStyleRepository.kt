package no.juleoelkalender.repository

import no.juleoelkalender.entity.BeerStyleEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface BeerStyleRepository : JpaRepository<BeerStyleEntity, UUID> {
    fun findBeerStyleEntitiesByNameIgnoreCase(name: String): BeerStyleEntity?
}
