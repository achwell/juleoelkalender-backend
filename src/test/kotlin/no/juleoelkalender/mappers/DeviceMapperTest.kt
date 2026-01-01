package no.juleoelkalender.mappers

import no.juleoelkalender.entity.DeviceEntity
import no.juleoelkalender.getDevice
import no.juleoelkalender.getDeviceEntity
import no.juleoelkalender.model.Device
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class DeviceMapperTest {
    private lateinit var testSubject: DeviceMapper
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
        testSubject = DeviceMapper(userWithoutChildrenMapper)
    }

    @Test
    fun testEntityToModel() {
        val device = testSubject.entityToModel(deviceEntity)
        assertAll(
                { assertNotNull(device) },
                { assertEquals(deviceEntity.mobile, device.isMobile) },
                { assertNotNull(device.user) },
                { assertNotNull(device.user.role) },
                { assertEquals(deviceEntity.id, device.id) },
                { assertEquals(deviceEntity.user.id, device.user.id) },
                {
                    assertEquals(
                            deviceEntity.user.role.id,
                            device.user.role.id
                    )
                }
        )
    }

    @Test
    fun testModelToEntity() {
        val deviceEntity = testSubject.modelToEntity(device)
        assertAll(
                { assertNotNull(deviceEntity) },
                { assertEquals(device.isMobile, deviceEntity.mobile) },
                { assertNotNull(deviceEntity.user) },
                { assertEquals(device.id, deviceEntity.id) },
                { assertEquals(device.user.id, deviceEntity.user.id) }
        )
    }
}
