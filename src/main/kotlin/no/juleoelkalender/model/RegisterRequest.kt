package no.juleoelkalender.model

data class RegisterRequest(
        val calendarToken: String, val firstName: String,
        val middleName: String?, val lastName: String, val email: String,
        val password: String, val area: String?
)
