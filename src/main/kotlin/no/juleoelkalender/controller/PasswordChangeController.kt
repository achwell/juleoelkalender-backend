package no.juleoelkalender.controller

import no.juleoelkalender.exception.NotFoundException
import no.juleoelkalender.model.PasswordChangeRequest
import no.juleoelkalender.model.User
import no.juleoelkalender.service.PasswordChangeRequestService
import no.juleoelkalender.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.net.URISyntaxException
import java.util.UUID

@RestController
@RequestMapping("/api/v1/passwordchange")
class PasswordChangeController(
        private val passwordChangeRequestService: PasswordChangeRequestService,
        private val userService: UserService
) {
    @get:PreAuthorize("isAnonymous()")
    @get:GetMapping
    val passwordChangeRequests: ResponseEntity<Set<PasswordChangeRequest>>
        get() = ResponseEntity.ok(passwordChangeRequestService.all)

    @GetMapping(path = ["/{id}"])
    @PreAuthorize("isAnonymous()")
    fun getPasswordChangeRequestById(@PathVariable id: UUID): ResponseEntity<PasswordChangeRequest> {
        val passwordChangeRequest = passwordChangeRequestService.getById(id)
                ?: throw NotFoundException("Password change request with id $id not found")
        return ResponseEntity.ok(passwordChangeRequest)
    }

    @PostMapping
    @PreAuthorize("isAnonymous()")
    @Throws(URISyntaxException::class)
    fun createPasswordChangeRequest(
            @RequestBody passwordChangeRequest: PasswordChangeRequest
    ): ResponseEntity<PasswordChangeRequest> {
        return ResponseEntity.created(URI("/changepassword")).body(passwordChangeRequestService.create(passwordChangeRequest))
    }

    @PutMapping(path = ["/{id}"])
    @PreAuthorize("isAnonymous()")
    fun updatePasswordChangeRequest(
            @PathVariable id: UUID,
            @RequestBody passwordChangeRequest: PasswordChangeRequest
    ): ResponseEntity<PasswordChangeRequest> {
        val updated = passwordChangeRequestService.update(id, passwordChangeRequest)
                ?: throw NotFoundException("Password change request with id $id not found")
        return ResponseEntity.ok(updated)
    }

    @PutMapping(path = ["/{email}/{password}"])
    @PreAuthorize("isAnonymous()")
    fun updatePassword(
            @PathVariable email: String,
            @PathVariable password: String
    ): ResponseEntity<User> {
        return ResponseEntity.ok(userService.updatePassword(email, password))
    }

    @DeleteMapping(path = ["/{id}"])
    @PreAuthorize("isAnonymous()")
    fun deletePasswordChangeRequest(@PathVariable id: UUID): ResponseEntity<Boolean> {
        passwordChangeRequestService.delete(id)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}
