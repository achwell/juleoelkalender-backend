package no.juleoelkalender.service

interface BaseService<PK, T> {
    val all: Set<T>

    fun getById(id: PK): T?

    fun create(model: T): T

    fun update(id: PK, model: T): T?

    fun delete(id: PK): Boolean
}
