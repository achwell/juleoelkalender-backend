package no.juleoelkalender.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "mail.welcome")
class MailProperties {
    var baseUrl: String? = null
    var from: String? = null
    var supportEmail: String? = null
}
