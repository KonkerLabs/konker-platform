package org.konker.registry.cassandraetl;

import java.time.Instant;

import org.konker.registry.cassandraetl.services.EventsCassandraToMongoService;
import org.konker.registry.cassandraetl.services.EventsMongoToCassandraService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class EventsMigrationApplication implements CommandLineRunner {

    private static final String C2M = "c2m";

    private static final String M2C = "m2c";

    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ConfigurableApplicationContext configurableApplicationContext;

    @Autowired
    private EventsMongoToCassandraService eventsMongoToCassandraService;

    @Autowired
    private EventsCassandraToMongoService eventsCassandraToMongoService;

    public static void main(String[] args) {
        SpringApplication.run(EventsMigrationApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        Instant startInstant = Instant.ofEpochSecond(1262304000L);
        String tenantDomainFilter = ".*";
        String direction = null;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--timestamp") || args[i].equals("-t")) {
                startInstant = Instant.ofEpochSecond(Long.parseLong(args[i + 1]));

            }
            if (args[i].equals("--tenant") || args[i].equals("-o")) {
                tenantDomainFilter = args[i + 1];
            }
            if (args[i].equals("--mongo2cassandra") || args[i].equals("-m2c")) {
                direction = M2C;
            }
            if (args[i].equals("--cassandra2mongo") || args[i].equals("-c2m")) {
                direction = C2M;
            }
        }

        LOGGER.info("Filter events created after " + startInstant);

        if (direction.equals(M2C)) {
            eventsMongoToCassandraService.migrate(tenantDomainFilter, startInstant);
        } else if (direction.equals(C2M)) {
            eventsCassandraToMongoService.migrate(tenantDomainFilter, startInstant);
        } else {
            LOGGER.info("Migration direction (m2c, c2m) not setted.");
        }

        configurableApplicationContext.close();

    }

}
