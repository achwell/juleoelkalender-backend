package no.juleoelkalender.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tools.jackson.databind.json.JsonMapper

class JsonConfigTest {
    private val config = JsonConfig()

    @Test
    fun `mapper bean omits null properties`() {
        assertNullPropertiesOmitted(config.jsonMapper())
    }

    @Test
    fun `builder customizer omits null properties`() {
        val builder = JsonMapper.builder()
        config.jacksonCustomizer().customize(builder)
        assertNullPropertiesOmitted(builder.build())
    }

    private fun assertNullPropertiesOmitted(mapper: JsonMapper) {
        val json = mapper.readTree(
            mapper.writeValueAsString(TestObject(null, "", false, NestedObject(null, "value")))
        )

        assertFalse(json.has("missing"))
        assertEquals("", json.get("empty").asString())
        assertFalse(json.get("enabled").asBoolean())
        assertTrue(json.has("nested"))
        assertFalse(json.get("nested").has("missing"))
        assertEquals("value", json.get("nested").get("present").asString())
    }

    data class TestObject(val missing: String?, val empty: String, val enabled: Boolean, val nested: NestedObject)
    data class NestedObject(val missing: String?, val present: String)
}
