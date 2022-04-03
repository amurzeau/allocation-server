package org.amurzeau.allocation;

import java.math.BigInteger;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.amurzeau.allocation.rest.User;

@Path("/user")
public class UserRessource {
    @GET
    public List<User> getAll() {
        return User.find("isDeleted", false).list();
    }

    @GET
    @Path("{id}")
    public User getById(BigInteger id) {
        return User.find("id = ? and isDeleted = false", id).firstResult();
    }
}
