package no.juleoelkalender.config

import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.core.json.JsonReadFeature.ALLOW_JAVA_COMMENTS
import tools.jackson.core.json.JsonReadFeature.ALLOW_SINGLE_QUOTES
import tools.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import tools.jackson.databind.cfg.CoercionAction.AsEmpty
import tools.jackson.databind.cfg.CoercionConfigs
import tools.jackson.databind.cfg.CoercionInputShape.EmptyArray
import tools.jackson.databind.cfg.CoercionInputShape.EmptyObject
import tools.jackson.databind.cfg.CoercionInputShape.EmptyString
import tools.jackson.databind.cfg.DateTimeFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE
import tools.jackson.databind.cfg.DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.type.LogicalType.Array
import tools.jackson.databind.type.LogicalType.Collection
import tools.jackson.databind.type.LogicalType.Map
import tools.jackson.databind.type.LogicalType.Textual
import tools.jackson.module.kotlin.KotlinFeature
import tools.jackson.module.kotlin.KotlinModule
import java.util.TimeZone

@Configuration
class JsonConfig {

    @Bean
    fun jsonMapper(): JsonMapper = jsonMapperBuilderConfigurer(JsonMapper.builder()).build()

    @Bean
    fun jacksonCustomizer(): JsonMapperBuilderCustomizer = JsonMapperBuilderCustomizer(::jsonMapperBuilderConfigurer)

    private fun jsonMapperBuilderConfigurer(builder: JsonMapper.Builder): JsonMapper.Builder {
        val kotlinModule = KotlinModule.Builder().configure(KotlinFeature.NullIsSameAsDefault, true).build()

        return builder
            .addModule(kotlinModule)
            .findAndAddModules()
            .defaultTimeZone(TimeZone.getTimeZone("Europe/Oslo"))
            .changeDefaultPropertyInclusion { it.withValueInclusion(NON_NULL) }
            .disable(FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(ALLOW_SINGLE_QUOTES, ALLOW_JAVA_COMMENTS)
            .disable(WRITE_DATES_AS_TIMESTAMPS, ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
            .withAllCoercionConfigs(::findOrCreateCoercion)
    }

    private fun findOrCreateCoercion(configs: CoercionConfigs) {
        configs.findOrCreateCoercion(Array).setCoercion(EmptyArray, AsEmpty)
        configs.findOrCreateCoercion(Collection).setCoercion(EmptyObject, AsEmpty)
        configs.findOrCreateCoercion(Map).setCoercion(EmptyObject, AsEmpty)
        configs.findOrCreateCoercion(Textual).setCoercion(EmptyString, AsEmpty)
    }
}