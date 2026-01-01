package no.juleoelkalender.controller

import io.swagger.v3.oas.annotations.responses.ApiResponse
import no.juleoelkalender.exception.NotFoundException
import no.juleoelkalender.model.User
import no.juleoelkalender.service.LocalesService
import no.juleoelkalender.service.UserService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.net.URISyntaxException
import java.util.UUID

@RestController
@RequestMapping("/api/v1/user")
class UserController(private val userService: UserService, private val localesService: LocalesService) {
    @get:ApiResponse(description = "Hent alle brukere")
    @get:PreAuthorize("hasAuthority('user:read')")
    @get:GetMapping
    val users: ResponseEntity<Set<User>>
        get() = ResponseEntity.ok(userService.all)

    @GetMapping(path = ["/export/{locale}"])
    @PreAuthorize("hasAuthority('user:read')")
    @ApiResponse(description = "Eksporter alle brukere til Excel")
    fun getAllUsersExport(@PathVariable locale: String): ResponseEntity<ByteArray> {
        val fileName = localesService.getString(locale, "pages.users.excelexportfilename")
        return ResponseEntity.ok()
                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        )
                )
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$fileName\"")
                .body(userService.getUsersXlsx(locale))
    }

    @GetMapping(path = ["/{id}"])
    @PreAuthorize("hasAuthority('user:read')")
    @ApiResponse(description = "Hent en bruker basert på id")
    fun getUserById(@PathVariable id: UUID): ResponseEntity<User> {
        val user = userService.getById(id) ?: throw NotFoundException("User with id $id not found")
        return ResponseEntity.ok(user)
    }

    @GetMapping(path = ["/email/{email}"])
    @PreAuthorize("hasAuthority('user:read')")
    @ApiResponse(description = "Hent alle brukere basert på e-post")
    fun getUserByEmail(@PathVariable email: String): ResponseEntity<UserDetails> {
        return ResponseEntity.ok(userService.loadUserByUsername(email))
    }

    @PostMapping
    @PreAuthorize("hasAuthority('user:create')")
    @ApiResponse(description = "Opprett bruker")
    @Throws(URISyntaxException::class)
    fun createUser(@RequestBody user: User): ResponseEntity<User> {
        return ResponseEntity.created(URI("/login")).body(userService.create(user))
    }

    @PutMapping(path = ["/{id}"])
    @PreAuthorize("hasAuthority('user:update') or (#user.email != authentication.principal.username and hasAuthority('user:update_other'))")
    @ApiResponse(description = "Oppdater bruker")
    fun updateUser(@PathVariable id: UUID, @RequestBody user: User): ResponseEntity<User> {
        val updated = userService.update(id, user)
                ?: throw NotFoundException("User with id $id not found")
        return ResponseEntity.ok(updated)
    }

    @DeleteMapping(path = ["/{id}"])
    @PreAuthorize("hasAuthority('user:delete')")
    @ApiResponse(description = "Slett bruker")
    fun deleteUser(@PathVariable id: UUID): ResponseEntity<Boolean> {
        userService.delete(id)
        return ResponseEntity(HttpStatus.NO_CONTENT)
    }
}
