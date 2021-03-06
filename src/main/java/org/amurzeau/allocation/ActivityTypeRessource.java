package org.amurzeau.allocation;

import javax.ws.rs.Path;

import org.amurzeau.allocation.rest.ActivityType;

@Path("/activity-types")
public class ActivityTypeRessource extends NamedItemRessource<ActivityType> {
    public ActivityTypeRessource() {
        super(ActivityType.class);
    }
}
