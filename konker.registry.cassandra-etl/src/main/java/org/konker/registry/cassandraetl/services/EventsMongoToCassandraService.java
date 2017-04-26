package org.konker.registry.cassandraetl.services;

import java.time.Instant;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.repositories.ApplicationRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.repositories.events.EventRepositoryMongoImpl;
import com.konkerlabs.platform.registry.business.repositories.events.api.EventRepository;

@Service
public class EventsMongoToCassandraService {

    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private EventRepositoryMongoImpl mongoEventsRepository;

    public void migrate(String tenantDomainFilter, Instant startInstant) throws BusinessException {

        LOGGER.info("Starting...");

        int count = 0;

        List<Tenant> tenants = tenantRepository.findAll();

        Pattern filterPattern = Pattern.compile(tenantDomainFilter);

        for (Tenant tenant : tenants) {
            if (filterPattern.matcher(tenant.getDomainName()).matches()) {
                process(tenant, startInstant);
                count++;
            }
        }

        LOGGER.info("Finished! Tenants processed: " + count);

    }

    private void process(Tenant tenant, Instant startInstant) throws BusinessException {

        List<Application> applications = applicationRepository.findAllByTenant(tenant.getId());
        for (Application application : applications) {
            process(tenant, application, null);
        }

    }

    private void process(Tenant tenant, Application application, Instant startInstant) throws BusinessException {

        List<Event> incomingEvents = mongoEventsRepository.findIncomingBy(tenant, application, null, null, startInstant, null, true, null);

        LOGGER.info("Tenant {}", tenant.getName());

    }

}
