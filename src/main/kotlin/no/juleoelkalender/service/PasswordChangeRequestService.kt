package no.juleoelkalender.service

import no.juleoelkalender.model.PasswordChangeRequest
import java.time.ZonedDateTime
import java.util.UUID

interface PasswordChangeRequestService : BaseService<UUID, PasswordChangeRequest> {
    fun deleteExpiredBefore(cutoff: ZonedDateTime): Int
}