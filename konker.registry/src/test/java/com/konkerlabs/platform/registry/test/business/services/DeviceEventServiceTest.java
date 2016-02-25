package com.konkerlabs.platform.registry.test.business.services;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.repositories.DeviceRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
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

import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    MongoTestConfiguration.class,
    BusinessTestConfiguration.class
})
@UsingDataSet(locations = {"/fixtures/tenants.json","/fixtures/devices.json"})
public class DeviceEventServiceTest extends BusinessLayerTestSupport {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private DeviceEventService deviceEventService;
    @Autowired
    private DeviceRegisterService deviceRegisterService;

    private String id = "71fc0d48-674a-4d62-b3e5-0216abca63af";
    private String apiKey = "84399b2e-d99e-11e5-86bc-34238775bac9";
    private String payload = "payload";
    private String channel = MessageFormat.format("iot/{0}/data",apiKey);
    private Event event;
    private Device device;
    private Tenant tenant;

    @Before
    public void setUp() throws Exception {
        event = Event.builder().channel(channel).payload(payload).build();
        tenant = tenantRepository.findByName("Konker");
        device = deviceRegisterService.getById(tenant, id).getResult();
    }
    @Test
    public void shouldRaiseAnExceptionIfDeviceIsNull() throws Exception {
        thrown.expect(BusinessException.class);
        thrown.expectMessage("Device cannot be null");

        deviceEventService.logEvent(null, event);
    }
    @Test
    public void shouldRaiseAnExceptionIfEventIsNull() throws Exception {
        thrown.expect(BusinessException.class);
        thrown.expectMessage("Event cannot be null");

        deviceEventService.logEvent(device, null);
    }
    @Test
    public void shouldRaiseAnExceptionIfPayloadIsNull() throws Exception {
        event.setPayload(null);

        thrown.expect(BusinessException.class);
        thrown.expectMessage("Event payload cannot be null or empty");

        deviceEventService.logEvent(device, event);
    }
    @Test
    public void shouldRaiseAnExceptionIfEventTimestampIsAlreadySet() throws Exception {
        event.setTimestamp(Instant.now());

        thrown.expect(BusinessException.class);
        thrown.expectMessage("Event timestamp cannot be already set!");

        deviceEventService.logEvent(device, event);
    }
    @Test
    public void shouldRaiseAnExceptionIfPayloadIsEmpty() throws Exception {
        event.setPayload("");

        thrown.expect(BusinessException.class);
        thrown.expectMessage("Event payload cannot be null or empty");

        deviceEventService.logEvent(device, event);
    }
    @Test
    public void shouldRaiseAnExceptionIfDeviceDoesNotExist() throws Exception {
        apiKey = "unknownDevice";
        device.setApiKey(apiKey);

        thrown.expect(BusinessException.class);
        thrown.expectMessage(MessageFormat.format("Device with API Key [{0}] does not exist", apiKey));

        deviceEventService.logEvent(device, event);
    }
    @Test
    public void shouldLogFirstDeviceEvent() throws Exception {
        deviceEventService.logEvent(device, event);

        Device device = deviceRepository.findByApiKey(apiKey);

        assertThat(device,notNullValue());

        Event last = device.getLastEvent();

        assertThat(last,notNullValue());
        assertThat(last.getPayload(),equalTo(payload));

        long gap = Duration.between(last.getTimestamp(), Instant.now()).abs().getSeconds();

        assertThat(gap,not(greaterThan(60L)));
    }
}