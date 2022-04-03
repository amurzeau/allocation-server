package org.amurzeau.allocation.services;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.LockModeType;
import javax.transaction.Transactional;

import org.amurzeau.allocation.rest.ActivityType;
import org.amurzeau.allocation.rest.NamedItem;
import org.jboss.logging.Logger;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.runtime.JpaOperations;

@ApplicationScoped
public class NamedItemService {
    private static final Logger LOG = Logger.getLogger(NamedItem.class);

    public <T extends NamedItem> List<T> getAll(Class<T> entityClass, Boolean deleted) {
        PanacheQuery<T> result;

        if (deleted != null && deleted == true) {
            @SuppressWarnings("unchecked")
            PanacheQuery<T> var = (PanacheQuery<T>) JpaOperations.INSTANCE.findAll(entityClass);
            result = var;
        } else {
            @SuppressWarnings("unchecked")
            PanacheQuery<T> var = (PanacheQuery<T>) JpaOperations.INSTANCE.find(entityClass,
                    ActivityType.Fields.isDisabled, false);
            result = var;
        }

        return result.list();
    }

    public <T extends NamedItem> T getById(Class<T> entityClass, String id) {
        @SuppressWarnings("unchecked")
        T var = (T) JpaOperations.INSTANCE.findById(entityClass, id);

        return var;
    }

    @Transactional
    public <T extends NamedItem> boolean postCreate(T entity) {
        if (entity.isDisabled == null)
            entity.isDisabled = false;

        @SuppressWarnings("unchecked")
        T item = (T) JpaOperations.INSTANCE.findById(entity.getClass(), entity.id);

        if (item == null) {
            LOG.infov("Creating new item {0} with name {1}", entity.id, entity.name);
            JpaOperations.INSTANCE.persist(entity);
            return true;
        } else {
            LOG.infov("Can't create {0}, already existing with name {1}", item.id, item.name);
            return false;
        }
    }

    @Transactional
    public <T extends NamedItem> T putUpdate(Class<T> entityClass, String id, T value) {
        @SuppressWarnings("unchecked")
        T item = (T) JpaOperations.INSTANCE.findById(entityClass, id, LockModeType.PESSIMISTIC_WRITE);
        T updatedItem;

        if (item == null) {
            LOG.infov("Creating new item {0} with name {1}", id, value.name);
            updatedItem = value;
            updatedItem.id = id;

            if (updatedItem.isDisabled == null)
                updatedItem.isDisabled = false;
        } else {
            LOG.infov("Updating item {0} with name {1}", item.id, value.name);
            updatedItem = item;

            if (value.name != null)
                updatedItem.name = value.name;

            if (value.isDisabled != null)
                updatedItem.isDisabled = value.isDisabled;
        }

        updatedItem.persist();

        return updatedItem;
    }

    @Transactional
    public <T> boolean delete(Class<T> entityClass, String id) {
        boolean result = JpaOperations.INSTANCE.deleteById(entityClass, id);
        JpaOperations.INSTANCE.flush(entityClass);

        return result;
    }
}
