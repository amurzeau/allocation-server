package org.amurzeau.allocation;

import javax.ws.rs.Path;

import org.amurzeau.allocation.rest.Eotp;

@Path("/eotps")
public class EotpRessource extends NamedItemRessource<Eotp> {
    EotpRessource() {
        super(Eotp.class);
    }
}
