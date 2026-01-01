package no.juleoelkalender.mappers

interface BaseMapper<MODEL, ENTITY> {
    fun entityToModel(entity: ENTITY): MODEL

    fun modelToEntity(model: MODEL): ENTITY
}
