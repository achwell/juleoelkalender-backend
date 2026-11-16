package no.juleoelkalender.service.impl

import no.juleoelkalender.exception.LocalesException
import no.juleoelkalender.service.LocalesService
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import org.springframework.util.FileCopyUtils
import tools.jackson.databind.JsonNode
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.node.ObjectNode
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

@Service
class LocalesServiceImpl(
    private val jsonMapper: JsonMapper,
    @param:Value($$"${app.locales.missing}") private val missingRootLocation: String
) : LocalesService {

    @Throws(IOException::class)
    override fun getLocale(locale: String): JsonNode {
        requireValidLocale(locale)
        val resource = ClassPathResource("/locales/$locale/translation.json")
        val binaryData = resource.inputStream.use(FileCopyUtils::copyToByteArray)
        val strJson = String(binaryData, StandardCharsets.UTF_8)
        return jsonMapper.readTree(strJson)
    }

    override fun addMissing(locale: String, missingData: Map<String, String>) {
        try {
            requireValidLocale(locale)
            val root = Path.of(missingRootLocation).toAbsolutePath().normalize()
            val missingFile = root.resolve(Path.of("locales", locale, "missing.json")).normalize()
            require(missingFile.startsWith(root)) { "Invalid locale path" }
            Files.createDirectories(missingFile.parent)
            if (Files.notExists(missingFile)) {
                Files.writeString(missingFile, "{}", StandardCharsets.UTF_8)
            }
            val missingJson = jsonMapper.readTree(missingFile.toFile()) as? ObjectNode
                ?: throw IllegalArgumentException("Missing locale file must contain a JSON object")
            missingData.forEach { (key, value) ->
                require(key.isNotBlank()) { "Locale key must not be blank" }
                var current = missingJson
                val parts = key.split('.')
                require(parts.all(String::isNotBlank)) { "Locale key contains an empty path segment: $key" }
                for (part in parts.dropLast(1)) {
                    if (!current.has(part)) {
                        current.set(part, jsonMapper.createObjectNode())
                    }
                    current = current.get(part) as? ObjectNode
                        ?: throw IllegalArgumentException("Locale key conflicts with an existing value: $key")
                }
                current.put(parts.last(), "---MISSING---   $value   ---MISSING---")
            }
            Files.writeString(missingFile, jsonMapper.writeValueAsString(missingJson), StandardCharsets.UTF_8)
        } catch (e: IOException) {
            throw LocalesException(e)
        }
    }

    override fun getString(locale: String, key: String): String {
        try {
            var textNode = getLocale(locale)
            val parts = key.split('.')
            for (part in parts) {
                val nextNode = textNode.get(part)
                if (nextNode == null || nextNode.isMissingNode) {
                    addMissing(locale, mapOf(key to key))
                    return ""
                }
                textNode = nextNode
            }
            return textNode.asString()
        } catch (e: IOException) {
            val missing: MutableMap<String, String> = mutableMapOf()
            missing[key] = key
            addMissing(locale, missing)
            return ""
        }
    }

    private fun requireValidLocale(locale: String) {
        require(locale.matches(Regex("[A-Za-z0-9_-]+"))) { "Invalid locale: $locale" }
    }

}