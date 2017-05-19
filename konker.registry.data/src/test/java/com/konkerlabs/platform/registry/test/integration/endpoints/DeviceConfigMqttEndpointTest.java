package com.konkerlabs.platform.registry.test.integration.endpoints;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.MessageFormat;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.context.MessageSource;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.DeviceModel;
import com.konkerlabs.platform.registry.business.model.Location;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.DeviceConfigSetupService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.integration.endpoints.DeviceConfigMqttEndpoint;
import com.konkerlabs.platform.registry.integration.gateways.MqttMessageGateway;

public class DeviceConfigMqttEndpointTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public DeviceConfigMqttEndpoint subject;
    private Message<String> message;
    private DeviceConfigSetupService deviceConfigSetupService;
    private DeviceRegisterService deviceRegisterService;
    private MessageSource messageSource;
    private MqttMessageGateway mqttMessageGateway;
    
    private Device device;
    private String deviceId = "95c14b36ba2b43f1";
    private String payload = "message";
    private String topic = MessageFormat.format("mgmt/{0}/pub/cfg", deviceId);

    @Before
    public void setUp() throws Exception {
    	deviceConfigSetupService = mock(DeviceConfigSetupService.class);
    	deviceRegisterService = mock(DeviceRegisterService.class);
    	messageSource = mock(MessageSource.class);
    	mqttMessageGateway = mock(MqttMessageGateway.class);
        subject = new DeviceConfigMqttEndpoint(deviceConfigSetupService, 
        										deviceRegisterService, 
        										messageSource, 
        										mqttMessageGateway);
        
        device = Device.builder()
        	.apiKey(deviceId)
        	.name("sendor1")
        	.tenant(Tenant.builder().domainName("konker").build())
        	.application(Application.builder().name("SmartAC").build())
        	.deviceModel(DeviceModel.builder().name("TempSensor").build())
        	.location(Location.builder().name("br_sp").build())
        	.build();

        message = MessageBuilder.withPayload(payload).setHeader(MqttHeaders.TOPIC,topic).build();
    }
    
    @Test
    public void shouldRaiseAnExceptionIfTopicIsNull() throws Exception {
        message = MessageBuilder.withPayload(payload).build();

        thrown.expect(MessagingException.class);
        thrown.expectMessage("Topic cannot be null or empty");

        subject.onEvent(message);
    }
    
    @Test
    public void shouldRaiseAnExceptionIfTopicIsEmpty() throws Exception {
        message = MessageBuilder.withPayload(payload).setHeader(MqttHeaders.TOPIC,"").build();

        thrown.expect(MessagingException.class);
        thrown.expectMessage("Topic cannot be null or empty");

        subject.onEvent(message);
    }
    
    @Test
    public void shouldRaiseAnExceptionIfDeviceNotExists() throws Exception {
        thrown.expect(MessagingException.class);
        thrown.expectMessage("Device does not exist");

        subject.onEvent(message);
    }
    
    @Test
    public void shouldReturnEmptyDeviceConfig() throws Exception {
    	when(deviceRegisterService.findByApiKey(anyString()))
    		.thenReturn(device);
    	
    	when(deviceConfigSetupService.findByModelAndLocation(
    			any(Tenant.class), 
    			any(Application.class), 
    			any(DeviceModel.class), 
    			any(Location.class)))
    		.thenReturn(ServiceResponseBuilder.<String> ok()
    				.withResult("{ }")
    				.build());
    	
        subject.onEvent(message);

        verify(deviceRegisterService).findByApiKey(device.getApiKey());
        verify(deviceConfigSetupService).findByModelAndLocation(
        		device.getTenant(), 
        		device.getApplication(), 
        		device.getDeviceModel(), 
        		device.getLocation());
    }
    
    @Test
    public void shouldReturnDeviceConfig() throws Exception {
    	when(deviceRegisterService.findByApiKey(anyString()))
    		.thenReturn(device);
    	
    	when(deviceConfigSetupService.findByModelAndLocation(
    			any(Tenant.class), 
    			any(Application.class), 
    			any(DeviceModel.class), 
    			any(Location.class)))
    		.thenReturn(ServiceResponseBuilder.<String> ok()
    				.withResult("{'sleepInterval' : 5, 'unit' : 'celsius'}")
    				.build());
    	
        subject.onEvent(message);

        verify(deviceRegisterService).findByApiKey(device.getApiKey());
        verify(deviceConfigSetupService).findByModelAndLocation(
        		device.getTenant(), 
        		device.getApplication(), 
        		device.getDeviceModel(), 
        		device.getLocation());
    }
}