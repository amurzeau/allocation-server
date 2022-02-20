package org.amurzeau.allocation.rest;

import java.util.List;

import javax.persistence.Id;
import javax.persistence.LockModeType;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotBlank;

import org.jboss.logging.Logger;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.hibernate.reactive.panache.runtime.JpaOperations;
import io.smallrye.mutiny.Uni;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;

@MappedSuperclass
@FieldNameConstants
@EqualsAndHashCode(callSuper = true)
public class NamedItem extends PanacheEntityBase {
    private static final Logger LOG = Logger.getLogger(NamedItem.class);

    @Id
    public String id;

    @NotBlank
    public String name;

    public Boolean isDisabled;

    public static <T extends NamedItem> Uni<List<T>> getAll(Class<T> entityClass, Boolean deleted) {
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

    public static <T extends NamedItem> Uni<T> getById(Class<T> entityClass, String id) {
        @SuppressWarnings("unchecked")
        Uni<T> var = (Uni<T>) JpaOperations.INSTANCE.findById(entityClass, id);

        return var;
    }

    public static <T extends NamedItem> Uni<Boolean> postCreate(T entity) {
        if (entity.isDisabled == null)
            entity.isDisabled = false;

        return Panache.withTransaction(() -> {
            @SuppressWarnings("unchecked")
            Uni<T> var = (Uni<T>) JpaOperations.INSTANCE.findById(entity.getClass(), entity.id);

            return var.onItem().<Boolean>transformToUni(item -> {
                if (item == null) {
                    LOG.infov("Creating new item {0} with name {1}", entity.id, entity.name);
                    return JpaOperations.INSTANCE.persist(entity).replaceWith(Boolean.TRUE);
                } else {
                    LOG.infov("Can't create {0}, already existing with name {1}", item.id, item.name);
                    return Uni.createFrom().item(Boolean.FALSE);
                }
            });
        });
    }

    public static <T extends NamedItem> Uni<T> putUpdate(Class<T> entityClass, String id, T value) {
        return Panache.withTransaction(() -> {
            @SuppressWarnings("unchecked")
            Uni<T> var = (Uni<T>) JpaOperations.INSTANCE.findById(entityClass, id, LockModeType.PESSIMISTIC_WRITE);
            return var.onItem().<T>transformToUni(item -> {
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

                return updatedItem.persist();
            }).onFailure().invoke((e) -> {
                LOG.errorv("Failure: {0}", e);
            });
        });
    }

    public static <T> Uni<Boolean> delete(Class<T> entityClass, String id) {
        return Panache.withTransaction(() -> {
            return JpaOperations.INSTANCE.deleteById(entityClass, id);
        });
    }
}
