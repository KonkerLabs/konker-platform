package com.konkerlabs.platform.registry.test.business.model;

import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.model.EventRoute.RouteActor;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.HashMap;
import java.util.UUID;

import static com.konkerlabs.platform.registry.business.services.publishers.EventPublisherMqtt.DEVICE_MQTT_CHANNEL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class EventRouteTest {

	private EventRoute subject;
	private String incomingAuthority = "0000000000000004";
	private String outgoingAuthority = "0000000000000005";
	private String guid;

	private EventRoute subjectForSameDeviceChannel;
	private String incomingOutgoingAuthority = "0000000000000006";

	@Before
	public void setUp() throws Exception {

		Tenant tenant = Tenant.builder().name("Konker").build();

		guid = UUID.randomUUID().toString();

		subject = EventRoute.builder().tenant(tenant).name("Route name").description("Description")
				.incoming(RouteActor.builder().data(new HashMap<String, String>() {
					{
						put(DEVICE_MQTT_CHANNEL, "data");
					}
				}).uri(new URI("device", incomingAuthority, null, null, null)).build())
				.outgoing(RouteActor.builder().data(new HashMap<String, String>() {
					{
						put(DEVICE_MQTT_CHANNEL, "in");
					}
				}).uri(new URI("device", outgoingAuthority, null, null, null)).build())
				// .transformations(Arrays.asList(new
				// EventRoute.RuleTransformation[]{
				// new
				// EventRoute.RuleTransformation(EventRouteExecutorImpl.RuleTransformationType.EXPRESSION_LANGUAGE.name())
				// }))
				.filteringExpression("#command.type == 'ButtonPressed'")
				.transformation(Transformation.builder().id("id").name("Name").description("Description")
						.step(RestTransformationStep.builder().build()).build())
				.guid(guid).active(true).build();

		subjectForSameDeviceChannel = EventRoute.builder().tenant(tenant).name("Route name2")
				.description("Description2").incoming(RouteActor.builder().data(new HashMap<String, String>() {
					{
						put(DEVICE_MQTT_CHANNEL, "same");
					}
				}).uri(new URI("device", incomingOutgoingAuthority, null, null, null)).build())
				.outgoing(RouteActor.builder().data(new HashMap<String, String>() {
					{
						put(DEVICE_MQTT_CHANNEL, "same");
					}
				}).uri(new URI("device", incomingOutgoingAuthority, null, null, null)).build()).guid(guid).active(true)
				.build();
	}

	@Test
	public void shouldReturnAValidationMessageIfTenantIsNull() throws Exception {
		subject.setTenant(null);

		String expectedMessage = CommonValidations.TENANT_NULL.getCode();

		assertThat(subject.applyValidations().get(), hasEntry(expectedMessage, null));
	}

	@Test
	public void shouldReturnAValidationMessageIfNameIsNull() throws Exception {
		subject.setName(null);

		String expectedMessage = EventRoute.Validations.NAME_NULL.getCode();

		assertThat(subject.applyValidations().get(), hasEntry(expectedMessage, null));
	}

	@Test
	public void shouldReturnAValidationMessageIfNameIsEmpty() throws Exception {
		subject.setName("");

		String expectedMessage = EventRoute.Validations.NAME_NULL.getCode();

		assertThat(subject.applyValidations().get(), hasEntry(expectedMessage, null));
	}

	@Test
	public void shouldReturnAValidationMessageIfIncomingIsNull() throws Exception {
		subject.setIncoming(null);

		String expectedMessage = EventRoute.Validations.INCOMING_ACTOR_NULL.getCode();

		assertThat(subject.applyValidations().get(), hasEntry(expectedMessage, null));
	}

	@Test
	public void shouldReturnAValidationMessageIfIncomingURIIsNull() throws Exception {
		subject.getIncoming().setUri(null);

		String expectedMessage = EventRoute.Validations.INCOMING_ACTOR_URI_NULL.getCode();

		assertThat(subject.applyValidations().get(), hasEntry(expectedMessage, null));
	}

	@Test
	public void shouldReturnAValidationMessageIfIncomingURIIsEmpty() throws Exception {
		subject.getIncoming().setUri(new URI(null, null, null, null, null));

		String expectedMessage = EventRoute.Validations.INCOMING_ACTOR_URI_NULL.getCode();

		assertThat(subject.applyValidations().get(), hasEntry(expectedMessage, null));
	}

	@Test
	public void shouldReturnAValidationMessageIfOutgoingIsNull() throws Exception {
		subject.setOutgoing(null);

		String expectedMessage = EventRoute.Validations.OUTGOING_ACTOR_NULL.getCode();

		assertThat(subject.applyValidations().get(), hasEntry(expectedMessage, null));
	}

	@Test
	public void shouldReturnAValidationMessageIfOutgoingURIIsNull() throws Exception {
		subject.getOutgoing().setUri(null);

		String expectedMessage = EventRoute.Validations.OUTGOING_ACTOR_URI_NULL.getCode();

		assertThat(subject.applyValidations().get(), hasEntry(expectedMessage, null));
	}

	@Test
	public void shouldReturnAValidationMessageIfOutgoingURIIsEmpty() throws Exception {
		subject.getOutgoing().setUri(new URI(null, null, null, null, null));

		String expectedMessage = EventRoute.Validations.OUTGOING_ACTOR_URI_NULL.getCode();

		assertThat(subject.applyValidations().get(), hasEntry(expectedMessage, null));
	}

	@Test
	public void shouldReturnAValidationMessageIfIncomingChannelIsEmpty() throws Exception {
		subject.getIncoming().setData(new HashMap<>());

		String expectedMessage = EventRoute.Validations.INCOMING_ACTOR_CHANNEL_NULL.getCode();

		assertThat(subject.applyValidations().get(), hasEntry(expectedMessage, null));
	}

	@Test
	public void shouldReturnAValidationMessageIfOutgoingChannelIsEmpty() throws Exception {
		subject.getOutgoing().setData(new HashMap<>());

		String expectedMessage = EventRoute.Validations.OUTGOING_ACTOR_CHANNEL_NULL.getCode();

		assertThat(subject.applyValidations().get(), hasEntry(expectedMessage, null));
	}

	@Test
	public void shouldReturnAValidationMessageIfGUIDIsNull() throws Exception {
		subject.setGuid(null);

		String expectedMessage = EventRoute.Validations.GUID_NULL.getCode();

		assertThat(subject.applyValidations().get(), hasEntry(expectedMessage, null));
	}

	@Test
	public void shouldReturnAValidationMessageIfGUIDIsEmpty() throws Exception {
		subject.setGuid("");

		String expectedMessage = EventRoute.Validations.GUID_NULL.getCode();

		assertThat(subject.applyValidations().get(), hasEntry(expectedMessage, null));
	}

	@Test
	public void shouldHaveNoValidationMessagesIfRecordIsValid() throws Exception {
		assertThat(subject.applyValidations().isPresent(), is(false));
	}

	@Test
	public void shouldReturnAValidationMessageIfIncomingAndOutgoingDeviceAndChannelAreEquals() throws Exception {
		String expectedMessage = EventRoute.Validations.INCOMING_OUTGOING_DEVICE_CHANNELS_SAME.getCode();

		assertThat(subjectForSameDeviceChannel.applyValidations().get(), hasEntry(expectedMessage, null));
	}
}
