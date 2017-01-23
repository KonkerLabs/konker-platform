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
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Supplier;

import static java.text.MessageFormat.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
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
    private HttpGateway httpGateway;
    private HttpMethod method;
    private HttpHeaders headers;
    private MediaType mediaType = MediaType.APPLICATION_JSON;

    @Before
    public void setUp() throws URISyntaxException {
        MockitoAnnotations.initMocks(this);
        headers = new HttpHeaders();
        method = HttpMethod.GET;
        uri = new URI("http://my.enrichment.service:8080/device/000000000001/product");
    }

    @Test
    public void shoulRaiseAnExceptionIfHttpMethodIsNull() throws IntegrationException {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("HTTP method must be provided");

        httpGateway.request(null, null, null, mediaType, () -> null, USERNAME, PASSWORD);

        verifyZeroInteractions(restTemplate);
    }

    @Test
    public void shouldFailWithExceptionIfUriNotDefined() throws IntegrationException {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Service URI must be provided");

        httpGateway.request(method, null, null, mediaType, () -> null, USERNAME, PASSWORD);

        verifyZeroInteractions(restTemplate);
    }

    @Test
    public void shouldFailWithExceptionIfUsernameNotDefined() throws IntegrationException {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Username and Password must be both provided together");

        httpGateway.request(method, headers, uri, mediaType, () -> null, null, PASSWORD);

        verifyZeroInteractions(restTemplate);
    }

    @Test
    public void shouldFailWithExceptionIfPasswordNotDefined() throws IntegrationException {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Username and Password must be both provided together");

        httpGateway.request(method, headers, uri, mediaType, () -> null, USERNAME, null);

        verifyZeroInteractions(restTemplate);
    }

    @Test
    public void shouldIncludeAuhtorizationHeader() throws IntegrationException {
        when(restTemplate.exchange(eq(uri), eq(HttpMethod.GET), httpEntityCaptor.capture(),
                eq(String.class))).thenReturn(new ResponseEntity<String>(HttpStatus.OK));

        httpGateway.request(method, headers, uri, mediaType, () -> null, USERNAME, PASSWORD);

        HttpEntity<String> entity = httpEntityCaptor.getValue();
        assertThat(entity,notNullValue());

        HttpHeaders headers = entity.getHeaders();
        assertThat(headers,notNullValue());

        assertThat(headers.getFirst("Authorization"), equalTo("Basic VXNlcm5hbWU6UGFzc3dvcmQ="));
    }

    @Test
    public void shouldAddKonkerVersionHeader() throws IntegrationException {
        when(restTemplate.exchange(eq(uri), eq(HttpMethod.GET), httpEntityCaptor.capture(),
                eq(String.class))).thenReturn(new ResponseEntity<String>(HttpStatus.OK));

        httpGateway.request(method, headers, uri, mediaType, () -> null, "", PASSWORD);

        HttpEntity<String> entity = httpEntityCaptor.getValue();
        assertThat(entity,notNullValue());

        HttpHeaders headers = entity.getHeaders();
        assertThat(headers,notNullValue());

        assertThat(headers.get(HttpGateway.KONKER_VERSION_HEADER), notNullValue());
    }
    
    @Test
    public void shouldIncludeAuhtorizationHeaderWithEmptyUser() throws IntegrationException {
        when(restTemplate.exchange(eq(uri), eq(HttpMethod.GET), httpEntityCaptor.capture(),
                eq(String.class))).thenReturn(new ResponseEntity<String>(HttpStatus.OK));

        httpGateway.request(method, headers, uri, mediaType, () -> null, "", PASSWORD);

        HttpEntity<String> entity = httpEntityCaptor.getValue();
        assertThat(entity,notNullValue());

        HttpHeaders headers = entity.getHeaders();
        assertThat(headers,notNullValue());

        assertThat(headers.getFirst("Authorization"), equalTo("Basic OlBhc3N3b3Jk"));
    }

    @Test
    public void shouldIncludeAuhtorizationHeaderWithEmptyPasword() throws IntegrationException {
        when(restTemplate.exchange(eq(uri), eq(HttpMethod.GET), httpEntityCaptor.capture(),
                eq(String.class))).thenReturn(new ResponseEntity<String>(HttpStatus.OK));

        httpGateway.request(method, headers, uri, mediaType, () -> null, USERNAME, "");

        HttpEntity<String> entity = httpEntityCaptor.getValue();
        assertThat(entity,notNullValue());

        HttpHeaders headers = entity.getHeaders();
        assertThat(headers,notNullValue());

        assertThat(headers.getFirst("Authorization"), equalTo("Basic VXNlcm5hbWU6"));
    }

    @Test
    public void shouldNotIncludeAuhtorizationHeader() throws IntegrationException {
        when(restTemplate.exchange(eq(uri), eq(HttpMethod.GET), httpEntityCaptor.capture(),
                eq(String.class))).thenReturn(new ResponseEntity<String>(HttpStatus.OK));

        httpGateway.request(method, headers, uri, mediaType, () -> null, null, null);

        HttpEntity<String> entity = httpEntityCaptor.getValue();
        assertThat(entity,notNullValue());

        HttpHeaders headers = entity.getHeaders();
        assertThat(headers,notNullValue());

        assertThat(headers.getFirst("Authorization"), nullValue());
    }

    @Test
    public void shouldNotIncludeAuhtorizationHeaderForEmptyUserAndPassword() throws IntegrationException {
        when(restTemplate.exchange(eq(uri), eq(HttpMethod.GET), httpEntityCaptor.capture(),
                eq(String.class))).thenReturn(new ResponseEntity<String>(HttpStatus.OK));

        httpGateway.request(method, headers, uri, mediaType, () -> null, "", "");

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

        httpGateway.request(method, headers, uri, mediaType, body, USERNAME, PASSWORD);

        HttpEntity<String> entity = httpEntityCaptor.getValue();
        assertThat(entity,notNullValue());

        assertThat(entity.getBody(),equalTo(body.get()));
    }

    @Test
    public void shouldApplyPOSTHttpMethodWhenProvided() throws IntegrationException {
        method = HttpMethod.POST;

        when(restTemplate.exchange(eq(uri), eq(method), httpEntityCaptor.capture(),
                eq(String.class))).thenReturn(new ResponseEntity<String>(HttpStatus.OK));

        httpGateway.request(method, headers, uri, mediaType, null, USERNAME, PASSWORD);

        verify(restTemplate).exchange(eq(uri), eq(method), httpEntityCaptor.capture(),
                eq(String.class));
    }

    @Test
    public void shouldRaiseAnExceptionIfResponseStatusIsNotA2xxStatus() throws Exception {
        String errorBody = "Server error";

        thrown.expect(IntegrationException.class);
        thrown.expectMessage(
            format("Exception while requesting GET from {0}. Status Code: {1}. Message: {2}.",
                    uri,HttpStatus.INTERNAL_SERVER_ERROR.value(),errorBody
        ));

        when(restTemplate.exchange(eq(uri), eq(method), httpEntityCaptor.capture(),
                eq(String.class))).thenReturn(new ResponseEntity<String>(errorBody,HttpStatus.INTERNAL_SERVER_ERROR));

        httpGateway.request(method, headers, uri, mediaType, null, USERNAME, PASSWORD);
    }
}
