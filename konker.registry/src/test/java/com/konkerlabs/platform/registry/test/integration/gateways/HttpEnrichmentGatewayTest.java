package com.konkerlabs.platform.registry.test.integration.gateways;

import com.konkerlabs.platform.registry.integration.exceptions.IntegrationException;
import com.konkerlabs.platform.registry.integration.gateways.HttpEnrichmentGatewayImpl;
import com.konkerlabs.platform.registry.test.base.IntegrationLayerTestContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = { IntegrationLayerTestContext.class })
@RunWith(MockitoJUnitRunner.class)
public class HttpEnrichmentGatewayTest {

    private static final String USERNAME = "Username";
    private static final String PASSWORD = "Password";

    private URI uri;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private RestTemplate restTemplate;

    @Captor
    private ArgumentCaptor<HttpEntity<MultiValueMap<String, String>>> httpEntityCaptor;

    private HttpEnrichmentGatewayImpl build(RestTemplate restTemplate) {
        HttpEnrichmentGatewayImpl httpEnrichmentGateway = new HttpEnrichmentGatewayImpl();
        httpEnrichmentGateway.setRestTemplate(restTemplate);
        return httpEnrichmentGateway;
    }

    @Before
    public void setUp() throws URISyntaxException {
        uri = new URI("http://my.enrichment.service:8080/device/000000000001/product");
    }

    @Test
    public void shouldFailWithExceptionIfRestTemplateNotDefined() throws IntegrationException {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("RestTemplate must be provided");

        build(null).get(uri, USERNAME, PASSWORD);

        verifyZeroInteractions(restTemplate);
    }

    @Test
    public void shouldFailWithExceptionIfUriNotDefined() throws IntegrationException {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Service URI must be provided");

        build(restTemplate).get(null, USERNAME, PASSWORD);

        verifyZeroInteractions(restTemplate);
    }

    @Test
    public void shouldFailWithExceptionIfUsernameNotDefined() throws IntegrationException {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Username and Password must be both provided together");

        build(restTemplate).get(uri, null, PASSWORD);

        verifyZeroInteractions(restTemplate);
    }

    @Test
    public void shouldFailWithExceptionIfPasswordNotDefined() throws IntegrationException {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Username and Password must be both provided together");

        build(restTemplate).get(uri, USERNAME, null);

        verifyZeroInteractions(restTemplate);
    }

    @Test
    public void shouldIncludeAuhtorizationHeader() throws IntegrationException {
        when(restTemplate.exchange(eq(uri), eq(HttpMethod.GET), httpEntityCaptor.capture(),
                eq(String.class))).thenReturn(new ResponseEntity<String>(HttpStatus.OK));

        build(restTemplate).get(uri, USERNAME, PASSWORD);

        HttpEntity<MultiValueMap<String, String>> entity = httpEntityCaptor.getValue();
        assertNotNull(entity);

        HttpHeaders headers = entity.getHeaders();
        assertNotNull(headers);

        assertEquals("Basic VXNlcm5hbWU6UGFzc3dvcmQ=", headers.getFirst("Authorization"));
    }

    @Test
    public void shouldNotIncludeAuhtorizationHeader() throws IntegrationException {
        when(restTemplate.exchange(eq(uri), eq(HttpMethod.GET), httpEntityCaptor.capture(),
                eq(String.class))).thenReturn(new ResponseEntity<String>(HttpStatus.OK));

        build(restTemplate).get(uri, null, null);

        HttpEntity<MultiValueMap<String, String>> entity = httpEntityCaptor.getValue();
        assertNotNull(entity);

        HttpHeaders headers = entity.getHeaders();
        assertNotNull(headers);
    }

}
