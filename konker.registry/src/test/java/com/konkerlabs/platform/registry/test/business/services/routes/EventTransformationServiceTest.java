package com.konkerlabs.platform.registry.test.business.services.routes;

import com.konkerlabs.platform.registry.business.model.Event;
import com.konkerlabs.platform.registry.business.model.RestTransformationStep;
import com.konkerlabs.platform.registry.business.model.Transformation;
import com.konkerlabs.platform.registry.business.model.TransformationStep;
import com.konkerlabs.platform.registry.business.services.routes.api.EventTransformationService;
import com.konkerlabs.platform.registry.integration.exceptions.IntegrationException;
import com.konkerlabs.platform.registry.integration.gateways.HttpGateway;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
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

import static com.konkerlabs.platform.registry.business.services.publishers.EventPublisherDevice.DEVICE_MQTT_CHANNEL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    BusinessTestConfiguration.class
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

    private String validPayloadJson = "{\n" +
            "  \"field\" : \"value\",\n" +
            "  \"count\" : 34,\n" +
            "  \"amount\" : 21.45,\n" +
            "  \"valid\" : true\n" +
            "}";

    private String invalidPayloadJson = "{\n" +
            "  \"field\" \"value\",\n" +
            "  \"count\" : 34,\n" +
            "  \"amount\" : 21,45\n" +
            "  \"valid\" : tr\n" +
            "}";

    private String transformationUrl = "http://server:8080/path/@{#field}?query=1";
    private String transformationServiceUsername = "username";
    private String transformationServicePassword = "password";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        event = Event.builder().timestamp(Instant.now())
            .channel(DEVICE_MQTT_CHANNEL).payload(validPayloadJson).build();

        transformation = Transformation.builder()
            .id("id")
            .name("Transformation name")
            .description("Description")
            .step(RestTransformationStep.builder()
                .attributes(new HashMap<String,String>() {
                    {
                        put(RestTransformationStep.REST_URL_ATTRIBUTE_NAME,transformationUrl);
                        put(RestTransformationStep.REST_USERNAME_ATTRIBUTE_NAME,transformationServiceUsername);
                        put(RestTransformationStep.REST_PASSWORD_ATTRIBUTE_NAME,transformationServicePassword);
                    }
                }).build()
            )
            .build();
    }

    @Test
    public void shouldRaiseAnExceptionIfEventIsNull() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Event cannot be null");

        subject.transform(null,transformation);
    }

    @Test
    public void shouldRaiseAnExceptionIfTransformationIsNull() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Transformation cannot be null");

        subject.transform(event,null);
    }

    @Test
    public void shouldReturnEmptyEventIfTransformationURLTemplateIsInvalid() throws Exception {
        transformation.getSteps().get(0).getAttributes()
                .put(RestTransformationStep.REST_URL_ATTRIBUTE_NAME,"http://server:8080/path/@{#dummy?query=1");

        Optional<Event> transformed = subject.transform(event,transformation);

        assertThat(transformed,equalTo(Optional.empty()));
    }

    @Test
    public void shouldReturnEmptyEventIfEventPayloadIsInvalid() throws Exception {
        event.setPayload(invalidPayloadJson);

        Optional<Event> transformed = subject.transform(event,transformation);

        assertThat(transformed,equalTo(Optional.empty()));
    }

    @Test
    public void shouldReturnEmptyEventInCaseOfAnIntegrationException() throws Exception {
        String url = transformationUrl.replaceAll("\\@\\{.*}", "value");
        URI uri = URI.create(url);
        when(
            httpGateway.request(
                eq(HttpMethod.POST),
                eq(uri),
                eq(MediaType.APPLICATION_JSON),
                bodyCaptor.capture(),
                eq(transformationServiceUsername),
                eq(transformationServicePassword))
        ).thenThrow(IntegrationException.class);

        Optional<Event> transformed = subject.transform(event,transformation);

        assertThat(transformed,equalTo(Optional.empty()));
    }

    @Test
    public void shouldReturnEmptyEventIfStepResponseBodyIsEmpty() throws Exception {
        when(
            httpGateway.request(
                    eq(HttpMethod.POST),
                    Mockito.any(URI.class),
                    eq(MediaType.APPLICATION_JSON),
                    bodyCaptor.capture(),
                    eq(transformationServiceUsername),
                    eq(transformationServicePassword))
        ).thenReturn("");

        Optional<Event> transformed = subject.transform(event,transformation);
        assertThat(transformed,equalTo(Optional.empty()));
    }

    @Test
    public void shouldReturnEmptyEventIfStepResponseIsAnEmptyJsonArray() throws Exception {
        when(
                httpGateway.request(
                        eq(HttpMethod.POST),
                        Mockito.any(URI.class),
                        eq(MediaType.APPLICATION_JSON),
                        bodyCaptor.capture(),
                        eq(transformationServiceUsername),
                        eq(transformationServicePassword))
        ).thenReturn("[]");

        Optional<Event> transformed = subject.transform(event,transformation);
        assertThat(transformed,equalTo(Optional.empty()));
    }

    @Test
    public void shouldReturnEmptyEventIfStepResponseIsAnEmptyJsonObject() throws Exception {
        when(
                httpGateway.request(
                        eq(HttpMethod.POST),
                        Mockito.any(URI.class),
                        eq(MediaType.APPLICATION_JSON),
                        bodyCaptor.capture(),
                        eq(transformationServiceUsername),
                        eq(transformationServicePassword))
        ).thenReturn("{}");

        Optional<Event> transformed = subject.transform(event,transformation);
        assertThat(transformed,equalTo(Optional.empty()));
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
                        Mockito.any(URI.class),
                        eq(MediaType.APPLICATION_JSON),
                        bodyCaptor.capture(),
                        eq(transformationServiceUsername),
                        eq(transformationServicePassword))
        ).thenReturn(firstStepResponseJson);

        Optional<Event> transformed = subject.transform(event,transformation);

        assertThat(transformed.isPresent(),equalTo(true));
        transformed.ifPresent(e -> {
            assertThat(e.getTimestamp(),equalTo(event.getTimestamp()));
            assertThat(e.getChannel(),equalTo(event.getChannel()));
            assertThat(e.getPayload(),equalTo(firstStepResponseJson));
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
                    eq(firstURI),
                    eq(MediaType.APPLICATION_JSON),
                    bodyCaptor.capture(),
                    eq(transformationServiceUsername),
                    eq(transformationServicePassword))
        ).thenReturn(firstStepResponseJson);

        String secondStepURI = "http://server:8080/service/@{#customerId}/verify";

        List<TransformationStep> steps = new ArrayList<>();
        steps.addAll(transformation.getSteps());
        steps.add(RestTransformationStep.builder()
                .attributes(new HashMap<String,String>() {
                    {
                        put(RestTransformationStep.REST_URL_ATTRIBUTE_NAME,secondStepURI);
                        put(RestTransformationStep.REST_USERNAME_ATTRIBUTE_NAME,"");
                        put(RestTransformationStep.REST_PASSWORD_ATTRIBUTE_NAME,"");
                    }
                }).build());
        transformation.setSteps(steps);

        URI secondURI = URI.create(secondStepURI.replaceAll("\\@\\{.*}", "123456"));

        when(
            httpGateway.request(
                    eq(HttpMethod.POST),
                    eq(secondURI),
                    eq(MediaType.APPLICATION_JSON),
                    bodyCaptor.capture(),
                    eq(""),
                    eq(""))
        ).thenReturn(secondStepResponseJson);

        Optional<Event> transformed = subject.transform(event,transformation);

        assertThat(transformed.isPresent(),equalTo(true));
        transformed.ifPresent(e -> {
            assertThat(e.getTimestamp(),equalTo(event.getTimestamp()));
            assertThat(e.getChannel(),equalTo(event.getChannel()));
            assertThat(e.getPayload(),equalTo(secondStepResponseJson));
        });
    }
}