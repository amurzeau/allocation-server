package org.amurzeau.allocation;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.LockModeType;
import javax.transaction.Transactional;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.amurzeau.allocation.rest.ActivityType;
import org.amurzeau.allocation.rest.AllocationReply;
import org.amurzeau.allocation.rest.AllocationUpdate;
import org.amurzeau.allocation.rest.ErrorReply;
import org.amurzeau.allocation.rest.ErrorType;
import org.amurzeau.allocation.rest.ProjectReply;
import org.amurzeau.allocation.services.NamedItemService;
import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

@Path("/allocations")
public class AllocationRessource {
    private static final Logger LOG = Logger.getLogger(AllocationRessource.class);

    @Inject
    ProjectRessource projectRessource;

    @Inject
    NamedItemService namedItemService;

    @GET
    public List<AllocationReply> getAll() {
        List<AllocationReply> results = AllocationReply.<AllocationReply>findAll().list();

        for (AllocationReply projectReply : results) {
            LOG.infov("Item: {0}: {1}", projectReply.id,
                    projectReply.project != null ? projectReply.project.name : "");
        }

        return results;
    }

    @GET
    @Path("{id}")
    public Response getById(@PathParam Long id) {
        AllocationReply result = AllocationReply.findById(id);

        if (result == null) {
            return Response
                    .status(Status.NOT_FOUND)
                    .entity(ErrorReply.create(ErrorType.NOT_EXISTS, "No allocation with id %s", id))
                    .build();
        }

        return Response.ok(result).build();
    }

    private AllocationReply createOrUpdateProject(AllocationReply replyItem, AllocationUpdate value) {
        replyItem.duration = value.duration;

        if (value.projectId != null)
            replyItem.project = ProjectReply.<ProjectReply>findById(value.projectId);

        if (value.activityTypeId != null)
            replyItem.activityType = namedItemService.getById(ActivityType.class, value.activityTypeId);

        replyItem.persist();

        LOG.infov("Creating new item {0} with project name {1}",
                replyItem.id,
                replyItem.project != null ? replyItem.project.name : "");

        return replyItem;
    }

    @POST
    @Transactional
    public Response postNew(AllocationUpdate value) {
        AllocationReply project = new AllocationReply();
        project = createOrUpdateProject(project, value);

        return Response.ok(project).build();
    }

    @PUT
    @Path("{id}")
    @Transactional
    public Response putUpdate(@PathParam Long id, AllocationUpdate value) {
        AllocationReply var = AllocationReply.findById(id, LockModeType.PESSIMISTIC_WRITE);

        if (var == null) {
            LOG.infov("No allocation with id {0}", id);
            return Response
                    .status(Status.NOT_FOUND)
                    .entity(ErrorReply.create(ErrorType.NOT_EXISTS, "No allocation with id %s", id))
                    .build();
        }

        var = createOrUpdateProject(var, value);
        return Response.ok(var).build();
    }

    @DELETE
    @Path("{id}")
    @Transactional
    public Response delete(@PathParam Long id) {
        boolean result = AllocationReply.deleteById(id);
        if (!result) {
            return Response
                    .status(Status.NOT_FOUND)
                    .entity(ErrorReply.create(ErrorType.NOT_EXISTS, "No allocation with id %s",
                            id))
                    .build();
        }

        return Response.ok().build();
    }
}
