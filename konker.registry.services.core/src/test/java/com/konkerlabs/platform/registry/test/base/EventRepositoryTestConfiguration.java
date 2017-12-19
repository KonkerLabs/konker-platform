package com.konkerlabs.platform.registry.test.base;

import com.github.fakemongo.Fongo;
import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.repositories.events.api.BaseEventRepositoryImpl;
import com.konkerlabs.platform.registry.business.repositories.events.api.EventRepository;
import com.konkerlabs.platform.registry.config.MongoConfig;
import com.mongodb.Mongo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Configuration
public class EventRepositoryTestConfiguration extends MongoConfig {

    @Repository("mongoEvents")
    public class EventRepositoryMongoImpl extends BaseEventRepositoryImpl {

        @Override
        protected Event doSave(Tenant tenant, Application application, Event event, Type incoming) throws BusinessException {
            return null;
        }

        @Override
        protected void doRemoveBy(Tenant tenant, Application application, String deviceGuid, Type incoming) throws Exception {

        }

        @Override
        protected List<Event> doFindBy(Tenant tenant, Application application, String deviceGuid, String channel, Instant startInstant, Instant endInstant, boolean ascending, Integer limit, Type incoming, boolean b) throws BusinessException {
            return null;
        }

    }

}