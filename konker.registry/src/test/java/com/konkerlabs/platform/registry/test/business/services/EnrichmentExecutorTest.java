package com.konkerlabs.platform.registry.test.business.services;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.behaviors.DeviceURIDealer;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.services.api.EnrichmentExecutor;
import com.konkerlabs.platform.registry.business.services.api.NewServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.integration.exceptions.IntegrationException;
import com.konkerlabs.platform.registry.integration.gateways.HttpGateway;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.konkerlabs.platform.registry.test.base.RedisTestConfiguration;
import com.konkerlabs.platform.utilities.config.UtilitiesConfig;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        MongoTestConfiguration.class,
        UtilitiesConfig.class,
        BusinessTestConfiguration.class,
        RedisTestConfiguration.class
})
public class EnrichmentExecutorTest extends BusinessLayerTestSupport {

    private static final String REGISTERED_TENANT_DOMAIN = "konker";
    private static final String REGISTERED_TENANT_NAME = "Konker";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Autowired
    private EnrichmentExecutor subject;
    @Autowired
    private HttpGateway httpGateway;

    private Event event;
    private Device device;

    private URI existingUri;
    private URI nonExistingUri;
    private static final String EXISTING_DEVICE_ID = "abc123";
    private static final String EXISTING_DEVICE_GUID = "8d51c242-81db-11e6-a8c2-0746f010e945";
    private static final String BAD_CONFIGURATION_DEVICE_ID = "abc456";
    private static final String ENRICHMENT_INACTIVE_DEVICE_ID = "abc789";
    private static final String NON_EXISTING_DEVICE_ID = "999";
    private static final String NON_EXISTING_DEVICE_GUID = "44421842-7438-4c46-8bb2-5a2f56cd8923";

    private static final String PAYLOAD = "{\"metric\":\"temperature\",\"deviceId\":\"abc123\",\"value\":30,\"ts\":1454900000,\"prestashopData\":\"\"}";
    private static final String PAYLOAD_WITHOUT_CONTAINER_KEY = "{\"metric\":\"temperature\",\"deviceId\":\"abc123\",\"value\":30,\"ts\":1454900000}";
    private static final String PAYLOAD_PRE_POPULATED_CONTAINER_KEY = "{\"metric\":\"temperature\",\"deviceId\":\"abc123\",\"value\":30,\"ts\":1454900000,\"prestashopData\":\"abc\"}";
    private static final String ENRICHED_PAYLOAD = "{\"metric\":\"temperature\",\"deviceId\":\"abc123\",\"value\":30,\"ts\":1454900000," +
            "\"prestashopData\":{\"product\":{\"SKU\":123,\"description\":\"20LBonafontWaterBottle\"},\"quantity\":1,\"storeUser\":\"johnsmith@nowhere.com\"}," +
            "\"magentoData\":{\"product\":{\"SKU\":123,\"description\":\"20LBonafontWaterBottle\"},\"quantity\":1,\"storeUser\":\"johnsmith@nowhere.com\"}}";

    private static final String ENRICHMENT_SERVICE_RESULT = "{\"product\":{\"SKU\":123,\"description\":\"20LBonafontWaterBottle\"},\"quantity\":1,\"storeUser\":\"johnsmith@nowhere.com\"}";

    @Before
    public void setUp() throws Exception {
        event = spy(Event.builder().channel("data").timestamp(Instant.now()).payload(PAYLOAD).build());

        existingUri = new DeviceURIDealer() {
        }.toDeviceRouteURI(REGISTERED_TENANT_DOMAIN, EXISTING_DEVICE_GUID);
        nonExistingUri = new DeviceURIDealer() {
        }.toDeviceRouteURI(REGISTERED_TENANT_DOMAIN, NON_EXISTING_DEVICE_GUID);

        device = spy(Device.builder()
                .tenant(
                        Tenant.builder()
                                .domainName(REGISTERED_TENANT_DOMAIN)
                                .name(REGISTERED_TENANT_NAME)
                                .build()
                )
                .apiKey("84399b2e-d99e-11e5-86bc-34238775bac9")
                .id("id")
                .deviceId(EXISTING_DEVICE_ID)
                .guid(EXISTING_DEVICE_GUID)
                .active(true)
                .name("device_name").build());

        when(httpGateway.request(eq(HttpMethod.GET), eq(new URI("https://www.google.com/device/abc123")),
                eq(MediaType.APPLICATION_JSON),
                eq(null), eq("user"), eq("pass"))).thenReturn(ENRICHMENT_SERVICE_RESULT);
    }

    @Test
    public void shouldReturnErrorMessageIfEventIsNull() {
        NewServiceResponse<Event> response = subject.enrich(null, device);
        assertThat(response.getStatus(), equalTo(NewServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(), hasEntry(EnrichmentExecutor.Validations.INCOMING_EVENT_NULL.getCode(),null));
    }

    @Test
    public void shouldReturnErrorMessageIfDeviceIsNull() {
        NewServiceResponse<Event> response = subject.enrich(event, null);
        assertThat(response.getStatus(), equalTo(NewServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(), hasEntry(EnrichmentExecutor.Validations.INCOMING_DEVICE_NULL.getCode(),null));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json"})
    public void shouldReturnErrorMessageIfTenantDoesNotExists() {
        device.getTenant().setName("999");

        NewServiceResponse<Event> response = subject.enrich(event, device);
        assertThat(response.getStatus(), equalTo(NewServiceResponse.Status.ERROR));
        assertThat(response.getResult(), nullValue());
        assertThat(response.getResponseMessages(), hasEntry(CommonValidations.TENANT_DOES_NOT_EXIST.getCode(),null));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json"})
    public void shouldNotChangeTheIncomingEventIfThereIsNoEnrichmentRegistered() {
        device.setDeviceId(NON_EXISTING_DEVICE_ID);

        NewServiceResponse<Event> response = subject.enrich(event, device);
        assertThat(response.getStatus(), equalTo(NewServiceResponse.Status.OK));
        assertThat(response.getResult(), notNullValue());
        assertThat(response.getResult(), equalTo(event));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/enrichment-rest.json"})
    public void shouldNotChangeTheIncomingEventIfThereIsNoContainerKeyInEventPayload() {
        event.setPayload(PAYLOAD_WITHOUT_CONTAINER_KEY);

        NewServiceResponse<Event> response = subject.enrich(event, device);
        assertThat(response.getStatus(), equalTo(NewServiceResponse.Status.OK));
        assertThat(response.getResult(), notNullValue());
        assertThat(response.getResult(), equalTo(event));
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/enrichment-rest.json"})
    public void shouldOverwriteThePrePopulated() throws URISyntaxException, IntegrationException {
        event.setPayload(PAYLOAD_PRE_POPULATED_CONTAINER_KEY);
        String originalPayload = new String(event.getPayload());

        NewServiceResponse<Event> response = subject.enrich(event, device);
        assertThat(response.getStatus(), equalTo(NewServiceResponse.Status.OK));
        assertThat(response.getResult(), notNullValue());
        assertNotEquals(originalPayload, response.getResult().getPayload());
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/enrichment-rest.json"})
    public void shouldEnrichSuccessfully() {
        NewServiceResponse<Event> response = subject.enrich(event, device);
        assertThat(response.getStatus(), equalTo(NewServiceResponse.Status.OK));
        assertThat(response.getResult(), notNullValue());
        assertEquals(ENRICHED_PAYLOAD, response.getResult().getPayload());
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json", "/fixtures/enrichment-rest.json"})
    public void shouldNotEnrichBecauseOfEnrichmentInactive() {
        device.setDeviceId(ENRICHMENT_INACTIVE_DEVICE_ID);

        NewServiceResponse<Event> response = subject.enrich(event, device);
        assertThat(response.getStatus(), equalTo(NewServiceResponse.Status.OK));
        assertThat(response.getResult(), notNullValue());
        assertEquals(event.getPayload(), response.getResult().getPayload());
    }
}
