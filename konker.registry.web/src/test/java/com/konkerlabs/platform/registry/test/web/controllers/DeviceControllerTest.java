package com.konkerlabs.platform.registry.test.web.controllers;

import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.services.api.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.konkerlabs.platform.registry.business.model.EventSchema.SchemaField;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.config.CdnConfig;
import com.konkerlabs.platform.registry.config.EmailConfig;
import com.konkerlabs.platform.registry.config.MessageSourceConfig;
import com.konkerlabs.platform.registry.config.PubServerConfig;
import com.konkerlabs.platform.registry.config.WebConfig;
import com.konkerlabs.platform.registry.config.WebMvcConfig;
import com.konkerlabs.platform.registry.test.base.SecurityTestConfiguration;
import com.konkerlabs.platform.registry.test.base.WebLayerTestContext;
import com.konkerlabs.platform.registry.test.base.WebTestConfiguration;
import com.konkerlabs.platform.registry.web.controllers.DeviceController.ChannelVO;
import com.konkerlabs.platform.registry.web.controllers.DeviceController.MetricVO;
import com.konkerlabs.platform.registry.web.converters.InstantToStringConverter;
import com.konkerlabs.platform.registry.web.forms.DeviceRegistrationForm;
import com.konkerlabs.platform.utilities.parsers.json.JsonParsingService;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = { WebMvcConfig.class, WebTestConfiguration.class, SecurityTestConfiguration.class,
		DeviceControllerTest.DeviceTestContextConfig.class, WebConfig.class, PubServerConfig.class,
		CdnConfig.class, EmailConfig.class, MessageSourceConfig.class})
public class DeviceControllerTest extends WebLayerTestContext {

	private static final String USER_DEFINED_DEVICE_ID = "SN1234567890";
	private static final String DEVICE_GUID = "7d51c242-81db-11e6-a8c2-0746f010e945";

	@Autowired
	ApplicationContext applicationContext;
	@Autowired
	DeviceRegisterService deviceRegisterService;
	@Autowired
	HealthAlertService healthAlertService;
	@Autowired
	DeviceEventService deviceEventService;
	@Autowired
	EventSchemaService eventSchemaService;
	@Autowired
	ApplicationService applicationService;
	@Autowired
	private Tenant tenant;

	private List<Device> registeredDevices;
	private ServiceResponse<Device> response;
	MultiValueMap<String, String> deviceData;
	private Device device;
	private DeviceRegistrationForm deviceForm;
	private Device savedDevice;
	private Application application;

	@Before
	public void setUp() {
		application = Application.builder().name(tenant.getDomainName()).build();

		registeredDevices = new ArrayList<>();
		registeredDevices.add(Device.builder().application(application).build());

		deviceData = new LinkedMultiValueMap<>();
		deviceData.add("name", "Device name");
		deviceData.add("deviceId", USER_DEFINED_DEVICE_ID);
		deviceData.add("description", "Some description");
		deviceData.add("guid", DEVICE_GUID);

		Device.DeviceBuilder builder = Device.builder().deviceId(deviceData.getFirst("deviceId"))
				.name(deviceData.getFirst("name")).description(deviceData.getFirst("description"))
				.guid(deviceData.getFirst("guid"));

		device = builder.build();
		device.setActive(true);

		savedDevice = builder.id("deviceId").application(application).active(true).build();

		deviceForm = new DeviceRegistrationForm();
		deviceForm.setDeviceId(device.getDeviceId());
		deviceForm.setName(device.getName());
		deviceForm.setDescription(device.getDescription());
		deviceForm.setGuid(DEVICE_GUID);
		deviceForm.setActive(Boolean.TRUE);
	}

	@After
	public void tearDown() {
		Mockito.reset(deviceRegisterService);
		Mockito.reset(eventSchemaService);
		Mockito.reset(deviceEventService);
	}

	@Test
	@WithMockUser(authorities={"LIST_DEVICES"})
	public void shouldListAllRegisteredDevices() throws Exception {
		when(deviceRegisterService.findAll(tenant, application))
				.thenReturn(ServiceResponseBuilder.<List<Device>> ok().withResult(registeredDevices).build());
		when(applicationService.findAll(tenant))
				.thenReturn(ServiceResponseBuilder.<List<Application>> ok().withResult(Collections.singletonList(application)).build());

		getMockMvc().perform(get("/devices")).andExpect(model().attribute("devices", equalTo(registeredDevices)))
				.andExpect(view().name("devices/index"));
	}

	@Test
	@WithMockUser(authorities={"ADD_DEVICE"})
	public void shouldShowRegistrationForm() throws Exception {
		getMockMvc().perform(get("/devices/new")).andExpect(view().name("devices/form"))
				.andExpect(model().attribute("device", any(DeviceRegistrationForm.class)))
				.andExpect(model().attribute("action", MessageFormat.format("/devices/{0}/save", application.getName())));

	}

	@Test
	@WithMockUser(authorities={"ADD_DEVICE"})
	public void shouldBindErrorMessagesWhenRegistrationFailsAndGoBackToRegistrationForm() throws Exception {
		response = ServiceResponseBuilder.<Device> error().withMessage(CommonValidations.RECORD_NULL.getCode(), null)
				.build();

		when(deviceRegisterService.register(eq(tenant), eq(application), eq(device))).thenReturn(response);
		when(applicationService.getByApplicationName(tenant, application.getName()))
			.thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application).build());

		getMockMvc().perform(post(MessageFormat.format("/devices/{0}/save", application.getName())).params(deviceData))
				.andExpect(model().attribute("errors", equalTo(new ArrayList() {
					{
						add(applicationContext.getMessage(CommonValidations.RECORD_NULL.getCode(), null,
								Locale.ENGLISH));
					}
				}))).andExpect(model().attribute("method", ""))
				.andExpect(model().attribute("device", equalTo(deviceForm))).andExpect(view().name("devices/form"));

		verify(deviceRegisterService).register(eq(tenant), eq(application), eq(device));
	}

	@Test
	@WithMockUser(authorities={"ADD_DEVICE"})
	public void shouldRedirectToShowAfterRegistrationSucceed() throws Exception {
		response = ServiceResponseBuilder.<Device> ok().withResult(savedDevice).build();

		when(deviceRegisterService.register(eq(tenant), eq(application), eq(device))).thenReturn(response);
		when(applicationService.getByApplicationName(tenant, application.getName()))
			.thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application).build());

		getMockMvc().perform(post(MessageFormat.format("/devices/{0}/save", application.getName())).params(deviceData))
				.andExpect(flash().attribute("message", applicationContext.getMessage(
				        DeviceRegisterService.Messages.DEVICE_REGISTERED_SUCCESSFULLY.getCode(), null, Locale.ENGLISH)))
				.andExpect(redirectedUrl(MessageFormat.format("/devices/{0}/{1}", application.getName(), savedDevice.getGuid())));

		verify(deviceRegisterService).register(eq(tenant), eq(application), eq(device));
	}

	@Test
	@WithMockUser(authorities={"SHOW_DEVICE"})
	public void shouldShowDeviceDetails() throws Exception {
		savedDevice.setRegistrationDate(Instant.now());
		when(deviceRegisterService.getByDeviceGuid(tenant, application, savedDevice.getGuid()))
				.thenReturn(ServiceResponseBuilder.<Device> ok().withResult(savedDevice).build());
		when(applicationService.getByApplicationName(tenant, application.getName()))
				.thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application).build());

		getMockMvc().perform(get(MessageFormat.format("/devices/{0}/{1}", application.getName(), savedDevice.getGuid())))
				.andExpect(model().attribute("device", savedDevice)).andExpect(view().name("devices/show"));

		verify(deviceRegisterService).getByDeviceGuid(tenant, application, savedDevice.getGuid());
	}

	@Test
	@WithMockUser(authorities={"SHOW_DEVICE"})
	public void shouldShowDeviceStatus() throws Exception {
        HealthAlert alert = HealthAlert.builder().severity(HealthAlert.HealthAlertSeverity.DISABLED).build();

		when(healthAlertService.getCurrentHealthByGuid(tenant, application, savedDevice.getGuid()))
				.thenReturn(ServiceResponseBuilder.<HealthAlert>ok().withResult(alert).build());

		getMockMvc()
				.perform(
				        get(MessageFormat.format("/devices/{0}/{1}/status", application.getName(), savedDevice.getGuid()))
                                .contentType("application/json")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
				.andExpect(content().contentType("application/json;charset=UTF-8"))
				.andExpect(jsonPath("$.status", org.hamcrest.Matchers.is("DISABLED")));

		verify(deviceRegisterService).getByDeviceGuid(tenant, application, savedDevice.getGuid());
	}

	@Test
	@WithMockUser(authorities={"VIEW_DEVICE_LOG"})
	public void shouldShowDeviceEventList() throws Exception {
		savedDevice.setRegistrationDate(Instant.now());
		when(deviceRegisterService.getByDeviceGuid(tenant, application, savedDevice.getGuid()))
				.thenReturn(ServiceResponseBuilder.<Device> ok().withResult(savedDevice).build());
		when(deviceEventService.findIncomingBy(tenant, application, savedDevice.getGuid(), null, null, null, false, 50))
				.thenReturn(ServiceResponseBuilder.<List<Event>> ok().withResult(Collections.emptyList()).build());
		when(deviceEventService.findOutgoingBy(tenant, application, savedDevice.getGuid(), null, null, null, false, 50))
				.thenReturn(ServiceResponseBuilder.<List<Event>> ok().withResult(Collections.emptyList()).build());

		// find last numeric metric mocks
        List<String> channels = Collections.singletonList("square");
        List<ChannelVO> channelVOs = Collections.singletonList(new ChannelVO("square"));
		EventSchema lastSchema = EventSchema.builder().channel("square").field(SchemaField.builder().path("rj").build()).build();

		when(eventSchemaService.findKnownIncomingChannelsBy(tenant, application, savedDevice.getGuid()))
			.thenReturn(ServiceResponseBuilder.<List<String>> ok().withResult(channels).build());
		when(eventSchemaService.findLastIncomingBy(tenant, application, savedDevice.getGuid(), JsonNodeType.NUMBER))
			.thenReturn(ServiceResponseBuilder.<EventSchema> ok().withResult(lastSchema).build());
		when(eventSchemaService.findKnownIncomingMetricsBy(tenant, application, savedDevice.getGuid(), "square", JsonNodeType.NUMBER))
			.thenReturn(ServiceResponseBuilder.<List<String>> ok().withResult(Collections.emptyList()).build());
		when(eventSchemaService.findKnownIncomingMetricsBy(tenant, application, savedDevice.getGuid(), JsonNodeType.NUMBER))
			.thenReturn(ServiceResponseBuilder.<List<String>> ok().withResult(Collections.emptyList()).build());
		when(applicationService.getByApplicationName(tenant, application.getName()))
			.thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application).build());

		getMockMvc().perform(get(MessageFormat.format("/devices/{0}/{1}/events", application.getName(), savedDevice.getGuid())))
                .andExpect(model().attribute("channels", channelVOs))
				.andExpect(model().attribute("metrics", Collections.emptyList()))
				.andExpect(model().attribute("device", savedDevice))
				.andExpect(model().attribute("hasAnyEvent", false))
				.andExpect(model().attribute("existsNumericMetric", false))
				.andExpect(view().name("devices/events"));

		verify(deviceRegisterService).getByDeviceGuid(tenant, application, savedDevice.getGuid());
		verify(deviceEventService).findIncomingBy(tenant, application, savedDevice.getGuid(), null, null, null, false, 50);
	}

    @Test
    @WithMockUser(authorities={"VIEW_DEVICE_LOG"})
    public void shouldShowDeviceEventListWithNumericMetric() throws Exception {
        savedDevice.setRegistrationDate(Instant.now());
        when(deviceRegisterService.getByDeviceGuid(tenant, application, savedDevice.getGuid()))
                .thenReturn(ServiceResponseBuilder.<Device> ok().withResult(savedDevice).build());
        when(deviceEventService.findIncomingBy(tenant, application, savedDevice.getGuid(), null, null, null, false, 50))
                .thenReturn(ServiceResponseBuilder.<List<Event>> ok().withResult(Collections.emptyList()).build());
        when(deviceEventService.findOutgoingBy(tenant, application, savedDevice.getGuid(), null, null, null, false, 50))
                .thenReturn(ServiceResponseBuilder.<List<Event>> ok().withResult(Collections.emptyList()).build());

        // find last numeric metric mocks
        List<String> channels = Collections.singletonList("square");
        List<ChannelVO> channelVOs = Collections.singletonList(new ChannelVO("square"));
        EventSchema lastSchema = EventSchema.builder().channel("square").field(SchemaField.builder().path("rj").build()).build();

        List<String> metrics = Collections.singletonList("lumens");
        List<MetricVO> metricVOs = Collections.singletonList(new MetricVO("lumens"));

        when(eventSchemaService.findKnownIncomingChannelsBy(tenant, application, savedDevice.getGuid()))
            .thenReturn(ServiceResponseBuilder.<List<String>> ok().withResult(channels).build());
        when(eventSchemaService.findLastIncomingBy(tenant, application, savedDevice.getGuid(), JsonNodeType.NUMBER))
            .thenReturn(ServiceResponseBuilder.<EventSchema> ok().withResult(lastSchema).build());
        when(eventSchemaService.findKnownIncomingMetricsBy(tenant, application, savedDevice.getGuid(), "square", JsonNodeType.NUMBER))
            .thenReturn(ServiceResponseBuilder.<List<String>> ok().withResult(metrics).build());
        when(eventSchemaService.findKnownIncomingMetricsBy(tenant, application, savedDevice.getGuid(), JsonNodeType.NUMBER))
            .thenReturn(ServiceResponseBuilder.<List<String>> ok().withResult(metrics).build());
        when(applicationService.getByApplicationName(tenant, application.getName()))
			.thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application).build());

        getMockMvc().perform(get(MessageFormat.format("/devices/{0}/{1}/events", application.getName(), savedDevice.getGuid())))
                .andExpect(model().attribute("channels", channelVOs))
                .andExpect(model().attribute("metrics", metricVOs))
                .andExpect(model().attribute("device", savedDevice))
                .andExpect(model().attribute("hasAnyEvent", false))
                .andExpect(model().attribute("existsNumericMetric", true))
                .andExpect(view().name("devices/events"));

        verify(deviceRegisterService).getByDeviceGuid(tenant, application, savedDevice.getGuid());
        verify(deviceEventService).findIncomingBy(tenant, application, savedDevice.getGuid(), null, null, null, false, 50);
    }

	@Test
	@WithMockUser(authorities={"EDIT_DEVICE"})
	public void shouldShowEditForm() throws Exception {
		when(deviceRegisterService.getByDeviceGuid(tenant, application, savedDevice.getGuid()))
				.thenReturn(ServiceResponseBuilder.<Device> ok().withResult(savedDevice).build());
		when(applicationService.getByApplicationName(tenant, application.getName()))
				.thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application).build());

		getMockMvc().perform(get(MessageFormat.format("/devices/{0}/{1}/edit", application.getName(), savedDevice.getGuid())))
				.andExpect(model().attribute("device", equalTo(deviceForm)))
				.andExpect(model().attribute("isEditing", true))
				.andExpect(model().attribute("action", MessageFormat.format("/devices/{0}/{1}", application.getName(), savedDevice.getGuid())))
				.andExpect(model().attribute("method", "put")).andExpect(view().name("devices/form"));
	}

	@Test
	@WithMockUser(authorities={"EDIT_DEVICE"})
	public void shouldBindErrorMessagesWhenEditFailsAndGoBackToEditForm() throws Exception {
		response = ServiceResponseBuilder.<Device> error().withMessage(CommonValidations.RECORD_NULL.getCode(), null)
				.build();

        when(applicationService.getByApplicationName(tenant, application.getName()))
              .thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application).build());
	    when(deviceRegisterService.getByDeviceGuid(tenant, application, DEVICE_GUID))
              .thenReturn(ServiceResponseBuilder.<Device>error().build());
		when(deviceRegisterService.update(Matchers.anyObject(), Matchers.anyObject(), Matchers.anyString(), Matchers.anyObject()))
			  .thenReturn(response);

		getMockMvc().perform(put(MessageFormat.format("/devices/{0}/{1}", application.getName(), DEVICE_GUID)).params(deviceData))
				.andExpect(model().attribute("errors", equalTo(new ArrayList() {
					{
						add(applicationContext.getMessage(CommonValidations.RECORD_NULL.getCode(), null,
								Locale.ENGLISH));
					}
				}))).andExpect(model().attribute("device", equalTo(deviceForm)))
				.andExpect(model().attribute("method", "put")).andExpect(view().name("devices/form"));

		verify(deviceRegisterService).update(eq(tenant), eq(application), eq(DEVICE_GUID), eq(device));
	}

	@Test
	@WithMockUser(authorities={"EDIT_DEVICE"})
	public void shouldRedirectToShowAfterEditSucceed() throws Exception {
		response = ServiceResponseBuilder.<Device> ok().withResult(savedDevice).build();

	    when(applicationService.getByApplicationName(tenant, application.getName()))
          .thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application).build());
        when(deviceRegisterService.getByDeviceGuid(tenant, application, savedDevice.getId()))
          .thenReturn(ServiceResponseBuilder.<Device>error().build());
		when(deviceRegisterService.update(eq(tenant), eq(application), eq(savedDevice.getId()), eq(device))).thenReturn(response);

		getMockMvc().perform(put(MessageFormat.format("/devices/{0}/{1}", application.getName(), savedDevice.getId())).params(deviceData))
				.andExpect(flash().attribute("message", applicationContext.getMessage(
				        DeviceRegisterService.Messages.DEVICE_REGISTERED_SUCCESSFULLY.getCode(), null, Locale.ENGLISH)))
				.andExpect(redirectedUrl(MessageFormat.format("/devices/{0}/{1}", application.getName(), savedDevice.getGuid())));

		verify(deviceRegisterService).update(eq(tenant), eq(application), eq(savedDevice.getId()), eq(device));
	}

	@Test
	@WithMockUser(authorities={"REMOVE_DEVICE"})
	public void shouldRedirectToListDevicesAndShowSuccessMessageAfterDeletionSucceed() throws Exception {
		device.setId(USER_DEFINED_DEVICE_ID);
		ServiceResponse<Device> responseRemoval = ServiceResponseBuilder.<Device> ok().withResult(device).build();

		ServiceResponse<List<Device>> responseListAll = ServiceResponseBuilder.<List<Device>> ok()
				.withResult(registeredDevices).build();

		spy(responseRemoval);
		spy(responseListAll);

		when(deviceRegisterService.remove(tenant, application, device.getId())).thenReturn(responseRemoval);
		when(deviceRegisterService.findAll(tenant, application)).thenReturn(responseListAll);
		when(applicationService.getByApplicationName(tenant, application.getName()))
			.thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application).build());

		getMockMvc().perform(delete("/devices/{0}/{1}", application.getName(), device.getId()))
				.andExpect(flash().attribute("message", applicationContext.getMessage(
				        DeviceRegisterService.Messages.DEVICE_REMOVED_SUCCESSFULLY.getCode(), null, Locale.ENGLISH)))
				.andExpect(redirectedUrl("/devices"));

		verify(deviceRegisterService).remove(tenant, application, device.getId());
	}

	@Test
	@WithMockUser(authorities={"VIEW_DEVICE_LOG"})
	public void shouldListIncomingEvents() throws Exception {

		when(deviceEventService.findIncomingBy(tenant, application, "deviceId", null, null, null, false, 50))
			.thenReturn(ServiceResponseBuilder.<List<Event>> ok().withResult(Collections.emptyList()).build());
		when(applicationService.getByApplicationName(tenant, application.getName()))
			.thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application).build());

		getMockMvc().perform(get(MessageFormat.format("/devices/{0}/{1}/events/incoming", application.getName(), savedDevice.getId())))
				.andExpect(model().attribute("recentIncomingEvents", org.hamcrest.Matchers.notNullValue()))
				.andExpect(view().name("devices/events-incoming"));

	}

	@Test
	@WithMockUser(authorities={"VIEW_DEVICE_LOG"})
	public void shouldListIncomingEventsWithFilter() throws Exception {

		when(deviceEventService.findIncomingBy(Matchers.any(Tenant.class), Matchers.any(Application.class), Matchers.matches("deviceId"), Matchers.isNull(String.class), Matchers.any(Instant.class), Matchers.any(Instant.class), Matchers.anyBoolean(), Matchers.anyInt()))
			.thenReturn(ServiceResponseBuilder.<List<Event>> ok().withResult(Collections.emptyList()).build());

		when(applicationService.getByApplicationName(Matchers.any(Tenant.class), Matchers.anyString()))
			.thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application).build());

		getMockMvc().perform(get(MessageFormat.format("/devices/{0}/{1}/events/incoming", application.getName(), savedDevice.getId()))
						.param("dateStart", "30/10/2016 16:35:57")
						.param("dateEnd",   "31/10/2016 11:00:00"))
				.andExpect(model().attribute("recentIncomingEvents", org.hamcrest.Matchers.notNullValue()))
				.andExpect(view().name("devices/events-incoming"));

	}

	@Test
	@WithMockUser(authorities={"VIEW_DEVICE_LOG"})
	public void shouldListOutgoingEvents() throws Exception {

		when(deviceEventService.findOutgoingBy(tenant, application, "deviceId", null, null, null, false, 50))
			.thenReturn(ServiceResponseBuilder.<List<Event>> ok().withResult(Collections.emptyList()).build());
		when(applicationService.getByApplicationName(tenant, application.getName()))
			.thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application).build());

		getMockMvc().perform(get(MessageFormat.format("/devices/{0}/{1}/events/outgoing", application.getName(), savedDevice.getId())))
				.andExpect(model().attribute("recentOutgoingEvents", org.hamcrest.Matchers.notNullValue()))
				.andExpect(view().name("devices/events-outgoing"));

	}

	@Test
	@WithMockUser(authorities={"VIEW_DEVICE_LOG"})
	public void shouldListOutgoingEventsWithFilter() throws Exception {

		when(deviceEventService.findOutgoingBy(Matchers.any(Tenant.class), Matchers.any(Application.class), Matchers.matches("deviceId"), Matchers.isNull(String.class), Matchers.any(Instant.class), Matchers.any(Instant.class), Matchers.anyBoolean(), Matchers.anyInt()))
			.thenReturn(ServiceResponseBuilder.<List<Event>> ok().withResult(Collections.emptyList()).build());
		when(applicationService.getByApplicationName(Matchers.any(Tenant.class), Matchers.anyString()))
			.thenReturn(ServiceResponseBuilder.<Application>ok().withResult(application).build());

		getMockMvc().perform(get(MessageFormat.format("/devices/{0}/{1}/events/outgoing", application.getName(), savedDevice.getId()))
						.param("dateStart", "30/10/2016 16:35:57")
						.param("dateEnd",   "31/10/2016 11:00:00"))
				.andExpect(model().attribute("recentOutgoingEvents", org.hamcrest.Matchers.notNullValue()))
				.andExpect(view().name("devices/events-outgoing"));

	}

	@Configuration
	static class DeviceTestContextConfig {
		@Bean
		public DeviceRegisterService deviceRegisterService() {
			return Mockito.mock(DeviceRegisterService.class);
		}

		@Bean
		public InstantToStringConverter instantToStringConverter() {
			return mock(InstantToStringConverter.class);
		}

		@Bean
		public DeviceEventService deviceEventService() {
			return Mockito.mock(DeviceEventService.class);
		}

		@Bean
		public EventSchemaService eventSchemaService() {
			return Mockito.mock(EventSchemaService.class);
		}

		@Bean
		public ApplicationService applicationService() {
			return Mockito.mock(ApplicationService.class);
		}

		@Bean
		public HealthAlertService healthAlertService() {
			return Mockito.mock(HealthAlertService.class);
		}

		@Bean
		public JsonParsingService jsonParsingService() {
			return Mockito.mock(JsonParsingService.class);
		}
	}

}