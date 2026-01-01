package no.juleoelkalender.service

import tools.jackson.databind.JsonNode
import java.io.IOException

interface LocalesService {
    @Throws(IOException::class)
    fun getLocale(locale: String): JsonNode

    fun addMissing(locale: String, missingData: Map<String, String>)

    fun getString(locale: String, key: String): String
}