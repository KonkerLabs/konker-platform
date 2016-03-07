package com.konkerlabs.platform.registry.test.business.services;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.EventRoute;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.behaviors.DeviceURIDealer;
import com.konkerlabs.platform.registry.business.repositories.EventRouteRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.routes.EventRouteExecutorImpl;
import com.konkerlabs.platform.registry.business.services.routes.api.EventRouteService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
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

import java.util.Arrays;
import java.util.List;

import static com.konkerlabs.platform.registry.business.model.EventRoute.*;
import static com.konkerlabs.platform.registry.business.services.api.ServiceResponse.Status;
import static com.konkerlabs.platform.registry.business.services.api.ServiceResponse.Status.ERROR;
import static com.konkerlabs.platform.registry.business.services.api.ServiceResponse.Status.OK;
import static com.konkerlabs.platform.registry.business.services.routes.EventRouteExecutorImpl.RuleTransformationType;
import static com.konkerlabs.platform.registry.business.services.routes.EventRouteExecutorImpl.RuleTransformationType.EXPRESSION_LANGUAGE;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MongoTestConfiguration.class, BusinessTestConfiguration.class})
public class EventRouteServiceTest extends BusinessLayerTestSupport {

    @Rule
    public ExpectedException thrown = none();

    @Autowired
    private EventRouteService subject;

    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private EventRouteRepository eventRouteRepository;

    private EventRoute route;

    private String routeId = "71fb0d48-674b-4f64-a3e5-0256ff3a63af";
    private Tenant tenant;
    private Tenant emptyTenant;

    @Before
    public void setUp() throws Exception {
        tenant = tenantRepository.findByDomainName("konker");
        emptyTenant = tenantRepository.findByDomainName("empty");

        route = spy(builder()
                .name("Route name")
                .description("Description")
                .incoming(new RuleActor(
                        new DeviceURIDealer() {
                        }.toDeviceRouteURI(tenant.getDomainName(), "0000000000000004")
                ))
                .outgoing(new RuleActor(
                        new DeviceURIDealer() {
                        }.toDeviceRouteURI(tenant.getDomainName(), "0000000000000005")
                ))
                .transformations(asList(new RuleTransformation[]{
                        new RuleTransformation(EXPRESSION_LANGUAGE.name())
                }))
                .active(true)
                .build());
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json"})
    public void shouldRaiseAnExceptionIfTenantIsNull() throws Exception {
        thrown.expect(BusinessException.class);
        thrown.expectMessage("Tenant cannot be null");

        subject.save(null, route);
    }

    @Test
    @UsingDataSet(locations = "/fixtures/tenants.json")
    public void shouldRaiseAnExceptionIfRecordIsNull() throws Exception {
        thrown.expect(BusinessException.class);
        thrown.expectMessage("Record cannot be null");

        subject.save(tenant, null);
    }

    @Test
    @UsingDataSet(locations = "/fixtures/tenants.json")
    public void shouldReturnResponseMessagesIfRecordIsInvalid() throws Exception {
        List<String> errorMessages = asList(new String[]{"Some error"});
        when(route.applyValidations()).thenReturn(errorMessages);

        ServiceResponse<EventRoute> response = subject.save(tenant, route);

        assertThat(response, notNullValue());
        assertThat(response.getStatus(), equalTo(ERROR));
        assertThat(response.getResponseMessages(), equalTo(errorMessages));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json"})
    public void shouldRaiseAnExceptionIfTenantDoesNotExist() throws Exception {
        thrown.expect(BusinessException.class);
        thrown.expectMessage("Tenant does not exist");

        subject.save(Tenant.builder().id("unknown_id").name("name").build(), route);
    }

    @Test
    @UsingDataSet(locations = "/fixtures/tenants.json")
    public void shouldPersistIfRouteIsValid() throws Exception {

        ServiceResponse<EventRoute> response = subject.save(tenant, route);

        assertThat(response, notNullValue());
        assertThat(response.getStatus(), equalTo(OK));
        assertThat(eventRouteRepository.findByIncomingURI(route.getIncoming().getUri()), notNullValue());
        assertThat(response.getResult().getIncoming().getUri(), equalTo(route.getIncoming().getUri()));
    }

    //TODO Verify this constraint for effectiveness
//    @Test
//    @UsingDataSet(locations = "/fixtures/tenants.json")
//    public void shouldReturnAValidationMessageIfIncomingAndOutgoingChannelsAreTheSame() throws Exception {
//        String channel = "channel";
//
//        route.getIncoming().getData().put("channel",channel);
//        route.getOutgoing().getData().put("channel",channel);
//
//        List<String> errorMessages = Arrays.asList(new String[] { "Incoming and outgoing device channels cannot be the same" });
//        ServiceResponse response = subject.save(tenant,route);
//
//        assertThat(response, notNullValue());
//        assertThat(response.getStatus(), equalTo(ServiceResponse.Status.ERROR));
//        assertThat(response.getResponseMessages(), equalTo(errorMessages));
//    }
    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/event-routes.json"})
    public void shouldReturnAllRegisteredRoutesWithinATenant() throws Exception {
        List<EventRoute> allRoutes = subject.getAll(tenant);

        assertThat(allRoutes, notNullValue());
        assertThat(allRoutes, hasSize(8));
        assertThat(allRoutes.get(0).getId(), equalTo("71fb0d48-674b-4f64-a3e5-0256ff3a63af"));
        assertThat(allRoutes.get(1).getId(), equalTo("71fb0d48-674b-4f64-a3e5-0256ff3a63ab"));
        assertThat(allRoutes.get(2).getId(), equalTo("71fb0d48-674b-4f64-a3e5-0256ff3a63ac"));
        assertThat(allRoutes.get(3).getId(), equalTo("71fb0d48-674b-4f64-a3e5-0256ff3a63ad"));
        assertThat(allRoutes.get(4).getId(), equalTo("71fb0d48-674b-4f64-a3e5-0256ff3a63ae"));
        assertThat(allRoutes.get(5).getId(), equalTo("71fb0d48-674b-4f64-a3e5-0256ff3a63ba"));
        assertThat(allRoutes.get(6).getId(), equalTo("71fb0d48-674b-4f64-a3e5-0256ff3a63bb"));
        assertThat(allRoutes.get(7).getId(), equalTo("71fb0d48-674b-4f64-a3e5-0256ff3a63bc"));

        allRoutes = subject.getAll(emptyTenant);
        assertThat(allRoutes, notNullValue());
        assertThat(allRoutes, empty());
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/event-routes.json"})
    public void shouldReturnARegisteredRouteByItsID() throws Exception {
        EventRoute route = subject.getById(tenant, routeId).getResult();

        assertThat(route, notNullValue());
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/event-routes.json"})
    public void shouldReturnARegisteredRouteByItsIncomingUri() throws Exception {
        List<EventRoute> routes = subject.findByIncomingUri(route.getIncoming().getUri());

        assertThat(routes, notNullValue());
        assertThat(routes, hasSize(5));
        assertThat(routes.get(0).getId(), equalTo("71fb0d48-674b-4f64-a3e5-0256ff3a63af"));
        assertThat(routes.get(1).getId(), equalTo("71fb0d48-674b-4f64-a3e5-0256ff3a63ab"));
        assertThat(routes.get(2).getId(), equalTo("71fb0d48-674b-4f64-a3e5-0256ff3a63ac"));
        assertThat(routes.get(3).getId(), equalTo("71fb0d48-674b-4f64-a3e5-0256ff3a63ae"));
        assertThat(routes.get(4).getId(), equalTo("71fb0d48-674b-4f64-a3e5-0256ff3a63ba"));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/event-routes.json"})
    public void shouldSaveEditedRouteState() throws Exception {
        EventRoute route = subject.getById(tenant, routeId).getResult();

        String editedName = "Edited name";
        route.setName(editedName);
        route.setActive(false);

        ServiceResponse<EventRoute> response = subject.save(tenant, route);

        assertThat(response, notNullValue());
        assertThat(response.getStatus(), equalTo(OK));
        assertThat(EventRoute.class.cast(response.getResult()).getName(), equalTo(editedName));
        assertThat(EventRoute.class.cast(response.getResult()).isActive(), equalTo(false));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/event-routes.json"})
    public void shouldReturnErrorMessageIfRouteDoesNotBelongToTenantWhenFindById() throws Exception {
        ServiceResponse<EventRoute> response = subject.getById(emptyTenant, routeId);

        assertThat(response, notNullValue());
        assertThat(response.getStatus(), equalTo(ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(), hasItem("Event Route does not exist"));
    }

}
