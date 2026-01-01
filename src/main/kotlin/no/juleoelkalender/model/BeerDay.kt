package no.juleoelkalender.model

data class BeerDay(
        val beer: Beer, val brewer: UserWithoutChildren, val day: Int
)
