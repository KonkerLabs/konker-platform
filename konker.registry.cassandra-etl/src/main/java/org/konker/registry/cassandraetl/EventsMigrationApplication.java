package org.konker.registry.cassandraetl;

import java.time.Instant;

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

    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ConfigurableApplicationContext configurableApplicationContext;

    @Autowired
    private EventsMongoToCassandraService eventsMongoToCassandraService;

    public static void main(String[] args) {
        SpringApplication.run(EventsMigrationApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        Instant startInstant = null;
        String tenantDomainFilter = ".*";

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--timestamp") || args[i].equals("--t")) {
                startInstant = Instant.ofEpochSecond(Long.parseLong(args[i + 1]));
                LOGGER.info("Start filter: " + startInstant);
            }
            if (args[i].equals("--tenant") || args[i].equals("--o")) {
                tenantDomainFilter = args[i + 1];
            }
        }

        eventsMongoToCassandraService.migrate(tenantDomainFilter, startInstant);

        configurableApplicationContext.close();

    }

}
