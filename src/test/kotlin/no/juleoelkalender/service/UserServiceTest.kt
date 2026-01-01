package no.juleoelkalender.service

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import no.juleoelkalender.entity.RoleEntity
import no.juleoelkalender.entity.UserEntity
import no.juleoelkalender.getRoleEntityUser
import no.juleoelkalender.getUser
import no.juleoelkalender.getUserEntity
import no.juleoelkalender.mappers.*
import no.juleoelkalender.model.User
import no.juleoelkalender.repository.CalendarTokenRepository
import no.juleoelkalender.repository.DeviceRepository
import no.juleoelkalender.repository.RoleRepository
import no.juleoelkalender.repository.UserRepository
import no.juleoelkalender.service.impl.UserServiceImpl
import no.juleoelkalender.utils.ExcelGenerator
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.Locale
import java.util.Optional

internal class UserServiceTest {
    private val userRepository = mockk<UserRepository>()
    private val calendarTokenRepository = mockk<CalendarTokenRepository>()
    private val roleRepository = mockk<RoleRepository>()
    private val localesService = mockk<LocalesService>()
    private val deviceRepository = mockk<DeviceRepository>()

    private val roleMapper = RoleMapper(AuthorityMapper())
    private val calendarTokenMapper = CalendarTokenMapper()
    private val userMapper = UserMapper(BeerMapper(UserWithoutChildrenMapper(calendarTokenMapper, roleMapper)), calendarTokenMapper, roleMapper)
    private val passwordEncoder: PasswordEncoder = BCryptPasswordEncoder()

    private lateinit var testSubject: UserService
    private lateinit var roleEntityUser: RoleEntity
    private lateinit var user: User
    private lateinit var userEntity: UserEntity

    @BeforeEach
    fun setUp() {
        roleEntityUser = getRoleEntityUser()
        user = getUser()
        userEntity = getUserEntity()
        testSubject = UserServiceImpl(userRepository, calendarTokenRepository, roleRepository, userMapper, passwordEncoder, ExcelGenerator(), localesService, deviceRepository)
    }

    @Test
    fun testGetUsers() {
        every { userRepository.findAll() } returns listOf(userEntity, userEntity, userEntity)

        val users = testSubject.all
        Assertions.assertAll(
                { Assertions.assertNotNull(users) },
                { Assertions.assertEquals(3, users.size) }
        )
    }

    @Test
    fun testGetUserById() {
        every { userRepository.findById(any()) } returns Optional.of(userEntity)
        val user = testSubject.getById(userEntity.id!!)
        Assertions.assertAll(
                { Assertions.assertNotNull(user) },
                { Assertions.assertEquals(userEntity.id, user?.id) }
        )
    }

    @Test
    fun testGetUserByEmail() {
        every { userRepository.findByEmailIgnoreCase(any()) } returns userEntity
        val user = testSubject.loadUserByUsername(userEntity.email.uppercase(Locale.getDefault()))
        Assertions.assertAll(
                { Assertions.assertNotNull(user) },
                { Assertions.assertEquals(userEntity.email, user.username) }
        )
    }

    @Test
    fun testGetUserByEmailNotFound() {
        every { userRepository.findByEmailIgnoreCase(any()) } returns null
        Assertions.assertThrows(UsernameNotFoundException::class.java) { testSubject.loadUserByUsername(userEntity.email) }
    }

    @Test
    fun testCreateUserOK() {
        every { userRepository.findByEmailIgnoreCase(any()) } returns null
        every { roleRepository.findRoleEntityByName(any()) } returns roleEntityUser
        every { userRepository.save(any()) } returns userEntity
        val user = testSubject.create(userMapper.entityToModel(userEntity))
        Assertions.assertAll(
                { Assertions.assertNotNull(user) },
                { Assertions.assertEquals(userEntity.email, user.email) },
                { Assertions.assertNotNull(user.pwd) }
        )
    }

    @Test
    fun testCreateUserUserExist() {
        every { userRepository.findByEmailIgnoreCase(any()) } returns userEntity
        Assertions.assertThrows(RuntimeException::class.java) { testSubject.create(user) }
    }

    @Test
    fun testUpdateUser() {
        every { userRepository.findById(any()) } returns Optional.of(userEntity)
        every { roleRepository.findRoleEntityByName(any()) } returns roleEntityUser
        every { userRepository.save(any()) } returns userEntity
        val user = userMapper.entityToModel(userEntity)
        user.pwd = "new password"
        val result = testSubject.update(userEntity.id!!, user)
        Assertions.assertNotNull(result)
    }

    @Test
    fun testUpdatePassword() {
        every { userRepository.findByEmailIgnoreCase(any()) } returns userEntity
        every { userRepository.save(any()) } returns userEntity
        val user = testSubject.updatePassword(userEntity.email, userEntity.password)
        Assertions.assertAll(
                { Assertions.assertNotNull(user) },
                { Assertions.assertEquals(userEntity.email, user.email) },
                { Assertions.assertNotNull(user.pwd) }
        )
    }


    @Test
    fun testDeleteUserExist() {
        every { deviceRepository.deleteByUserId(any()) } just Runs
        every { userRepository.existsById(any()) } returns true
        every { userRepository.deleteById(any()) } just Runs
        val deleted = testSubject.delete(userEntity.id!!)
        Assertions.assertTrue(deleted)
    }

    @Test
    fun testDeleteUserDontExist() {
        every { userRepository.existsById(any()) } returns false
        val deleted = testSubject.delete(userEntity.id!!)
        Assertions.assertFalse(deleted)
    }

    @Test
    fun testGetUsersXlsx() {
        val locale = "no"
        every { localesService.getString(locale, "pages.users.name") } returns "name"
        every { localesService.getString(locale, "pages.users.email") } returns "email"
        every { localesService.getString(locale, "pages.users.area") } returns "area"
        every { localesService.getString(locale, "pages.users.role") } returns "role"
        every { localesService.getString(locale, "pages.users.numberofbeers") } returns "numberofbeers"
        every { localesService.getString(locale, "role.user") } returns "Brygger"
        every { localesService.getString(locale, "menu.users") } returns "users"
        every { userRepository.findAll() } returns listOf(userEntity)
        val xlsx = testSubject.getUsersXlsx(locale)
        Assertions.assertNotNull(xlsx)
    }
}