package no.juleoelkalender.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import tools.jackson.databind.JsonNode
import tools.jackson.databind.json.JsonMapper
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class OpenApiSchemaTest {
    @LocalServerPort
    private var port: Int = 0

    @Test
    fun `generated schemas describe optional non-null properties and retain references`() {
        val request = HttpRequest.newBuilder(URI.create("http://localhost:$port/v3/api-docs")).GET().build()
        val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
        assertEquals(200, response.statusCode())

        val schemas = JsonMapper().readTree(response.body()).path("components").path("schemas")
        val beer = schemas.path("Beer")
        assertEquals("Et øl og informasjon om brygging, brygger og anmeldelser.", beer.path("description").asString())
        assertEquals("5.5", beer.path("properties").path("abv").path("example").asString())
        assertEquals(setOf("id", "name", "style", "abv", "ibu", "ebc", "brewedDate", "bottleDate", "archived", "brewer", "reviews", "createdDate"), requiredProperties(beer))

        assertOptionalProperties(schemas, "Beer", "description", "recipe", "untapped", "desiredDate")
        assertEquals("string", beer.path("properties").path("description").path("type").asString())
        assertEquals("integer", beer.path("properties").path("desiredDate").path("type").asString())
        assertOptionalProperties(schemas, "Calendar", "id")
        assertOptionalProperties(schemas, "BeerStyle", "id")
        assertOptionalProperties(schemas, "Review", "id", "comment")
        assertOptionalProperties(schemas, "ReviewWithoutChildren", "comment")
        assertOptionalProperties(schemas, "ReviewWithUser", "comment")
        assertOptionalProperties(schemas, "RegisterRequest", "middleName", "area")
        assertOptionalProperties(schemas, "User", "id", "middleName", "area", "lastLoginDate", "facebookUserId", "imageUrl", "imageHeight", "imageWidth")
        assertOptionalProperties(schemas, "UserWithoutChildren", "id", "middleName", "area", "lastLoginDate", "facebookUserId", "imageUrl", "imageHeight", "imageWidth")
        assertOptionalProperties(schemas, "AuthenticationResponse", "token", "user", "message")
        assertOptionalProperties(schemas, "BeerWithCalendarAndDay", "calendar")
        assertOptionalProperties(schemas, "BeerWithCalendarDayAndReview", "review")
        assertOptionalProperties(schemas, "DashboardData", "newestBeer")
        assertOptionalProperties(schemas, "GoogleAuthenticationRequest", "picture")
        assertOptionalProperties(schemas, "FacebookAuthenticationRequest", "middleName", "picture")
        assertOptionalProperties(schemas, "FacebookPicture", "data")

        assertTrue(schemas.path("AuthenticationResponse").path("properties").path("user").toString().contains("#/components/schemas/User"))
        assertTrue(schemas.path("BeerWithCalendarAndDay").path("properties").path("calendar").toString().contains("#/components/schemas/Calendar"))
    }

    private fun requiredProperties(schema: JsonNode): Set<String> {
        val requiredNode = schema.path("required")
        return (0 until requiredNode.size()).map { requiredNode.get(it).asString() }.toSet()
    }

    private fun assertOptionalProperties(schemas: JsonNode, schemaName: String, vararg names: String) {
        val schema = schemas.path(schemaName)
        assertFalse(schema.isMissingNode, "Missing schema $schemaName")
        val required = requiredProperties(schema)
        for (name in names) {
            val property = schema.path("properties").path(name)
            assertFalse(property.isMissingNode, "Missing $schemaName.$name")
            assertFalse(name in required, "$schemaName.$name must be optional")
            assertFalse(property.path("nullable").asBoolean(false), "$schemaName.$name must not be nullable")
            assertFalse(property.toString().contains("\"null\""), "$schemaName.$name must not include null")
        }
    }
}
