package org.amurzeau.allocation.services;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import org.amurzeau.allocation.rest.SchemaVersion;
import org.jboss.logging.Logger;

import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class UserService {
    private static final Logger LOGGER = Logger.getLogger(UserService.class);

    public void onStart(@Observes StartupEvent ev) {
        LOGGER.info("The application is starting...");

        SchemaVersion item = SchemaVersion.<SchemaVersion>find("order by id DESC").firstResult();

        if (item == null)
            return;

        migrateDatabase(item.version);
    }

    public void migrateDatabase(long fromVersion) {
        LOGGER.infov("Migrating database from version {0}", fromVersion);
    }
}
