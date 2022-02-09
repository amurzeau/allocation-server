package org.amurzeau.allocation;

import javax.ws.rs.Path;

import org.amurzeau.allocation.rest.ApplicationType;

@Path("/application_type")
public class ApplicationTypeResource extends NamedItemRessource<ApplicationType> {
    ApplicationTypeResource() {
        super(ApplicationType.class);
    }
}