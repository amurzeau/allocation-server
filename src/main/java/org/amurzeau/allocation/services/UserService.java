package org.amurzeau.allocation.services;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import org.amurzeau.allocation.rest.SchemaVersion;
import org.jboss.logging.Logger;

import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class UserService {
    private static final Logger LOGGER = Logger.getLogger(UserService.class);

    void onStart(@Observes StartupEvent ev) {
        LOGGER.info("The application is starting...");

        SchemaVersion.<SchemaVersion>find("order by id DESC LIMIT 1").firstResult()
                .onItem().transform(item -> {
                    if (item == null)
                        return (long) 0;
                    else
                        return item.version;
                })
                .onItem().transformToUni(this::migrateDatabase);
    }

    Uni<Void> migrateDatabase(long fromVersion) {
        LOGGER.infov("Migrating database from version {0}", fromVersion);
        return Uni.createFrom().nullItem();
    }
}
