package no.juleoelkalender.service

import io.mockk.every
import io.mockk.mockk
import no.juleoelkalender.entity.DeviceEntity
import no.juleoelkalender.getDevice
import no.juleoelkalender.getDeviceEntity
import no.juleoelkalender.mappers.*
import no.juleoelkalender.model.Device
import no.juleoelkalender.repository.DeviceRepository
import no.juleoelkalender.service.impl.DeviceServiceImpl
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Optional

internal class DeviceServiceTest {
    private lateinit var testSubject: DeviceService

    private val deviceRepository = mockk<DeviceRepository>()
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
        testSubject = DeviceServiceImpl(deviceRepository, deviceMapper, userWithoutChildrenMapper)
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
        every { deviceRepository.save(any()) } returns deviceEntity
        val result = testSubject.create(device)
        assertAll(
                { assertNotNull(result) },
                { assertEquals(device.user.id, result.user.id) },
                { assertTrue(result.isMobile) },
                { assertEquals(deviceEntity.browserName, result.browserName) }
        )
    }

    @Test
    fun testUpdate() {
        every { deviceRepository.findById(any()) } returns Optional.of(deviceEntity)
        val updated = testSubject.update(device.id, device)
        assertNotNull(updated)
    }
}
