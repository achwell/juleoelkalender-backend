package no.juleoelkalender.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.juleoelkalender.entity.DeviceEntity
import no.juleoelkalender.getDevice
import no.juleoelkalender.getDeviceEntity
import no.juleoelkalender.mappers.AuthorityMapper
import no.juleoelkalender.mappers.CalendarTokenMapper
import no.juleoelkalender.mappers.DeviceMapper
import no.juleoelkalender.mappers.RoleMapper
import no.juleoelkalender.mappers.UserWithoutChildrenMapper
import no.juleoelkalender.model.Device
import no.juleoelkalender.repository.DeviceRepository
import no.juleoelkalender.repository.UserRepository
import no.juleoelkalender.service.impl.DeviceServiceImpl
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Optional

internal class DeviceServiceTest {
    private lateinit var testSubject: DeviceService

    private val deviceRepository = mockk<DeviceRepository>()
    private val userRepository = mockk<UserRepository>()
    private lateinit var device: Device
    private lateinit var deviceEntity: DeviceEntity

    @BeforeEach
    fun setUp() {
        device = getDevice()
        deviceEntity = getDeviceEntity()
        val authorityMapper = AuthorityMapper()
        val calendarTokenMapper = CalendarTokenMapper()
        val roleMapper = RoleMapper(authorityMapper)
        val userWithoutChildrenMapper = UserWithoutChildrenMapper(calendarTokenMapper, roleMapper)
        val deviceMapper = DeviceMapper(userWithoutChildrenMapper)
        testSubject = DeviceServiceImpl(deviceRepository, deviceMapper, userRepository)
    }

    @Test
    fun testGetDevices() {
        every { deviceRepository.findAll() } returns listOf(deviceEntity)
        val devices = testSubject.all
        assertAll(
            { assertNotNull(devices) },
            { assertEquals(1, devices.size) }
        )
    }

    @Test
    fun testCreateDevice() {
        every { userRepository.getReferenceById(any()) } returns deviceEntity.user
        every { deviceRepository.save(any()) } returns deviceEntity
        val result = testSubject.create(device)
        assertAll(
            { assertNotNull(result) },
            { assertEquals(device.user.id, result.user.id) },
            { assertTrue(result.isMobile) },
            { assertEquals(deviceEntity.browserName, result.browserName) }
        )
        verify(exactly = 1) { deviceRepository.save(any()) }
    }

    @Test
    fun testUpdate() {
        every { userRepository.getReferenceById(any()) } returns deviceEntity.user
        every { deviceRepository.findById(any()) } returns Optional.of(deviceEntity)
        every { deviceRepository.save(any()) } returns deviceEntity
        val updated = testSubject.update(device.id, device)
        assertNotNull(updated)
    }

    @Test
    fun testUserDisplayName() {
        assertEquals("FIRST LAST", device.user.name())
    }
}
