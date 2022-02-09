package org.amurzeau.allocation;

import javax.ws.rs.Path;

import org.amurzeau.allocation.rest.Eotp;

@Path("/eotp")
public class EotpRessource extends NamedItemRessource<Eotp> {
    EotpRessource() {
        super(Eotp.class);
    }
}