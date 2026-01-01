package no.juleoelkalender.service.impl

import jakarta.mail.MessagingException
import no.juleoelkalender.config.MailProperties
import no.juleoelkalender.entity.PasswordChangeRequestEntity
import no.juleoelkalender.mappers.PasswordChangeRequestMapper
import no.juleoelkalender.model.PasswordChangeRequest
import no.juleoelkalender.repository.PasswordChangeRequestRepository
import no.juleoelkalender.service.EmailService
import no.juleoelkalender.service.PasswordChangeRequestService
import no.juleoelkalender.utils.ResourceReader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class PasswordChangeRequestServiceImpl(
        repository: PasswordChangeRequestRepository,
        private val emailService: EmailService, mapper: PasswordChangeRequestMapper, private val mailProperties: MailProperties,
        @param:Value("classpath:emails/forgottenPassword.html") private val forgottenPasswordEmail: Resource
) : BaseServiceImpl<UUID, PasswordChangeRequest, PasswordChangeRequestEntity>(repository, mapper), PasswordChangeRequestService {

    override fun preCreate(model: PasswordChangeRequest): PasswordChangeRequestEntity {
        val pcr = PasswordChangeRequest(
                model.id,
                model.token, model.email, ZonedDateTime.now()
        )

        val expires = pcr.created.plusHours(1)
        val mailContent = ResourceReader.asString(forgottenPasswordEmail)
                .replace($$"${base_url}", mailProperties.baseUrl!!)
                .replace($$"${email}", pcr.email)
                .replace($$"${token}", pcr.token)
                .replace($$"${expires}", DateTimeFormatter.ofPattern("dd.MM.yyyy HH:SS").format(expires))
        try {
            emailService.sendSimpleMessage(
                    mailProperties.from!!, pcr.email,
                    "Glemt passord på Juleølkalender", mailContent
            )
        } catch (e: MessagingException) {
            log.error("Error sending mail", e)
        }
        return mapper.modelToEntity(pcr)
    }

    override fun mapModelToEntity(model: PasswordChangeRequest, entity: PasswordChangeRequestEntity) {
        entity.token = model.token
        entity.email = model.email
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(PasswordChangeRequestServiceImpl::class.java)
    }
}