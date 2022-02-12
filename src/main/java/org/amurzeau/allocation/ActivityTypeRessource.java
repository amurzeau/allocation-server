package org.amurzeau.allocation;

import javax.ws.rs.Path;

import org.amurzeau.allocation.rest.ActivityType;

@Path("/activity_type")
public class ActivityTypeRessource extends NamedItemRessource<ActivityType> {
    ActivityTypeRessource() {
        super(ActivityType.class);
    }
}
