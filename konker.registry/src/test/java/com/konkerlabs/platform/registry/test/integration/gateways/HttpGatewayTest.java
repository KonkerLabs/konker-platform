package com.konkerlabs.platform.registry.test.integration.gateways;

import com.konkerlabs.platform.registry.integration.exceptions.IntegrationException;
import com.konkerlabs.platform.registry.integration.gateways.HttpGateway;
import com.konkerlabs.platform.registry.test.base.IntegrationLayerTestContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    IntegrationLayerTestContext.class
})
public class HttpGatewayTest {

    private static final String USERNAME = "Username";
    private static final String PASSWORD = "Password";

    private URI uri;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Captor
    private ArgumentCaptor<HttpEntity<String>> httpEntityCaptor;

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private HttpGateway enrichmentGateway;
    private HttpMethod method;

    @Before
    public void setUp() throws URISyntaxException {
        MockitoAnnotations.initMocks(this);

        method = HttpMethod.GET;
        uri = new URI("http://my.enrichment.service:8080/device/000000000001/product");
    }

    @Test
    public void shoulRaiseAnExceptionIfHttpMethodIsNull() throws IntegrationException {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("HTTP method must be provided");

        enrichmentGateway.request(null, null, () -> null, USERNAME, PASSWORD);

        verifyZeroInteractions(restTemplate);
    }

    @Test
    public void shouldFailWithExceptionIfUriNotDefined() throws IntegrationException {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Service URI must be provided");

        enrichmentGateway.request(method, null, () -> null, USERNAME, PASSWORD);

        verifyZeroInteractions(restTemplate);
    }

    @Test
    public void shouldFailWithExceptionIfUsernameNotDefined() throws IntegrationException {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Username and Password must be both provided together");

        enrichmentGateway.request(method, uri, () -> null, null, PASSWORD);

        verifyZeroInteractions(restTemplate);
    }

    @Test
    public void shouldFailWithExceptionIfPasswordNotDefined() throws IntegrationException {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Username and Password must be both provided together");

        enrichmentGateway.request(method, uri, () -> null, USERNAME, null);

        verifyZeroInteractions(restTemplate);
    }

    @Test
    public void shouldIncludeAuhtorizationHeader() throws IntegrationException {
        when(restTemplate.exchange(eq(uri), eq(HttpMethod.GET), httpEntityCaptor.capture(),
                eq(String.class))).thenReturn(new ResponseEntity<String>(HttpStatus.OK));

        enrichmentGateway.request(method, uri, () -> null, USERNAME, PASSWORD);

        HttpEntity<String> entity = httpEntityCaptor.getValue();
        assertThat(entity,notNullValue());

        HttpHeaders headers = entity.getHeaders();
        assertThat(headers,notNullValue());

        assertThat(headers.getFirst("Authorization"), equalTo("Basic VXNlcm5hbWU6UGFzc3dvcmQ="));
    }

    @Test
    public void shouldNotIncludeAuhtorizationHeader() throws IntegrationException {
        when(restTemplate.exchange(eq(uri), eq(HttpMethod.GET), httpEntityCaptor.capture(),
                eq(String.class))).thenReturn(new ResponseEntity<String>(HttpStatus.OK));

        enrichmentGateway.request(method, uri, () -> null, null, null);

        HttpEntity<String> entity = httpEntityCaptor.getValue();
        assertThat(entity,notNullValue());

        HttpHeaders headers = entity.getHeaders();
        assertThat(headers,notNullValue());

        assertThat(headers.getFirst("Authorization"), nullValue());
    }

    @Test
    public void shouldApplyRequestBodyWhenProvided() throws IntegrationException {
        Supplier<String> body = () -> "requestBody";

        when(restTemplate.exchange(eq(uri), eq(HttpMethod.GET), httpEntityCaptor.capture(),
                eq(String.class))).thenReturn(new ResponseEntity<String>(HttpStatus.OK));

        enrichmentGateway.request(method, uri, body, USERNAME, PASSWORD);

        HttpEntity<String> entity = httpEntityCaptor.getValue();
        assertThat(entity,notNullValue());

        assertThat(entity.getBody(),equalTo(body.get()));
    }

    @Test
    public void shouldApplyPOSTHttpMethodWhenProvided() throws IntegrationException {
        method = HttpMethod.POST;

        when(restTemplate.exchange(eq(uri), eq(method), httpEntityCaptor.capture(),
                eq(String.class))).thenReturn(new ResponseEntity<String>(HttpStatus.OK));

        enrichmentGateway.request(method, uri, null, USERNAME, PASSWORD);

        verify(restTemplate).exchange(eq(uri), eq(method), httpEntityCaptor.capture(),
                eq(String.class));
    }
}
