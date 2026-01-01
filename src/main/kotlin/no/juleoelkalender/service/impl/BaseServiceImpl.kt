package no.juleoelkalender.service.impl

import no.juleoelkalender.mappers.BaseMapper
import no.juleoelkalender.service.BaseService
import org.springframework.data.jpa.repository.JpaRepository

abstract class BaseServiceImpl<PK : Any, T : Any, E : Any> protected constructor(protected val repository: JpaRepository<E, PK>, protected val mapper: BaseMapper<T, E>) : BaseService<PK, T> {

    override val all: Set<T>
        get() = repository.findAll().map { mapper.entityToModel(it) }.map { postGet(it) }.toSet()

    override fun getById(id: PK): T? {
        val entity = repository.findById(id)
        return if (entity.isPresent) {
            postGet(mapper.entityToModel(entity.get()))
        } else null
    }

    override fun create(model: T): T {
        return mapper.entityToModel(repository.save(preCreate(model)))
    }

    override fun update(id: PK, model: T): T? {
        preUpdate(model)
        var updated: T? = null
        repository.findById(id).ifPresent {
            mapModelToEntity(model, it)
            updated = mapper.entityToModel(repository.save(it))
        }
        return updated
    }


    override fun delete(id: PK): Boolean {
        preDelete(id)
        if (repository.existsById(id)) {
            repository.deleteById(id)
            return true
        }
        return false
    }


    @Throws(RuntimeException::class)
    protected open fun preDelete(id: PK) {
    }

    @Throws(RuntimeException::class)
    protected open fun preUpdate(model: T) {
    }

    protected open fun postGet(model: T): T {
        return model
    }

    protected open fun preCreate(model: T): E {
        return mapper.modelToEntity(model)
    }

    protected abstract fun mapModelToEntity(model: T, entity: E)
}
