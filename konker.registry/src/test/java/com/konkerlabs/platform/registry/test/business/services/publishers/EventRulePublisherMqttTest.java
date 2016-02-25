package com.konkerlabs.platform.registry.test.business.services.publishers;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.EventRule;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.rules.api.EventRulePublisher;
import com.konkerlabs.platform.registry.integration.gateways.MqttMessageGateway;
import com.konkerlabs.platform.registry.test.base.BusinessLayerTestSupport;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    MongoTestConfiguration.class,
    BusinessTestConfiguration.class,
    EventRulePublisherMqttTest.EventRulePublisherMqttTestContext.class
})
public class EventRulePublisherMqttTest extends BusinessLayerTestSupport {

    private static final String THE_DEVICE_ID = "71fc0d48-674a-4d62-b3e5-0216abca63af";
    private static final String REGISTERED_DEVICE_ID = "95c14b36ba2b43f1";

    private static final String MQTT_OUTGOING_TOPIC_TEMPLATE = "iot/{0}/{1}";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Autowired
    private DeviceRegisterService deviceRegisterService;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private MqttMessageGateway mqttMessageGateway;

    @Autowired
    @Qualifier("device")
    private EventRulePublisher subject;

    private Event event;
    private URI outgoingUri;
    private EventRule.RuleActor outgoingRuleActor;

    @Before
    public void setUp() throws Exception {
        event = Event.builder()
            .channel("channel")
            .payload("payload")
            .timestamp(Instant.now()).build();

        outgoingUri = new URI("device",REGISTERED_DEVICE_ID,null,null,null);

        outgoingRuleActor = new EventRule.RuleActor(outgoingUri);
        outgoingRuleActor.getData().put("channel",event.getChannel());
    }

    @Test
    public void shouldRaiseAnExceptionIfDeviceIsUnknown() throws Exception {
        outgoingUri = new URI("device","unknown_authority",null,null,null);
        outgoingRuleActor = new EventRule.RuleActor(outgoingUri);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Device authority is unknown");

        subject.send(event,outgoingRuleActor);
    }
    @Test
    @UsingDataSet(locations = {"/fixtures/devices.json", "/fixtures/tenants.json"})
    public void shouldNotSendAnyEventThroughGatewayIfDeviceIsDisabled() throws Exception {
        Tenant tenant = tenantRepository.findByName("Konker");
        
        Optional.of(deviceRegisterService.getById(tenant, THE_DEVICE_ID).getResult())
            .filter(device -> !device.isActive())
            .orElseGet(() -> deviceRegisterService.switchActivation(tenant, THE_DEVICE_ID).getResult());

        subject.send(event,outgoingRuleActor);

        verify(mqttMessageGateway,never()).send(anyString(),anyString());
    }

    @Test
    @UsingDataSet(locations = {"/fixtures/devices.json", "/fixtures/tenants.json"})
    public void shouldSendAnEventThroughGatewayIfDeviceIsEnabled() throws Exception {
        Tenant tenant = tenantRepository.findByName("Konker");

        Optional.of(deviceRegisterService.getById(tenant, THE_DEVICE_ID).getResult())
                .filter(Device::isActive)
                .orElseGet(() -> deviceRegisterService.switchActivation(tenant, THE_DEVICE_ID).getResult());

        String expectedMqttTopic = MessageFormat
            .format(MQTT_OUTGOING_TOPIC_TEMPLATE,outgoingUri.getAuthority(),outgoingRuleActor.getData().get("channel"));

        subject.send(event,outgoingRuleActor);

        verify(mqttMessageGateway).send(event.getPayload(),expectedMqttTopic);
    }

    @Configuration
    static class EventRulePublisherMqttTestContext {
        @Bean
        public MqttMessageGateway mqttMessageGateway() {
            return Mockito.mock(MqttMessageGateway.class);
        }
    }
}