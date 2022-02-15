package org.amurzeau.allocation;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.LockModeType;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.amurzeau.allocation.rest.AllocationReply;
import org.amurzeau.allocation.rest.AllocationUpdate;
import org.jboss.logging.Logger;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;

@Path("/allocations")
public class AllocationRessource {
    private static final Logger LOG = Logger.getLogger(AllocationRessource.class);

    @Inject
    ActivityTypeRessource activityTypeRessource;
    @Inject
    ProjectRessource projectRessource;

    @GET
    public Uni<List<AllocationReply>> getAll() {
        return AllocationReply.<AllocationReply>findAll().list().invoke((List<AllocationReply> l) -> {
            for (AllocationReply projectReply : l) {
                LOG.infov("Item: {0}: {1}", projectReply.id,
                        projectReply.project != null ? projectReply.project.name : "");
            }
        });
    }

    @GET
    @Path("{id}")
    public Uni<AllocationReply> getById(Long id) {
        return AllocationReply.findById(id);
    }

    private Uni<AllocationReply> createOrUpdateProject(AllocationReply replyItem, AllocationUpdate value) {
        replyItem.duration = value.duration;

        Uni<?> projectUni = projectRessource.getById(value.projectId).invoke(v -> {
            replyItem.project = v;
        });

        Uni<?> activityTypeUni = activityTypeRessource.getById(value.activityTypeId).invoke(v -> {
            replyItem.activityType = v;
        });

        return Uni.combine().all().unis(projectUni, activityTypeUni)
                .discardItems()
                .replaceWith(replyItem)
                .onItem().<AllocationReply>transformToUni(item -> {
                    return item.<AllocationReply>persist()
                            .invoke((persistedItem) -> {
                                LOG.infov("Creating new item {0} with project name {1}",
                                        persistedItem.id,
                                        persistedItem.project != null ? persistedItem.project.name : "");
                            });
                });
    }

    @POST
    public Uni<Response> postNew(AllocationUpdate value) {
        return Panache.withTransaction(() -> {
            AllocationReply project = new AllocationReply();
            return createOrUpdateProject(project, value);
        }).map((item) -> {
            return Response.ok(item).build();
        });
    }

    @PUT
    @Path("{id}")
    public Uni<Response> putUpdate(Long id, AllocationUpdate value) {
        return Panache.withTransaction(() -> {
            Uni<AllocationReply> var = AllocationReply.findById(id, LockModeType.PESSIMISTIC_WRITE);
            return var
                    .onItem().<AllocationReply>transformToUni(item -> {
                        if (item == null) {
                            LOG.infov("No project with id {0}", id);
                            return Uni.createFrom().item(null);
                        }
                        return createOrUpdateProject(item, value);
                    })
                    .onFailure().invoke((e) -> {
                        LOG.errorv("Failure: {0}", e);
                    });
        }).onItem().transform(res -> {
            if (res != null) {
                return Response.ok(res).build();
            } else {
                return Response
                        .status(Status.NOT_FOUND)
                        .entity(String.format("No project with id %s", id))
                        .build();
            }
        });
    }

    @DELETE
    @Path("{id}")
    public Uni<Response> delete(Long id) {
        return Panache.withTransaction(() -> {
            Uni<AllocationReply> var = AllocationReply.findById(id, LockModeType.PESSIMISTIC_WRITE);
            return var
                    .onItem().<Boolean>transformToUni(item -> {
                        if (item != null)
                            return item.delete().replaceWith(true);
                        else
                            return Uni.createFrom().item(false);
                    })
                    .onFailure().invoke((e) -> {
                        LOG.errorv("Failure: {0}", e);
                    });
        }).onItem().transform(res -> {
            if (res) {
                return Response.ok(res).build();
            } else {
                return Response
                        .status(Status.NOT_FOUND)
                        .entity(String.format("No project with id %s", id))
                        .build();
            }
        });
    }
}
