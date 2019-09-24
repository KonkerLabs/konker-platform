package com.konkerlabs.platform.registry.test.business.services;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.EventRoute;
import com.konkerlabs.platform.registry.business.model.EventRouteCounter;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.*;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.EventRouteCounterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.config.EventStorageConfig;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.hasErrorMessage;
import static com.konkerlabs.platform.registry.test.base.matchers.ServiceResponseMatchers.isResponseOk;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.rules.ExpectedException.none;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        BusinessTestConfiguration.class,
        MongoTestConfiguration.class,
        EventStorageConfig.class
})
public class EventRouteCounterServiceTest extends BusinessLayerTestSupport {

    @Rule
    public ExpectedException thrown = none();

    @Autowired
    private EventRouteCounterService eventRouteCounterService;

    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private EventRouteRepository eventRouteRepository;

    private EventRouteCounter eventRouteCounter;
    private EventRoute route;

    private final String routeId = "71fb0d48-674b-4f64-a3e5-0256ff3a63aa";
    private Tenant tenant;
    private Application application;

    @Before
    public void setUp() {
        tenant = tenantRepository.findByDomainName("konker");
        application = applicationRepository.findByTenantAndName(tenant.getId(), "konker");
        route = eventRouteRepository.findOne(routeId);
        eventRouteCounter = EventRouteCounter.builder()
                .eventRoute(route)
                .performedTimes(1l)
                .build();

    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/event-routes.json"})
    public void shouldTrySaveTenantIsNull() {
        ServiceResponse<EventRouteCounter> response = eventRouteCounterService.save(null, application, eventRouteCounter);

        assertThat(response, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/event-routes.json"})
    public void shouldTrySaveTenantNotExist() {
        ServiceResponse<EventRouteCounter> response = eventRouteCounterService.save(Tenant.builder().domainName("no-exist").build(),
                application,
                eventRouteCounter);

        assertThat(response, hasErrorMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/event-routes.json"})
    public void shouldTrySaveApplicationIsNull() {
        ServiceResponse<EventRouteCounter> response = eventRouteCounterService.save(tenant, null, eventRouteCounter);

        assertThat(response, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/event-routes.json"})
    public void shouldTrySaveApplicationNotFound() {
        ServiceResponse<EventRouteCounter> response = eventRouteCounterService.save(tenant,
                Application.builder().name("not-found").build(),
                eventRouteCounter);

        assertThat(response, hasErrorMessage(ApplicationService.Validations.APPLICATION_NOT_FOUND.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/event-routes.json"})
    public void shouldTrySaveEventRouteCounterNull() {
        ServiceResponse<EventRouteCounter> response = eventRouteCounterService.save(tenant,
                application,
                null);

        assertThat(response, hasErrorMessage(EventRouteCounterService.Validations.EVENT_ROUTE_COUNTER_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/event-routes.json"})
    public void shouldTrySaveEventRouteNull() {
        eventRouteCounter.setEventRoute(null);
        ServiceResponse<EventRouteCounter> response = eventRouteCounterService.save(tenant,
                application,
                eventRouteCounter);

        assertThat(response, hasErrorMessage(EventRouteCounterService.Validations.EVENT_ROUTE_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/event-routes.json"})
    public void shouldSave() {
        ServiceResponse<EventRouteCounter> response = eventRouteCounterService.save(tenant,
                application,
                eventRouteCounter);

        assertThat(response, isResponseOk());
        assertThat(response.getResult().getId(), notNullValue());
        assertThat(response.getResult().getCreationDate(), notNullValue());
        assertThat(response.getResult().getGuid(), notNullValue());
        assertThat(response.getResult().getEventRoute(), notNullValue());
        assertThat(response.getResult().getApplication(), notNullValue());
        assertThat(response.getResult().getTenant(), notNullValue());
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/event-routes.json"})
    public void shouldGetByEventRouteCreationDateTenantIsNull() {
        ServiceResponse<EventRouteCounter> response = eventRouteCounterService.getByEventRoute(
                null,
                application,
                route);

        assertThat(response, hasErrorMessage(CommonValidations.TENANT_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/event-routes.json"})
    public void shouldGetByEventRouteCreationDateTenantNotExist() {
        ServiceResponse<EventRouteCounter> response = eventRouteCounterService.getByEventRoute(
                Tenant.builder().domainName("no-exist").build(),
                application,
                route);

        assertThat(response, hasErrorMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/event-routes.json"})
    public void shouldGetByEventRouteCreationDateApplicationIsNull() {
        ServiceResponse<EventRouteCounter> response = eventRouteCounterService.getByEventRoute(
                tenant,
                null,
                route);

        assertThat(response, hasErrorMessage(ApplicationService.Validations.APPLICATION_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/event-routes.json"})
    public void shouldGetByEventRouteCreationDateApplicationNotFound() {
        ServiceResponse<EventRouteCounter> response = eventRouteCounterService.getByEventRoute(
                tenant,
                Application.builder().name("not-found").build(),
                route);

        assertThat(response, hasErrorMessage(ApplicationService.Validations.APPLICATION_NOT_FOUND.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/event-routes.json"})
    public void shouldGetByEventRouteCreationDateEventRouteNull() {
        ServiceResponse<EventRouteCounter> response = eventRouteCounterService.getByEventRoute(
                tenant,
                application,
                null);

        assertThat(response, hasErrorMessage(EventRouteCounterService.Validations.EVENT_ROUTE_NULL.getCode()));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/applications.json", "/fixtures/event-routes.json"})
    public void shouldGetByEventRoute() {
        EventRouteCounter eventRouteCounter = EventRouteCounter.builder()
                .performedTimes(1l)
                .eventRoute(route)
                .build();
        eventRouteCounterService.save(tenant, application, eventRouteCounter);

        ServiceResponse<EventRouteCounter> response = eventRouteCounterService.getByEventRoute(
                tenant,
                application,
                route);

        assertThat(response, isResponseOk());
        assertThat(response.getResult().getId(), notNullValue());
        assertThat(response.getResult().getCreationDate(), notNullValue());
        assertThat(response.getResult().getGuid(), notNullValue());
        assertThat(response.getResult().getEventRoute(), notNullValue());
        assertThat(response.getResult().getApplication(), notNullValue());
        assertThat(response.getResult().getTenant(), notNullValue());
    }

}
