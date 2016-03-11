package com.konkerlabs.platform.registry.test.integration.gateways;

import com.konkerlabs.platform.registry.integration.exceptions.IntegrationException;
import com.konkerlabs.platform.registry.integration.gateways.HttpEnrichmentGateway;
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
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    IntegrationLayerTestContext.class
})
public class HttpEnrichmentGatewayTest {

    private static final String USERNAME = "Username";
    private static final String PASSWORD = "Password";

    private URI uri;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Captor
    private ArgumentCaptor<HttpEntity<MultiValueMap<String, String>>> httpEntityCaptor;

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private HttpEnrichmentGateway enrichmentGateway;

    @Before
    public void setUp() throws URISyntaxException {
        MockitoAnnotations.initMocks(this);

        uri = new URI("http://my.enrichment.service:8080/device/000000000001/product");
    }

    @Test
    public void shouldFailWithExceptionIfUriNotDefined() throws IntegrationException {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Service URI must be provided");

        enrichmentGateway.get(null, USERNAME, PASSWORD);

        verifyZeroInteractions(restTemplate);
    }

    @Test
    public void shouldFailWithExceptionIfUsernameNotDefined() throws IntegrationException {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Username and Password must be both provided together");

        enrichmentGateway.get(uri, null, PASSWORD);

        verifyZeroInteractions(restTemplate);
    }

    @Test
    public void shouldFailWithExceptionIfPasswordNotDefined() throws IntegrationException {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Username and Password must be both provided together");

        enrichmentGateway.get(uri, USERNAME, null);

        verifyZeroInteractions(restTemplate);
    }

    @Test
    public void shouldIncludeAuhtorizationHeader() throws IntegrationException {
        when(restTemplate.exchange(eq(uri), eq(HttpMethod.GET), httpEntityCaptor.capture(),
                eq(String.class))).thenReturn(new ResponseEntity<String>(HttpStatus.OK));

        enrichmentGateway.get(uri, USERNAME, PASSWORD);

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

        enrichmentGateway.get(uri, null, null);

        HttpEntity<MultiValueMap<String, String>> entity = httpEntityCaptor.getValue();
        assertNotNull(entity);

        HttpHeaders headers = entity.getHeaders();
        assertNotNull(headers);
    }

}
