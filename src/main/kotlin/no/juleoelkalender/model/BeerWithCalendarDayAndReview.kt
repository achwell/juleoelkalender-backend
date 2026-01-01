package no.juleoelkalender.model

data class BeerWithCalendarDayAndReview(
        val beer: Beer, val calendar: Calendar,
        val brewer: UserWithoutChildren,
        val day: Int, val review: Review?
) 