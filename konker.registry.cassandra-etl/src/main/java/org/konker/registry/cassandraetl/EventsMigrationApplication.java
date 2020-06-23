package org.konker.registry.cassandraetl;

import java.time.Instant;

import org.konker.registry.cassandraetl.services.EqualizeCassandraTablesService;
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

    private static final String EC = "ec";

    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ConfigurableApplicationContext configurableApplicationContext;

    @Autowired
    private EventsMongoToCassandraService eventsMongoToCassandraService;

    @Autowired
    private EventsCassandraToMongoService eventsCassandraToMongoService;

    @Autowired
    private EqualizeCassandraTablesService equilizeCassandraTablesService;

    public static void main(String[] args) {
        SpringApplication.run(EventsMigrationApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        Instant startInstant = Instant.ofEpochSecond(1262304000L);
        Instant endInstant = null;
        String tenantDomainFilter = ".*";
        String direction = null;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--timestampStart") || args[i].equals("-ts")) {
                startInstant = Instant.ofEpochSecond(Long.parseLong(args[i + 1]));
            }
            if (args[i].equals("--timestampEnd") || args[i].equals("-te")) {
                endInstant = Instant.ofEpochSecond(Long.parseLong(args[i + 1]));
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
            if (args[i].equals("--equilizeCassandra") || args[i].equals("-ec")) {
                direction = EC;
            }
        }

        LOGGER.info("Filter events created after " + startInstant);
        if (endInstant != null) {
            LOGGER.info("Filter events created until " + endInstant);
        }

        if (M2C.equals(direction)) {
            eventsMongoToCassandraService.migrate(tenantDomainFilter, startInstant, endInstant);
        } else if (C2M.equals(direction)) {
            eventsCassandraToMongoService.migrate(tenantDomainFilter, startInstant, endInstant);
        } else if (EC.equals(direction)) {
            equilizeCassandraTablesService.equilize();
        } else {
            LOGGER.info("Migration direction (m2c, c2m, ec) not setted.");
        }

        configurableApplicationContext.close();

    }

}
