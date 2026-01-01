package no.juleoelkalender

import no.juleoelkalender.entity.*
import no.juleoelkalender.mappers.*
import no.juleoelkalender.model.*
import java.time.ZonedDateTime
import java.util.UUID

private const val beerCalendarDay = 1

private val authorityId: UUID = UUID.randomUUID()
private val beerId: UUID = UUID.randomUUID()
private val beerStyleId: UUID = UUID.randomUUID()
private val beerCalendarId: UUID = UUID.randomUUID()
private val calendarId: UUID = UUID.randomUUID()
private val calendarTokenId: UUID = UUID.randomUUID()
private val deviceId: UUID = UUID.randomUUID()
private val passwordChangeRequestId: UUID = UUID.randomUUID()
private val reviewId: UUID = UUID.randomUUID()
private val roleId: UUID = UUID.randomUUID()
private val userId: UUID = UUID.randomUUID()

private val authorityMapper = AuthorityMapper()
private val beerStyleMapper = BeerStyleMapper()
private val calendarTokenMapper = CalendarTokenMapper()
private val roleMapper = RoleMapper(authorityMapper)
private val beerMapper = BeerMapper(UserWithoutChildrenMapper(calendarTokenMapper, roleMapper))
private val calendarMapper = CalendarMapper(beerMapper, calendarTokenMapper)
private val userMapper = UserMapper(beerMapper, calendarTokenMapper, roleMapper)
private const val calendarName = "KAL"
private val now: ZonedDateTime = ZonedDateTime.now()
private val year = now.year

fun getCalendarEntity(): CalendarEntity {
    val beerStyleEntity: BeerStyleEntity = getBeerStyleEntity()
    val calendarTokenEntity: CalendarTokenEntity = getCalendarTokenEntity()
    val roleEntityUser: RoleEntity = getRoleEntityUser()
    val calendarTokenEntityInActive: CalendarTokenEntity = calendarTokenEntityInActive
    val user = UserEntity(
            id = userId,
            firstName = "FIRST",
            middleName = null,
            lastName = "LAST",
            email = "first@last.no",
            password = "pwd",
            area = null,
            role = roleEntityUser,
            locked = false,
            beers = mutableSetOf(),
            devices = mutableSetOf(),
            calendarToken = mutableSetOf(calendarTokenEntityInActive),
            reviews = mutableSetOf(),
            lastLoginDate = now,
            createdDate = now,
            updatedDate = now,
            facebookUserId = null,
            imageUrl = null,
            imageHeight = null,
            imageWidth = null,
            imageSilhouette = false
    )
    val beerEntity =
            BeerEntity(id = beerId, name = "", style = beerStyleEntity.name, description = null, abv = 0.0, ibu = 0.0, ebc = 0.0, recipe = null, untapped = null, brewedDate = now, bottleDate = now, archived = false, user = user, beerCalendars = mutableSetOf(), reviews = mutableSetOf(), createdDate = now, updatedDate = now, desiredDate = null)
    val userEntity = UserEntity(
            id = userId,
            firstName = "FIRST",
            middleName = null,
            lastName = "LAST",
            email = "first@last.no",
            password = "pwd",
            area = null,
            role = roleEntityUser,
            locked = false,
            beers = mutableSetOf(beerEntity),
            devices = mutableSetOf(),
            calendarToken = mutableSetOf(calendarTokenEntity),
            reviews = mutableSetOf(),
            lastLoginDate = now,
            createdDate = now,
            updatedDate = now,
            facebookUserId = null,
            imageUrl = null,
            imageHeight = null,
            imageWidth = null,
            imageSilhouette = false
    )
    val calendarEntity = CalendarEntity(id = calendarId, name = calendarName, year = year, published = true, archived = false, beerCalendars = mutableSetOf(), calendarToken = calendarTokenEntity, reviews = mutableSetOf(), createdDate = now, updatedDate = now)
    BeerCalendarEntity(
            id = beerCalendarId,
            day = beerCalendarDay,
            beer = beerEntity,
            calendar = CalendarEntity(
                    id = calendarId,
                    name = calendarName,
                    year = year,
                    published = true,
                    archived = false,
                    beerCalendars = mutableSetOf(BeerCalendarEntity(id = beerCalendarId, day = beerCalendarDay, beer = beerEntity, calendar = calendarEntity)),
                    calendarToken = calendarTokenEntity,
                    reviews = mutableSetOf(),
                    createdDate = now,
                    updatedDate = now
            )
    )
    val beerCalendarEntity = BeerCalendarEntity(
            id = beerCalendarId,
            day = beerCalendarDay,
            beer = BeerEntity(
                    id = beerId,
                    name = "",
                    style = beerStyleEntity.name,
                    description = null,
                    abv = 0.0,
                    ibu = 0.0,
                    ebc = 0.0,
                    recipe = null,
                    untapped = null,
                    brewedDate = now,
                    bottleDate = now,
                    archived = false,
                    user = userEntity,
                    beerCalendars = mutableSetOf(
                            BeerCalendarEntity(
                                    id = beerCalendarId,
                                    day = beerCalendarDay,
                                    beer = BeerEntity(
                                            id = beerId,
                                            name = "",
                                            style = beerStyleEntity.name,
                                            description = null,
                                            abv = 0.0,
                                            ibu = 0.0,
                                            ebc = 0.0,
                                            recipe = null,
                                            untapped = null,
                                            brewedDate = now,
                                            bottleDate = now,
                                            archived = false,
                                            user = userEntity,
                                            beerCalendars = mutableSetOf(),
                                            reviews = mutableSetOf(),
                                            createdDate = now,
                                            updatedDate = now,
                                            desiredDate = null
                                    ),
                                    calendar = calendarEntity
                            )
                    ),
                    reviews = mutableSetOf(),
                    createdDate = now,
                    updatedDate = now,
                    desiredDate = null
            ),
            calendar = calendarEntity
    )
    val calendarEntity1 =
            CalendarEntity(id = calendarId, name = calendarName, year = year, published = true, archived = false, beerCalendars = mutableSetOf(beerCalendarEntity), calendarToken = calendarTokenEntity, reviews = mutableSetOf(), createdDate = now, updatedDate = now)
    val beerCalendarEntity1 = BeerCalendarEntity(id = beerCalendarId, day = beerCalendarDay, beer = beerEntity, calendar = calendarEntity1)
    val reviewEntity = ReviewEntity(
            id = reviewId,
            ratingLabel = 1.0,
            ratingLooks = 1.0,
            ratingSmell = 1.0,
            ratingTaste = 1.0,
            ratingFeel = 1.0,
            ratingOverall = 1.0,
            comment = null,
            createdAt = now,
            updatedDate = null,
            beer = beerEntity,
            calendar = CalendarEntity(
                    id = calendarId,
                    name = calendarName,
                    year = year,
                    published = true,
                    archived = false,
                    beerCalendars = mutableSetOf(
                            BeerCalendarEntity(
                                    id = beerCalendarId,
                                    day = beerCalendarDay,
                                    beer = beerEntity,
                                    calendar = CalendarEntity(id = calendarId, name = calendarName, year = year, published = true, archived = false, beerCalendars = mutableSetOf(beerCalendarEntity1), calendarToken = calendarTokenEntity, reviews = mutableSetOf(), createdDate = now, updatedDate = now)
                            )
                    ),
                    calendarToken = calendarTokenEntity,
                    reviews = mutableSetOf(),
                    createdDate = now,
                    updatedDate = now
            ),
            user = userEntity
    )
    return CalendarEntity(
            id = calendarId,
            name = calendarName,
            year = year,
            published = true,
            archived = false,
            beerCalendars = mutableSetOf(
                    BeerCalendarEntity(
                            id = beerCalendarId,
                            day = beerCalendarDay,
                            beer = beerEntity,
                            calendar = CalendarEntity(id = calendarId, name = calendarName, year = year, published = true, archived = false, beerCalendars = mutableSetOf(beerCalendarEntity1), calendarToken = calendarTokenEntity, reviews = mutableSetOf(), createdDate = now, updatedDate = now)
                    )
            ),
            calendarToken = calendarTokenEntity,
            reviews = mutableSetOf(reviewEntity),
            createdDate = now,
            updatedDate = now
    )
}


fun getCalendar() = calendarMapper.entityToModel(getCalendarEntity())

fun getBeerCalendarEntity(): BeerCalendarEntity {
    val beerEntity: BeerEntity = getBeerEntity()
    val calendarEntity: CalendarEntity = getCalendarEntity()
    return BeerCalendarEntity(id = beerCalendarId, day = beerCalendarDay, beer = beerEntity, calendar = calendarEntity)
}


fun getBeerCalendar(): BeerCalendar {
    val beerStyle: BeerStyle = getBeerStyle()
    val userWithoutChildren: UserWithoutChildren = getUserWithoutChildren()
    val beer = Beer(id = beerId, name = "", style = beerStyle.name, description = null, abv = 0.0, ibu = 0.0, ebc = 0.0, recipe = null, untapped = null, brewedDate = now, bottleDate = now, archived = false, brewer = userWithoutChildren, reviews = mutableSetOf(), createdDate = now, desiredDate = null)
    val calendar: Calendar = getCalendar()
    val calendarToken: CalendarToken = getCalendarToken()
    return BeerCalendar(
            id = beerCalendarId,
            day = beerCalendarDay,
            beer = beer,
            calendar = Calendar(
                    id = calendar.id,
                    name = calendar.name,
                    year = calendar.year,
                    published = calendar.published,
                    archived = calendar.archived,
                    beerCalendars = mutableSetOf(BeerCalendar(id = beerCalendarId, day = beerCalendarDay, beer = beer, calendar = calendar)),
                    calendarToken = calendarToken
            )
    )
}

fun getReviewEntity(): ReviewEntity {
    val beerEntity: BeerEntity = getBeerEntity()
    val calendarEntity: CalendarEntity = getCalendarEntity()
    val userEntity: UserEntity = getUserEntity()
    return ReviewEntity(id = reviewId, ratingLabel = 1.0, ratingLooks = 1.0, ratingSmell = 1.0, ratingTaste = 1.0, ratingFeel = 1.0, ratingOverall = 1.0, comment = null, createdAt = now, updatedDate = null, beer = beerEntity, calendar = calendarEntity, user = userEntity)
}


fun getCalendarWithBeer(): CalendarWithBeer {
    val beer: Beer = getBeer()
    val beerCalendar: BeerCalendar = getBeerCalendar()
    val calendar: Calendar = getCalendar()
    return CalendarWithBeer(id = calendar.id!!, name = calendar.name, year = calendar.year, published = calendar.published, archived = calendar.archived, beerCalendars = calendar.beerCalendars, calendarToken = calendar.calendarToken, beer = beer, day = beerCalendar.day)
}


fun getBeerWithCalendarAndDay(): BeerWithCalendarAndDay {
    val beer: Beer = getBeer()
    val calendar: Calendar = getCalendar()
    val userWithoutChildren: UserWithoutChildren = getUserWithoutChildren()
    val beerCalendar: BeerCalendar = getBeerCalendar()
    return BeerWithCalendarAndDay(beer = beer, calendar = calendar, brewer = userWithoutChildren, day = beerCalendar.day)
}

val review: Review
    get() {
        val beer: Beer = getBeer()
        val calendar: Calendar = getCalendar()
        val userWithoutChildren: UserWithoutChildren = getUserWithoutChildren()
        return Review(id = reviewId, ratingLabel = 0.0, ratingLooks = 0.0, ratingSmell = 0.0, ratingTaste = 0.0, ratingFeel = 0.0, ratingOverall = 0.0, comment = null, createdAt = now, beer = beer, calendar = calendar, user = userWithoutChildren)
    }


fun getBeerWithCalendarDayAndReview(): BeerWithCalendarDayAndReview {
    val beer: Beer = getBeer()
    val calendar: Calendar = getCalendar()
    val review: Review = review
    val userWithoutChildren: UserWithoutChildren = getUserWithoutChildren()
    return BeerWithCalendarDayAndReview(beer = beer, calendar = calendar, brewer = userWithoutChildren, day = beerCalendarDay, review = review)
}

fun getBeerEntity(): BeerEntity {
    val beerStyleEntity: BeerStyleEntity = getBeerStyleEntity()
    val calendarTokenEntity: CalendarTokenEntity = getCalendarTokenEntity()
    val calendarEntity = CalendarEntity(id = calendarId, name = calendarName, year = year, published = true, archived = false, beerCalendars = mutableSetOf(), calendarToken = calendarTokenEntity, reviews = mutableSetOf(), createdDate = now, updatedDate = now)
    val userEntity: UserEntity = getUserEntity()
    val beerEntity =
            BeerEntity(id = beerId, name = "", style = beerStyleEntity.name, description = null, abv = 0.0, ibu = 0.0, ebc = 0.0, recipe = null, untapped = null, brewedDate = now, bottleDate = now, archived = false, user = userEntity, beerCalendars = mutableSetOf(), reviews = mutableSetOf(), createdDate = now, updatedDate = now, desiredDate = null)
    val beerCalendarEntity = BeerCalendarEntity(id = beerCalendarId, day = beerCalendarDay, beer = beerEntity, calendar = calendarEntity)
    return BeerEntity(
            id = beerId,
            name = "",
            style = beerStyleEntity.name,
            description = null,
            abv = 0.0,
            ibu = 0.0,
            ebc = 0.0,
            recipe = null,
            untapped = null,
            brewedDate = now,
            bottleDate = now,
            archived = false,
            user = userEntity,
            beerCalendars = mutableSetOf(beerCalendarEntity),
            reviews = mutableSetOf(),
            createdDate = now,
            updatedDate = now,
            desiredDate = null
    )
}

fun getPasswordChangeRequestEntity() = PasswordChangeRequestEntity(id = passwordChangeRequestId, token = "TOKEN", email = "first@last.no", created = now, updatedDate = now)

fun getPasswordChangeRequest(): PasswordChangeRequest {
    val passwordChangeRequestEntity: PasswordChangeRequestEntity = getPasswordChangeRequestEntity()
    return PasswordChangeRequest(id = passwordChangeRequestEntity.id, token = passwordChangeRequestEntity.token, email = passwordChangeRequestEntity.email, created = passwordChangeRequestEntity.created)
}


fun getRegisterRequest() = RegisterRequest("TOKEN", "Ny", null, "Bruker", "first@last.no", "password", "AREA")

fun getDevice(): Device {
    val device: DeviceEntity = getDeviceEntity()
    return Device(id = device.id, mobileVendor = device.mobileVendor, mobileModel = device.mobileModel, isMobile = device.mobile, osName = device.osName, osVersion = device.osVersion, browserName = device.browserName, browserVersion = device.browserVersion, user = getUserWithoutChildren())
}


fun getBeer(): Beer {
    val beerEntity: BeerEntity = getBeerEntity()
    return beerMapper.entityToModel(beerEntity)
}

fun getDeviceEntity() = DeviceEntity(id = deviceId, mobileVendor = "MOBILEVENDOR", mobileModel = "MOBILEMODEL", mobile = true, osName = "OSNAME", osVersion = "OSVERSION", browserName = "BROWSERNAME", browserVersion = "BROWSERVERSION", user = getUserEntity(), createdDate = now, updatedDate = now)

fun getBeerStyleEntity() = BeerStyleEntity(id = beerStyleId, name = "Pils")

fun getBeerStyle(): BeerStyle {
    return beerStyleMapper.entityToModel(getBeerStyleEntity())
}

fun getCalendarTokenEntity() = CalendarTokenEntity(id = calendarTokenId, token = "TOKEN", name = "TOKENNAME", active = true, calendars = mutableSetOf(), users = mutableSetOf(), createdDate = now, updatedDate = now)

private val calendarTokenEntityInActive: CalendarTokenEntity
    get() = CalendarTokenEntity(id = calendarTokenId, token = "TOKEN", name = "TOKENNAME", active = false, calendars = mutableSetOf(), users = mutableSetOf(), createdDate = now, updatedDate = now)

fun getCalendarToken() = calendarTokenMapper.entityToModel(getCalendarTokenEntity())

fun getAuthorityEntityUser() = AuthorityEntity(id = authorityId, name = RoleNameEntity.ROLE_USER.name, users = mutableSetOf())

val authorityEntityMaster: AuthorityEntity
    get() = AuthorityEntity(id = authorityId, name = RoleNameEntity.ROLE_MASTER.name, users = mutableSetOf())

fun getRoleEntityUser() = RoleEntity(id = roleId, name = RoleNameEntity.ROLE_USER, authorities = mutableSetOf(getAuthorityEntityUser()), users = mutableSetOf())

fun getRoleEntityMaster() = RoleEntity(id = roleId, name = RoleNameEntity.ROLE_MASTER, authorities = mutableSetOf(authorityEntityMaster), users = mutableSetOf())

fun getUserEntity(): UserEntity {
    val roleEntityUser: RoleEntity = getRoleEntityUser()
    val beerStyleEntity: BeerStyleEntity = getBeerStyleEntity()
    val calendarTokenEntity: CalendarTokenEntity = getCalendarTokenEntity()
    val calendarTokenEntityInActive: CalendarTokenEntity = calendarTokenEntityInActive
    val userEntity = UserEntity(
            id = userId,
            firstName = "FIRST",
            middleName = null,
            lastName = "LAST",
            email = "first@last.no",
            password = "pwd",
            area = null,
            role = roleEntityUser,
            locked = false,
            beers = mutableSetOf(),
            devices = mutableSetOf(),
            calendarToken = mutableSetOf(calendarTokenEntityInActive),
            reviews = mutableSetOf(),
            lastLoginDate = now,
            createdDate = now,
            updatedDate = now,
            facebookUserId = null,
            imageUrl = null,
            imageHeight = null,
            imageWidth = null,
            imageSilhouette = false
    )
    val beerEntity =
            BeerEntity(id = beerId, name = "", style = beerStyleEntity.name, description = null, abv = 0.0, ibu = 0.0, ebc = 0.0, recipe = null, untapped = null, brewedDate = now, bottleDate = now, archived = false, user = userEntity, beerCalendars = mutableSetOf(), reviews = mutableSetOf(), createdDate = now, updatedDate = now, desiredDate = null)
    return UserEntity(
            id = userId,
            firstName = "FIRST",
            middleName = null,
            lastName = "LAST",
            email = "first@last.no",
            password = "pwd",
            area = null,
            role = roleEntityUser,
            locked = false,
            beers = mutableSetOf(beerEntity),
            devices = mutableSetOf(),
            calendarToken = mutableSetOf(calendarTokenEntity),
            reviews = mutableSetOf(),
            lastLoginDate = now,
            createdDate = now,
            updatedDate = now,
            facebookUserId = null,
            imageUrl = null,
            imageHeight = null,
            imageWidth = null,
            imageSilhouette = false
    )
}

fun getUserEntityNoValidToken(): UserEntity {
    val roleEntityUser: RoleEntity = getRoleEntityUser()
    val calendarTokenEntityInActive: CalendarTokenEntity = calendarTokenEntityInActive
    return UserEntity(
            id = userId,
            firstName = "FIRST",
            middleName = null,
            lastName = "LAST",
            email = "first@last.no",
            password = "pwd",
            area = null,
            role = roleEntityUser,
            locked = false,
            beers = mutableSetOf(),
            devices = mutableSetOf(),
            calendarToken = mutableSetOf(calendarTokenEntityInActive),
            reviews = mutableSetOf(),
            lastLoginDate = now,
            createdDate = now,
            updatedDate = now,
            facebookUserId = null,
            imageUrl = null,
            imageHeight = null,
            imageWidth = null,
            imageSilhouette = false
    )
}

fun getUserEntityAdmin(): UserEntity {
    val roleEntityUser: RoleEntity = getRoleEntityUser()
    val calendarTokenEntity: CalendarTokenEntity = getCalendarTokenEntity()
    return UserEntity(
            id = userId,
            firstName = "FIRST",
            middleName = null,
            lastName = "LAST",
            email = "first@last.no",
            password = "pwd",
            area = null,
            role = roleEntityUser,
            locked = false,
            beers = mutableSetOf(),
            devices = mutableSetOf(),
            calendarToken = mutableSetOf(calendarTokenEntity),
            reviews = mutableSetOf(),
            lastLoginDate = now,
            createdDate = now,
            updatedDate = now,
            facebookUserId = null,
            imageUrl = null,
            imageHeight = null,
            imageWidth = null,
            imageSilhouette = false
    )
}

fun getUserEntityMaster(): UserEntity {
    val roleEntityMaster: RoleEntity = getRoleEntityMaster()
    val calendarTokenEntity: CalendarTokenEntity = getCalendarTokenEntity()
    return UserEntity(
            id = userId,
            firstName = "FIRST",
            middleName = null,
            lastName = "LAST",
            email = "first@last.no",
            password = "pwd",
            area = null,
            role = roleEntityMaster,
            locked = false,
            beers = mutableSetOf(),
            devices = mutableSetOf(),
            calendarToken = mutableSetOf(calendarTokenEntity),
            reviews = mutableSetOf(),
            lastLoginDate = now,
            createdDate = now,
            updatedDate = now,
            facebookUserId = null,
            imageUrl = null,
            imageHeight = null,
            imageWidth = null,
            imageSilhouette = false
    )
}


fun getUser() = userMapper.entityToModel(getUserEntity())

fun getUserWithoutChildren() = userMapper.entityToModel(getUserEntity()).userWithoutChildren