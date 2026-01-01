package no.juleoelkalender.service

import no.juleoelkalender.service.impl.LocalesServiceImpl
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tools.jackson.databind.json.JsonMapper
import java.io.File
import java.io.IOException

internal class LocalesServiceTest {
    private lateinit var testSubject: LocalesService

    @BeforeEach
    fun setUp() {
        testSubject = LocalesServiceImpl(JsonMapper(), File(".").absolutePath)
    }

    @Test
    @Throws(IOException::class)
    fun testGetLocale() {
        val locale = testSubject.getLocale("no")
        assertNotNull(locale)
    }

    @Test
    fun testAddMissing() {
        try {
            val missingData = mutableMapOf<String, String>()
            missingData["test.test"] = "test"
            testSubject.addMissing("no", missingData)
        } catch (e: Exception) {
            fail("Should throw get error, but got " + e.javaClass.getName())
        }
    }

    @Test
    fun getString() {
        assertEquals("Du har ikke tilgang", testSubject.getString("no", "pages.403.header"))
    }
}
