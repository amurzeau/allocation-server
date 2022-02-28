package org.amurzeau.allocation;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceException;
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

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

@Path("/projects")
public class ProjectRessource {
    private static final Logger LOG = Logger.getLogger(ProjectRessource.class);

    @Inject
    NamedItemService namedItemService;

    @GET
    public Uni<List<ProjectReply>> getAll() {
        return ProjectReply.<ProjectReply>findAll().list().invoke((List<ProjectReply> l) -> {
            for (ProjectReply projectReply : l) {
                LOG.infov("Item: {0}: {1}", projectReply.id, projectReply.name);
            }
        });
    }

    @GET
    @Path("{id}")
    public Uni<Response> getById(Long id) {
        return ProjectReply.findById(id).onItem().transform(project -> {
            if (project != null) {
                return Response.ok(project).build();
            } else {
                return Response
                        .status(Status.NOT_FOUND)
                        .entity(ErrorReply.create(ErrorType.NOT_EXISTS, "No project with id %s", id))
                        .build();
            }
        });
    }

    private Uni<Void> fetchEotps(List<String> eotps, Set<Eotp> eotpList) {
        Uni<Void> uni;

        if (eotps != null) {
            uni = Multi.createFrom().iterable(eotps)
                    .onItem().transformToUniAndConcatenate(eotpId -> {
                        return namedItemService.getById(Eotp.class, eotpId).invoke(eotp -> {
                            if (eotp != null)
                                eotpList.add(eotp);
                        }).replaceWith(true);
                    })
                    .onItem().ignoreAsUni();
        } else {
            uni = Uni.createFrom().nullItem();
        }

        return uni;
    }

    private Uni<ProjectReply> createOrUpdateProject(ProjectReply project, ProjectUpdate value) {
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

        Uni<?> projectTypeUni = namedItemService.getById(ApplicationType.class, value.type).invoke(v -> {
            project.type = v;
        });

        Uni<Void> eotpOpenUni = fetchEotps(value.eotpOpen, project.eotpOpen);
        Uni<Void> eotpClosedUni = fetchEotps(value.eotpClosed, project.eotpClosed);

        return Uni.combine().all().unis(projectTypeUni, eotpOpenUni, eotpClosedUni)
                .discardItems()
                .replaceWith(project)
                .onItem().<ProjectReply>transformToUni(item -> {
                    return item.<ProjectReply>persist()
                            .invoke((persistedItem) -> {
                                LOG.infov("Creating new item {0} with name {1}", persistedItem.id,
                                        persistedItem.name);
                            });
                });
    }

    @POST
    public Uni<Response> postNew(ProjectUpdate value) {
        return Panache.withTransaction(() -> {
            ProjectReply project = new ProjectReply();
            return createOrUpdateProject(project, value);
        }).map((item) -> {
            return Response.ok(item).build();
        });
    }

    @PUT
    @Path("{id}")
    public Uni<Response> putUpdate(Long id, ProjectUpdate value) {
        return Panache.withTransaction(() -> {
            Uni<ProjectReply> var = ProjectReply.findById(id, LockModeType.PESSIMISTIC_WRITE);
            return var
                    .onItem().<ProjectReply>transformToUni(item -> {
                        if (item == null) {
                            LOG.infov("No project with id {0}", id);
                            return Uni.createFrom().nullItem();
                        }
                        return createOrUpdateProject(item, value);
                    });
        }).onItem().transform(res -> {
            if (res != null) {
                return Response.ok(res).build();
            } else {
                return Response
                        .status(Status.NOT_FOUND)
                        .entity(ErrorReply.create(ErrorType.NOT_EXISTS, "No project with id %s", id))
                        .build();
            }
        });
    }

    @DELETE
    @Path("{id}")
    public Uni<Response> delete(Long id) {
        return Panache.withTransaction(() -> {
            Uni<ProjectReply> var = ProjectReply.findById(id, LockModeType.PESSIMISTIC_WRITE);
            return var
                    .onItem().<Boolean>transformToUni(item -> {
                        if (item != null)
                            return item.delete().replaceWith(true);
                        else
                            return Uni.createFrom().item(false);
                    });
        }).onItem().transform(res -> {
            if (res) {
                return Response.ok(res).build();
            } else {
                return Response
                        .status(Status.NOT_FOUND)
                        .entity(ErrorReply.create(ErrorType.NOT_EXISTS, "No project with id %s", id))
                        .build();
            }
        }).onFailure(PersistenceException.class).recoverWithItem(res -> {
            return Response.status(Status.NOT_ACCEPTABLE)
                    .entity(ErrorReply.create(ErrorType.CANT_DELETE_REFERENCED,
                            "Project id %s is referenced and can't be deleted",
                            id))
                    .build();
        });
    }
}
