package com.konkerlabs.platform.registry.test.base;

import java.time.Instant;
import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Repository;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.repositories.events.api.BaseEventRepositoryImpl;
import com.konkerlabs.platform.registry.config.MongoConfig;

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
        protected List<Event> doFindBy(Tenant tenant, Application application, String deviceGuid, String locationGuid, String channel, Instant startInstant, Instant endInstant, boolean ascending, Integer limit, Type incoming, boolean b) throws BusinessException {
            return null;
        }

		@Override
		protected void doRemoveBy(Tenant tenant, Application application, String deviceGuid, List<Event> events, Type incoming) throws Exception {
						
		}

    }

}