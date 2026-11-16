package no.juleoelkalender.service

import no.juleoelkalender.service.impl.LocalesServiceImpl
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import tools.jackson.databind.json.JsonMapper
import java.io.IOException
import java.nio.file.Path

internal class LocalesServiceTest {
    private lateinit var testSubject: LocalesService

    @TempDir
    lateinit var tempDir: Path

    @BeforeEach
    fun setUp() {
        testSubject = LocalesServiceImpl(JsonMapper(), tempDir.toFile().absolutePath)
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
            val missingFile = tempDir.resolve("locales/no/missing.json").toFile()
            assertTrue(missingFile.isFile)
            assertTrue(missingFile.readText().contains("---MISSING---   test   ---MISSING---"))
        } catch (e: Exception) {
            fail("Should throw get error, but got " + e.javaClass.getName())
        }
    }

    @Test
    fun testAddMissingRejectsLocalePathTraversal() {
        assertThrows<IllegalArgumentException> {
            testSubject.addMissing("../outside", mapOf("key" to "value"))
        }
    }

    @Test
    fun getString() {
        assertEquals("Du har ikke tilgang", testSubject.getString("no", "pages.403.header"))
    }
}
