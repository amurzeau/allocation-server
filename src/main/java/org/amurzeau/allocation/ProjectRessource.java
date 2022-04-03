package org.amurzeau.allocation;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceException;
import javax.transaction.Transactional;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.amurzeau.allocation.rest.ApplicationType;
import org.amurzeau.allocation.rest.Eotp;
import org.amurzeau.allocation.rest.ErrorReply;
import org.amurzeau.allocation.rest.ErrorType;
import org.amurzeau.allocation.rest.ProjectReply;
import org.amurzeau.allocation.rest.ProjectUpdate;
import org.amurzeau.allocation.services.NamedItemService;
import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

@Path("/projects")
public class ProjectRessource {
    private static final Logger LOG = Logger.getLogger(ProjectRessource.class);

    @Inject
    NamedItemService namedItemService;

    @GET
    public List<ProjectReply> getAll() {
        List<ProjectReply> results = ProjectReply.<ProjectReply>findAll().list();

        for (ProjectReply projectReply : results) {
            LOG.infov("Item: {0}: {1}", projectReply.id, projectReply.name);
        }

        return results;
    }

    @GET
    @Path("{id}")
    public Response getById(@PathParam Long id) {
        ProjectReply result = ProjectReply.findById(id);

        if (result == null) {
            return Response
                    .status(Status.NOT_FOUND)
                    .entity(ErrorReply.create(ErrorType.NOT_EXISTS, "No project with id %s", id))
                    .build();
        }

        return Response.ok(result).build();
    }

    private void fetchEotps(List<String> eotps, Set<Eotp> eotpList) {
        if (eotps != null) {
            for (String eotpId : eotps) {
                Eotp eotp = namedItemService.getById(Eotp.class, eotpId);
                if (eotp != null)
                    eotpList.add(eotp);
            }
        }
    }

    private ProjectReply createOrUpdateProject(ProjectReply project, ProjectUpdate value) {
        project.name = value.name;
        project.board = value.board;
        project.component = value.component;
        project.arch = value.arch;

        if (project.eotpOpen != null)
            project.eotpOpen.clear();
        else
            project.eotpOpen = new LinkedHashSet<>();

        if (project.eotpClosed != null)
            project.eotpClosed.clear();
        else
            project.eotpClosed = new LinkedHashSet<>();

        if (value.type != null)
            project.type = namedItemService.getById(ApplicationType.class, value.type);

        fetchEotps(value.eotpOpen, project.eotpOpen);
        fetchEotps(value.eotpClosed, project.eotpClosed);

        project.persist();
        LOG.infov("Creating new item {0} with name {1}",
                project.id,
                project.name);

        return project;
    }

    @POST
    @Transactional
    public Response postNew(ProjectUpdate value) {
        ProjectReply project = new ProjectReply();
        project = createOrUpdateProject(project, value);

        return Response.ok(project).build();
    }

    @PUT
    @Path("{id}")
    @Transactional
    public Response putUpdate(@PathParam Long id, ProjectUpdate value) {
        ProjectReply var = ProjectReply.findById(id, LockModeType.PESSIMISTIC_WRITE);
        if (var == null) {
            LOG.infov("No project with id {0}", id);
            return Response
                    .status(Status.NOT_FOUND)
                    .entity(ErrorReply.create(ErrorType.NOT_EXISTS, "No project with id %s", id))
                    .build();
        }
        var = createOrUpdateProject(var, value);

        return Response.ok(var).build();
    }

    @DELETE
    @Path("{id}")
    @Transactional
    public Response delete(@PathParam Long id) {
        boolean result;

        try {
            result = ProjectReply.deleteById(id);
            ProjectReply.flush(); // Exception is probably thrown here
        } catch (PersistenceException ex) {
            return Response.status(Status.NOT_ACCEPTABLE)
                    .entity(ErrorReply.create(ErrorType.CANT_DELETE_REFERENCED,
                            "Project id %s is referenced and can't be deleted",
                            id))
                    .build();
        }

        if (!result) {
            return Response
                    .status(Status.NOT_FOUND)
                    .entity(ErrorReply.create(ErrorType.NOT_EXISTS, "No project with id %s", id))
                    .build();
        }

        return Response.ok().build();
    }
}
