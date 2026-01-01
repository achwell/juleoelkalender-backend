package no.juleoelkalender.service.impl

import no.juleoelkalender.entity.CalendarTokenEntity
import no.juleoelkalender.entity.RoleNameEntity
import no.juleoelkalender.entity.UserEntity
import no.juleoelkalender.exception.UserExistException
import no.juleoelkalender.mappers.UserMapper
import no.juleoelkalender.model.User
import no.juleoelkalender.repository.CalendarTokenRepository
import no.juleoelkalender.repository.DeviceRepository
import no.juleoelkalender.repository.RoleRepository
import no.juleoelkalender.repository.UserRepository
import no.juleoelkalender.service.LocalesService
import no.juleoelkalender.service.UserService
import no.juleoelkalender.utils.ExcelGenerator
import org.apache.commons.lang3.ObjectUtils
import org.jspecify.annotations.NullMarked
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.util.UUID

@Service
class UserServiceImpl(
        private val userRepository: UserRepository, private val calendarTokenRepository: CalendarTokenRepository,
        private val roleRepository: RoleRepository, mapper: UserMapper, private val passwordEncoder: PasswordEncoder,
        private val excelGenerator: ExcelGenerator, private val localesService: LocalesService,
        private val deviceRepository: DeviceRepository
) : BaseServiceImpl<UUID, User, UserEntity>(userRepository, mapper), UserService {

    @NullMarked
    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(email: String): UserDetails {
        val userEntity = userRepository.findByEmailIgnoreCase(email)
                ?: throw UsernameNotFoundException("User with email: $email not found !")
        userEntity.calendarToken = userEntity.calendarToken.filter(CalendarTokenEntity::active).toMutableSet()
        val user = mapper.entityToModel(userEntity)
        return postGet(user)
    }

    override fun updatePassword(email: String, password: String): User {
        val userEntity = userRepository.findByEmailIgnoreCase(email)
                ?: throw UsernameNotFoundException("User with email: $email not found !")
        userEntity.password = passwordEncoder.encode(password)!!
        return mapper.entityToModel(repository.save(userEntity))
    }

    override fun getUsersXlsx(locale: String): ByteArray {
        val headers = arrayOf(
                localesService.getString(locale, "pages.users.name"),
                localesService.getString(locale, "pages.users.email"),
                localesService.getString(locale, "pages.users.area"),
                localesService.getString(locale, "pages.users.role"),
                localesService.getString(locale, "pages.users.numberofbeers")
        )
        val rowData = all.map {
            val role = localesService.getString(
                    locale,
                    it.role.name.name.lowercase().replace("_", ".")
            )
            arrayOf<Any>(
                    it.name, it.email, it.area ?: "", role,
                    it.beers.size
            )
        }.toList()
        val sheetname = localesService.getString(locale, "menu.users")
        return excelGenerator.generateReport(sheetname, headers, rowData)
    }

    override fun preCreate(model: User): UserEntity {
        val existingUser = userRepository.findByEmailIgnoreCase(model.email)
        if (existingUser != null) {
            throw UserExistException("Brukeren finnes allerede")
        }
        val role = roleRepository.findRoleEntityByName(RoleNameEntity.valueOf(model.role.name.name))
        val calendarTokens = model.calendarToken.mapNotNull { calendarTokenRepository.findCalendarTokenByToken(it.token) }.toMutableSet()
        model.pwd = passwordEncoder.encode(model.pwd)!!
        val userEntity = mapper.modelToEntity(model)
        userEntity.role = role!!
        userEntity.calendarToken = calendarTokens
        userEntity.updatedDate = ZonedDateTime.now()
        return userEntity
    }

    override fun postGet(model: User): User {
        val securityContext = SecurityContextHolder.getContext()
        if (securityContext.authentication == null || securityContext.authentication!!.authorities.none { "user:seelogintime" == it.authority }
        ) {
            model.lastLoginDate = null
        }
        return model
    }

    override fun mapModelToEntity(model: User, entity: UserEntity) {
        val role = roleRepository.findRoleEntityByName(RoleNameEntity.valueOf(model.role.name.name))
        entity.area = model.area
        entity.email = model.email
        entity.firstName = model.firstName
        entity.middleName = model.middleName
        entity.lastName = model.lastName
        entity.locked = model.locked
        entity.role = role!!
        entity.facebookUserId = model.facebookUserId
        entity.imageUrl = model.imageUrl
        entity.imageHeight = model.imageHeight
        entity.imageWidth = model.imageWidth
        entity.imageSilhouette = model.imageSilhouette
        val calendarTokens = model.calendarToken.mapNotNull { calendarTokenRepository.findCalendarTokenByToken(it.token) }.toMutableSet()
        entity.calendarToken = calendarTokens
        if (ObjectUtils.notEqual(entity.password, model.pwd)) {
            entity.password = passwordEncoder.encode(model.pwd)!!
        }
    }

    @Throws(RuntimeException::class)
    override fun preDelete(id: UUID) {
        deviceRepository.deleteByUserId(id)
        super.preDelete(id)
    }
}