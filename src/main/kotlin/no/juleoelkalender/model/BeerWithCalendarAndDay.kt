package no.juleoelkalender.model

data class BeerWithCalendarAndDay(
        val beer: Beer, val calendar: Calendar?,
        val brewer: UserWithoutChildren, val day: Int
)
