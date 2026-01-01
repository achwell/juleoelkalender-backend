package no.juleoelkalender.model

data class ReviewData(
        val reviews: Set<Review>, val calendar: Calendar, val average: Review
)
