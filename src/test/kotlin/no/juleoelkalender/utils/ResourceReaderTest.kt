package no.juleoelkalender.utils

import io.mockk.every
import io.mockk.mockk
import no.juleoelkalender.utils.ResourceReader.asString
import org.apache.commons.io.input.NullInputStream
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.core.io.InputStreamResource
import org.springframework.core.io.Resource
import java.io.IOException
import java.io.UncheckedIOException

internal class ResourceReaderTest {
    var resource = mockk<Resource>()

    @Test
    fun testAsStringOk() {
        val resource: Resource = InputStreamResource(NullInputStream())
        val string = asString(resource)
        assertEquals("", string)
    }

    @Test
    @Throws(IOException::class)
    fun testAsStringException() {
        every { resource.inputStream } throws IOException("")
        val thrown = assertThrows<UncheckedIOException> { asString(resource) }
        assertAll(
                { assertNotNull(thrown) },
                { assertEquals("java.io.IOException: ", thrown.message) }
        )
    }
}
