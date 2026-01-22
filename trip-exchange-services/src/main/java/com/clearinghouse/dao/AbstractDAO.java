/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;

/**
 * @param <PK>
 * @param <T>
 * @author manisha
 */
public abstract class AbstractDAO<PK extends Serializable, T> {

    private final Class<T> persistentClass;

    @PersistenceContext
    EntityManager entityManager;

    public AbstractDAO() {
        this.persistentClass = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[1];
    }

    protected EntityManager getEntityManager() {
        return this.entityManager;
    }

    protected T getByKey(PK key) {
        return entityManager.find(persistentClass, key);
    }

    protected void add(T entity) {
        entityManager.persist(entity);
    }

    protected T update(T entity) {
        return entityManager.merge(entity);
    }

    protected void delete(T entity) {
        entityManager.remove(entity);
    }

}
