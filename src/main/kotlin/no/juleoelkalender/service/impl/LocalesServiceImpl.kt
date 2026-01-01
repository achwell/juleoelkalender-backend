package no.juleoelkalender.service.impl

import no.juleoelkalender.exception.LocalesException
import no.juleoelkalender.service.LocalesService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import org.springframework.util.FileCopyUtils
import tools.jackson.databind.JsonNode
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.node.ObjectNode
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.nio.charset.StandardCharsets

@Service
class LocalesServiceImpl(
        private val jsonMapper: JsonMapper,
        @param:Value($$"${app.locales.missing}") private val missingRootLocation: String
) : LocalesService {

    @Throws(IOException::class)
    override fun getLocale(locale: String): JsonNode {
        val resource = ClassPathResource("/locales/$locale/translation.json")
        val inputStream = resource.inputStream
        val binaryData = FileCopyUtils.copyToByteArray(inputStream)
        val strJson = String(binaryData, StandardCharsets.UTF_8)
        return jsonMapper.readTree(strJson)
    }

    override fun addMissing(locale: String, missingData: Map<String, String>) {
        try {
            val resource = ClassPathResource("locales/$locale/missing.json")
            val path: String = "%s/%s".format(missingRootLocation, resource.path)
            val missingFile = File(path)
            log.warn(missingFile.absolutePath)
            if (!missingFile.exists() && File(missingFile.getParent()).mkdirs()) {
                if (missingFile.createNewFile()) {
                    log.info("File created: {}", missingFile.getName())
                    FileWriter(missingFile).use { myWriter ->
                        myWriter.write("{}")
                    }
                } else {
                    log.info("File already exists.")
                }
            }
            val missingJson = jsonMapper.readTree(missingFile) as ObjectNode
            missingData.forEach { (s: String, s2: String) ->
                var current = missingJson
                val parts: Array<String> = s.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val length = parts.size - 1
                for (i in 0..<length) {
                    val part = parts[i]
                    if (!current.has(part)) {
                        current.set(part, jsonMapper.createObjectNode())
                    }
                    current = current.get(part) as ObjectNode
                }
                current.put(parts[length], "---MISSING---   $s2   ---MISSING---")
            }
            FileWriter(missingFile).use { myWriter ->
                myWriter.write(jsonMapper.writeValueAsString(missingJson))
            }
        } catch (e: IOException) {
            throw LocalesException(e)
        }
    }

    override fun getString(locale: String, key: String): String {
        try {
            var textNode = getLocale(locale)
            val parts: Array<String> = key.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (part in parts) {
                textNode = textNode.get(part)
            }
            return textNode.asString()
        } catch (e: IOException) {
            val missing: MutableMap<String, String> = mutableMapOf()
            missing[key] = key
            addMissing(locale, missing)
            return ""
        }
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(LocalesServiceImpl::class.java)
    }
}