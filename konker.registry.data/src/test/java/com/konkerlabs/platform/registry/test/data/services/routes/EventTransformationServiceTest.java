package com.konkerlabs.platform.registry.test.data.services.routes;

import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.RestTransformationStep;
import com.konkerlabs.platform.registry.business.model.Transformation;
import com.konkerlabs.platform.registry.business.model.TransformationStep;
import com.konkerlabs.platform.registry.config.PubServerConfig;
import com.konkerlabs.platform.registry.data.services.routes.api.EventTransformationService;
import com.konkerlabs.platform.registry.integration.exceptions.IntegrationException;
import com.konkerlabs.platform.registry.integration.gateways.HttpGateway;
import com.konkerlabs.platform.registry.test.data.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.data.base.MongoTestConfiguration;
import com.konkerlabs.platform.registry.test.data.base.RedisTestConfiguration;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.konkerlabs.platform.registry.data.services.publishers.EventPublisherDevice.DEVICE_MQTT_CHANNEL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        BusinessTestConfiguration.class,
        MongoTestConfiguration.class,
        RedisTestConfiguration.class,
        PubServerConfig.class
})
public class EventTransformationServiceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    private HttpGateway httpGateway;
    @Autowired
    private EventTransformationService subject;
    private Event event;
    private Transformation transformation;

    @Captor
    private ArgumentCaptor<Supplier<String>> bodyCaptor;

    private final String validPayloadJson = "{\n" +
            "  \"field\" : \"value\",\n" +
            "  \"count\" : 34,\n" +
            "  \"amount\" : 21.45,\n" +
            "  \"valid\" : true\n" +
            '}';

    private final String invalidPayloadJson = "{\n" +
            "  \"field\" \"value\",\n" +
            "  \"count\" : 34,\n" +
            "  \"amount\" : 21,45\n" +
            "  \"valid\" : tr\n" +
            '}';

    private final String transformationMethod = "POST";
    private final String transformationUrl = "http://server:8080/path/@{#field}?query=1";
    private final String transformationServiceUsername = "username";
    private final String transformationServicePassword = "password";

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        event = Event.builder().creationTimestamp(Instant.now())
                .incoming(
                        Event.EventActor.builder()
                                .channel(DEVICE_MQTT_CHANNEL).build()
                ).payload(validPayloadJson).build();

        transformation = Transformation.builder()
                .id("id")
                .name("Transformation name")
                .description("Description")
                .step(RestTransformationStep.builder()
                        .attributes(new HashMap<String, Object>() {
                            {
                                put(RestTransformationStep.REST_ATTRIBUTE_METHOD, transformationMethod);
                                put(RestTransformationStep.REST_URL_ATTRIBUTE_NAME, transformationUrl);
                                put(RestTransformationStep.REST_USERNAME_ATTRIBUTE_NAME, transformationServiceUsername);
                                put(RestTransformationStep.REST_PASSWORD_ATTRIBUTE_NAME, transformationServicePassword);
                            }
                        }).build()
                )
                .build();
    }

    @Test
    public void shouldRaiseAnExceptionIfEventIsNull() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Event cannot be null");

        subject.transform(null, transformation);
    }

    @Test
    public void shouldRaiseAnExceptionIfTransformationIsNull() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Transformation cannot be null");

        subject.transform(event, null);
    }

    @Test
    public void shouldReturnEmptyEventIfTransformationURLTemplateIsInvalid() {
        transformation.getSteps().get(0).getAttributes()
                .put(RestTransformationStep.REST_URL_ATTRIBUTE_NAME, "http://server:8080/path/@{#dummy?query=1");

        Optional<Event> transformed = subject.transform(event, transformation);

        assertThat(transformed, equalTo(Optional.empty()));
    }

    @Test
    public void shouldReturnEmptyEventIfEventPayloadIsInvalid() {
        event.setPayload(invalidPayloadJson);

        Optional<Event> transformed = subject.transform(event, transformation);

        assertThat(transformed, equalTo(Optional.empty()));
    }

    @Test
    public void shouldReturnEmptyEventInCaseOfAnIntegrationException() throws Exception {
        String url = transformationUrl.replaceAll("\\@\\{.*}", "value");
        URI uri = URI.create(url);
        when(
                httpGateway.request(
                        eq(HttpMethod.POST),
                        Mockito.any(HttpHeaders.class),
                        eq(uri),
                        eq(MediaType.APPLICATION_JSON),
                        bodyCaptor.capture(),
                        eq(transformationServiceUsername),
                        eq(transformationServicePassword))
        ).thenThrow(IntegrationException.class);

        Optional<Event> transformed = subject.transform(event, transformation);

        assertThat(transformed, equalTo(Optional.empty()));
    }

    @Test
    public void shouldReturnEmptyEventIfStepResponseBodyIsEmpty() throws Exception {
        when(
                httpGateway.request(
                        eq(HttpMethod.POST),
                        Mockito.any(HttpHeaders.class),
                        Mockito.any(URI.class),
                        eq(MediaType.APPLICATION_JSON),
                        bodyCaptor.capture(),
                        eq(transformationServiceUsername),
                        eq(transformationServicePassword))
        ).thenReturn("");

        Optional<Event> transformed = subject.transform(event, transformation);
        assertThat(transformed, equalTo(Optional.empty()));
    }

    @Test
    public void shouldReturnEmptyEventIfStepResponseIsAnEmptyJsonArray() throws Exception {
        when(
                httpGateway.request(
                        eq(HttpMethod.POST),
                        Mockito.any(HttpHeaders.class),
                        Mockito.any(URI.class),
                        eq(MediaType.APPLICATION_JSON),
                        bodyCaptor.capture(),
                        eq(transformationServiceUsername),
                        eq(transformationServicePassword))
        ).thenReturn("[]");

        Optional<Event> transformed = subject.transform(event, transformation);
        assertThat(transformed, equalTo(Optional.empty()));
    }

    @Test
    public void shouldReturnEmptyEventIfStepResponseIsAnEmptyJsonObject() throws Exception {
        when(
                httpGateway.request(
                        eq(HttpMethod.POST),
                        Mockito.any(HttpHeaders.class),
                        Mockito.any(URI.class),
                        eq(MediaType.APPLICATION_JSON),
                        bodyCaptor.capture(),
                        eq(transformationServiceUsername),
                        eq(transformationServicePassword))
        ).thenReturn("{}");

        Optional<Event> transformed = subject.transform(event, transformation);
        assertThat(transformed, equalTo(Optional.empty()));
    }

    @Test
    public void shouldReturnATransformedEventAfterASingleStep() throws Exception {
        String firstStepResponseJson = "{\n" +
                "    \"field\" : \"valueOne\",\n" +
                "    \"count\" : 21,\n" +
                "    \"amount\" : 356.45,\n" +
                "    \"valid\" : false\n" +
                "  }";

        when(
                httpGateway.request(
                        eq(HttpMethod.POST),
                        Mockito.any(HttpHeaders.class),
                        Mockito.any(URI.class),
                        eq(MediaType.APPLICATION_JSON),
                        bodyCaptor.capture(),
                        eq(transformationServiceUsername),
                        eq(transformationServicePassword))
        ).thenReturn(firstStepResponseJson);

        Optional<Event> transformed = subject.transform(event, transformation);

        assertThat(transformed.isPresent(), equalTo(true));
        transformed.ifPresent(e -> {
            assertThat(e.getCreationTimestamp(), equalTo(event.getCreationTimestamp()));
            assertThat(e.getIncoming().getChannel(), equalTo(event.getIncoming().getChannel()));
            assertThat(e.getPayload(), equalTo(firstStepResponseJson));
        });
    }

    @Test
    public void shouldReturnATransformedEventAfterManySteps() throws Exception {
        String firstStepResponseJson = "{\n" +
                "    \"field\" : \"valueOne\",\n" +
                "    \"count\" : 21,\n" +
                "    \"amount\" : 356.45,\n" +
                "    \"valid\" : false,\n" +
                "    \"customerId\" : \"123456\"" +
                "  }";

        String secondStepResponseJson = "{\n" +
                "    \"okToGo\" : true\n" +
                "  }";

        URI firstURI = URI.create(transformationUrl.replaceAll("\\@\\{.*}", "value"));

        when(
                httpGateway.request(
                        eq(HttpMethod.POST),
                        Mockito.any(HttpHeaders.class),
                        eq(firstURI),
                        eq(MediaType.APPLICATION_JSON),
                        bodyCaptor.capture(),
                        eq(transformationServiceUsername),
                        eq(transformationServicePassword))
        ).thenReturn(firstStepResponseJson);

        String secondStepURI = "http://server:8080/service/@{#customerId}/verify";

        List<TransformationStep> steps = new ArrayList<>(transformation.getSteps());
        steps.add(RestTransformationStep.builder()
                .attributes(new HashMap<String, Object>() {
                    {
                        put(RestTransformationStep.REST_ATTRIBUTE_METHOD, transformationMethod);
                        put(RestTransformationStep.REST_URL_ATTRIBUTE_NAME, secondStepURI);
                        put(RestTransformationStep.REST_USERNAME_ATTRIBUTE_NAME, "");
                        put(RestTransformationStep.REST_PASSWORD_ATTRIBUTE_NAME, "");
                    }
                }).build());
        transformation.setSteps(steps);

        URI secondURI = URI.create(secondStepURI.replaceAll("\\@\\{.*}", "123456"));

        when(
                httpGateway.request(
                        eq(HttpMethod.POST),
                        Mockito.any(HttpHeaders.class),
                        eq(secondURI),
                        eq(MediaType.APPLICATION_JSON),
                        bodyCaptor.capture(),
                        eq(""),
                        eq(""))
        ).thenReturn(secondStepResponseJson);

        Optional<Event> transformed = subject.transform(event, transformation);

        assertThat(transformed.isPresent(), equalTo(true));
        transformed.ifPresent(e -> {
            assertThat(e.getCreationTimestamp(), equalTo(event.getCreationTimestamp()));
            assertThat(e.getIncoming().getChannel(), equalTo(event.getIncoming().getChannel()));
            assertThat(e.getPayload(), equalTo(secondStepResponseJson));
        });
    }
}