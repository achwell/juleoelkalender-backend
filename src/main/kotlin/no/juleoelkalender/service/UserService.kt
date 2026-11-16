package no.juleoelkalender.service

import no.juleoelkalender.model.User
import org.springframework.security.core.userdetails.UserDetailsService
import java.util.UUID

interface UserService : UserDetailsService, BaseService<UUID, User> {
    fun updatePassword(email: String, password: String): User
    fun getUsersXlsx(locale: String): ByteArray
    fun countUsers(): Long
    fun countActiveUsers(): Long
    fun getNewestUser(): User?
    fun getNewestActiveUser(): User?
}