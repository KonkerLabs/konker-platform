package org.konker.registry.cassandraetl.services;

import java.time.Instant;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.repositories.ApplicationRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.repositories.events.EventRepositoryCassandraImpl;
import com.konkerlabs.platform.registry.business.repositories.events.EventRepositoryMongoImpl;

@Service
public class EventsCassandraToMongoService {

    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private EventRepositoryMongoImpl mongoEventsRepository;

    @Autowired
    private EventRepositoryCassandraImpl cassandraEventsRepository;

    public void migrate(String tenantDomainFilter, Instant startInstant, Instant endInstant) throws BusinessException {

        LOGGER.info("Starting...");

        int count = 0;

        List<Tenant> tenants = tenantRepository.findAll();

        Pattern filterPattern = Pattern.compile(tenantDomainFilter);

        for (Tenant tenant : tenants) {
            if (filterPattern.matcher(tenant.getDomainName()).matches()) {
                process(tenant, startInstant, endInstant);
                count++;
            }
        }

        LOGGER.info("Finished! Tenants processed: " + count);

    }

    private void process(Tenant tenant, Instant startInstant, Instant endInstant) throws BusinessException {

        List<Application> applications = applicationRepository.findAllByTenant(tenant.getId());
        for (Application application : applications) {
            process(tenant, application, startInstant, endInstant);
        }

    }

    private void process(Tenant tenant, Application application, Instant startInstant, Instant endInstant) throws BusinessException {

        String deviceGuid = null;
        String channel = null;
        boolean ascending = false;
        Integer limit = null;

        LOGGER.info("Tenant {} Application {}", tenant.getName(), application.getName());

        List<Event> incomingEvents = cassandraEventsRepository.findIncomingBy(tenant, application, deviceGuid, channel, startInstant, endInstant, ascending, limit);
        LOGGER.info("\tIncoming events: {}", incomingEvents.size());

        for (Event event : incomingEvents) {
            mongoEventsRepository.saveIncoming(tenant, application, event);
        }

        if (incomingEvents.size() > 0) {
            LOGGER.info("\tCompleted!");
        }

        List<Event> outgoingEvents = cassandraEventsRepository.findOutgoingBy(tenant, application, deviceGuid, channel, startInstant, endInstant, ascending, limit);
        LOGGER.info("\tOutgoing events: {}", outgoingEvents.size());

        for (Event event : outgoingEvents) {
            mongoEventsRepository.saveOutgoing(tenant, application, event);
        }

        if (outgoingEvents.size() > 0) {
            LOGGER.info("\tCompleted!");
        }

    }

}
