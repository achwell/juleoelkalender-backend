package no.juleoelkalender.config

import no.juleoelkalender.service.PasswordChangeRequestService
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import java.time.ZonedDateTime

@Configuration
@EnableScheduling
class DatabaseConfig(private val passwordChangeRequestService: PasswordChangeRequestService) {

    @Scheduled(cron = "0 * * * * *")
    fun cleanPasswordRequest() {
        passwordChangeRequestService.all
                .filter { it.created.isBefore(ZonedDateTime.now().minusHours(2)) }
                .forEach { passwordChangeRequestService.delete(it.id) }
    }
}
