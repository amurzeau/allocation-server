package org.amurzeau.allocation;

import java.math.BigInteger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.amurzeau.allocation.rest.User;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

@Path("/user")
public class UserRessource {
    @GET
    public Multi<User> getAll() {
        return User.stream("isDeleted", false);
    }

    @GET
    @Path("{id}")
    public Uni<User> getById(BigInteger id) {
        return User.find("id = ? and isDeleted = false", id).firstResult();
    }
}
