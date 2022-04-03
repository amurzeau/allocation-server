package org.amurzeau.allocation;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.amurzeau.allocation.rest.ErrorReply;
import org.amurzeau.allocation.rest.ErrorType;
import org.amurzeau.allocation.rest.NamedItem;
import org.amurzeau.allocation.services.NamedItemService;
import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.jboss.resteasy.annotations.jaxrs.QueryParam;

public class NamedItemRessource<T extends NamedItem> {
    private static final Logger LOG = Logger.getLogger(NamedItemRessource.class);
    private final Class<T> typeParameterClass;

    @Inject
    NamedItemService namedItemService;

    public NamedItemRessource(Class<T> typeParameterClass) {
        this.typeParameterClass = typeParameterClass;
    }

    @GET
    public List<T> getAll(@QueryParam Boolean deleted) {
        return namedItemService.getAll(typeParameterClass, deleted);
    }

    @GET
    @Path("{id}")
    public Response getById(@PathParam String id) {
        NamedItem item = namedItemService.getById(typeParameterClass, id);

        if (item == null) {
            return Response
                    .status(Status.NOT_FOUND)
                    .entity(ErrorReply.create(ErrorType.NOT_EXISTS, "No item with id %s", id))
                    .build();
        }

        return Response.ok(item).build();
    }

    @POST
    public Response postNew(T value, UriInfo uriInfo) {
        if (value.id == null) {
            LOG.errorv("id is required");
            return Response.status(Status.NOT_ACCEPTABLE)
                    .entity(ErrorReply.create(ErrorType.MISSING_REQUIRED_FIELD, "id field is required"))
                    .build();
        }

        if (!value.id.matches("[a-z0-9\\-]+")) {
            LOG.errorv("id has invalid characters, only alpha and dash is allowed: {0}", value.id);
            return Response.status(Status.NOT_ACCEPTABLE)
                    .entity(ErrorReply.create(ErrorType.INVALID_FIELD, "id field must contains a-z, 0-9 or -"))
                    .build();
        }

        boolean result = namedItemService.postCreate(value);
        if (!result) {
            return Response.status(Status.CONFLICT).build();
        }

        return Response.ok(value).build();
    }

    @PUT
    @Path("{id}")
    public Response putUpdate(@PathParam String id, T value) {
        if (!id.matches("[a-z0-9\\-]+")) {
            LOG.errorv("id has invalid characters, only alpha and dash is allowed: {0}", id);
            return Response.status(Status.NOT_ACCEPTABLE)
                    .entity(ErrorReply.create(ErrorType.INVALID_FIELD, "id field must contains a-z, 0-9 or -"))
                    .build();
        }

        if (value.id == null) {
            LOG.errorv("id within object is null", id);
            return Response.status(Status.NOT_ACCEPTABLE)
                    .entity(ErrorReply.create(ErrorType.INVALID_FIELD, "id must not be null"))
                    .build();
        }

        if (!id.equals(value.id)) {
            LOG.errorv("id from URL is not the same as id within object", id);
            return Response.status(Status.NOT_ACCEPTABLE)
                    .entity(ErrorReply.create(ErrorType.INVALID_FIELD, "id field is not the same as in the URL"))
                    .build();
        }

        NamedItem result = namedItemService.putUpdate(typeParameterClass, id, value);

        return Response.ok(result).build();
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam String id) {
        boolean result;

        try {
            result = namedItemService.delete(typeParameterClass, id);
        } catch (PersistenceException ex) {
            return Response.status(Status.NOT_ACCEPTABLE)
                    .entity(ErrorReply.create(ErrorType.CANT_DELETE_REFERENCED,
                            "Item id %s is referenced and can't be deleted",
                            id))
                    .build();
        }

        if (!result) {
            return Response.status(Status.NOT_FOUND)
                    .entity(ErrorReply.create(ErrorType.NOT_EXISTS, "No item with id %s", id))
                    .build();
        }

        return Response.ok().build();
    }
}
