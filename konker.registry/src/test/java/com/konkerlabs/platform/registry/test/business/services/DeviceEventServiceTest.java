package com.konkerlabs.platform.registry.test.business.services;

import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.repositories.DeviceRepository;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
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
public class DeviceEventServiceTest extends BusinessLayerTestSupport {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private DeviceEventService deviceEventService;

    private String deviceId = "95c14b36ba2b43f1";
    private String payload = "payload";

    @Test
    public void shouldRaiseAnExceptionIfPayloadIsNull() throws Exception {
        thrown.expect(BusinessException.class);
        thrown.expectMessage("Payload cannot be null or empty");

        deviceEventService.logEvent(null,deviceId);
    }
    @Test
    public void shouldRaiseAnExceptionIfPayloadIsEmpty() throws Exception {
        thrown.expect(BusinessException.class);
        thrown.expectMessage("Payload cannot be null or empty");

        deviceEventService.logEvent("",deviceId);
    }
    @Test
    public void shouldRaiseAnExceptionIfDeviceDoesNotExist() throws Exception {
        deviceId = "unknownDevice";

        thrown.expect(BusinessException.class);
        thrown.expectMessage(MessageFormat.format("Device ID [{0}] does not exist",deviceId));

        deviceEventService.logEvent(payload,deviceId);
    }
    @Test
    @UsingDataSet(locations = {"/fixtures/tenants.json","/fixtures/devices.json"})
    public void shouldLogDeviceEvent() throws Exception {
        deviceEventService.logEvent(payload,deviceId);

        Device device = deviceRepository.findByDeviceId(deviceId);

        assertThat(device,notNullValue());

        Event last = device.getLastEvent();

        assertThat(last,notNullValue());
        assertThat(last.getPayload(),equalTo(payload));

        long gap = Duration.between(last.getTimestamp(), Instant.now()).abs().getSeconds();

        assertThat(gap,not(greaterThan(60L)));
    }
}