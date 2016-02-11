package com.konkerlabs.platform.registry.test.integration.gateways;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.net.URI;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.konkerlabs.platform.registry.integration.exceptions.IntegrationException;
import com.konkerlabs.platform.registry.integration.gateways.SMSMessageGatewayTwilioImpl;
import com.konkerlabs.platform.registry.test.base.IntegrationLayerTestContext;
import com.konkerlabs.platform.registry.test.base.IntegrationLayerTestSupport;

@ContextConfiguration(classes = { IntegrationLayerTestContext.class })
@RunWith(MockitoJUnitRunner.class)
public class SMSMessageGatewayTwilioImplTest extends IntegrationLayerTestSupport {
    private static final String USERNAME = "Username";
    private static final String PASSWORD = "Password";
    private static final String FROM_NUMBER = "+123456";

    private URI apiUri;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private RestTemplate restTemplate;

    @Captor
    private ArgumentCaptor<HttpEntity<MultiValueMap<String, String>>> httpEntityCaptor;

    private SMSMessageGatewayTwilioImpl build(URI uri, String username, String password, String fromNumber,
            RestTemplate restTemplate) {
        SMSMessageGatewayTwilioImpl s = new SMSMessageGatewayTwilioImpl();
        s.setApiUri(uri);
        s.setUsername(username);
        s.setFromPhoneNumber(fromNumber);
        s.setPassword(password);
        s.setRestTemplate(restTemplate);
        return s;
    }

    @Before
    public void setUp() throws Exception {
        apiUri = new URI("http://a");
    }

    @Test
    public void shouldFailWithExceptionIfRestTemplateNotDefined() throws IntegrationException {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("RestTemplate must be provided");

        build(apiUri, USERNAME, PASSWORD, FROM_NUMBER, null).send("a", "+111111111");

        verifyZeroInteractions(restTemplate);
    }

    @Test
    public void shouldFailWithExceptionIfUriIsNull() throws IntegrationException {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("API URI must be provided");

        build(null, USERNAME, PASSWORD, FROM_NUMBER, restTemplate).send("a", "+111111111");

        verifyZeroInteractions(restTemplate);
    }

    @Test
    public void shouldFailWithExceptionIfUsernameIsNull() throws IntegrationException {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("API Username must be provided");

        build(apiUri, null, PASSWORD, FROM_NUMBER, restTemplate).send("a", "+111111111");

        verifyZeroInteractions(restTemplate);
    }
    
    @Test
    public void shouldFailWithExceptionIfUsernameIsEmpty() throws IntegrationException {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("API Username must be provided");

        build(apiUri, "", PASSWORD, FROM_NUMBER, restTemplate).send("a", "+111111111");

        verifyZeroInteractions(restTemplate);
    }
    
    @Test
    public void shouldFailWithExceptionIfPasswordIsNull() throws IntegrationException {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("API Password must be provided");

        build(apiUri, USERNAME, null, FROM_NUMBER, restTemplate).send("a", "+111111111");

        verifyZeroInteractions(restTemplate);
    }
    
    @Test
    public void shouldFailWithExceptionIfPasswordIsEmpty() throws IntegrationException {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("API Password must be provided");

        build(apiUri, USERNAME, "", FROM_NUMBER, restTemplate).send("a", "+111111111");

        verifyZeroInteractions(restTemplate);
    }
    
    @Test
    public void shouldFailWithExceptionIfFromIsNull() throws IntegrationException {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("From Phone Number must be provided");

        build(apiUri, USERNAME, PASSWORD, null, restTemplate).send("a", "+111111111");

        verifyZeroInteractions(restTemplate);
    }
    
    @Test
    public void shouldFailWithExceptionIfFromIsEmpty() throws IntegrationException {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("From Phone Number must be provided");

        build(apiUri, USERNAME, PASSWORD, "", restTemplate).send("a", "+111111111");

        verifyZeroInteractions(restTemplate);
    }
    
    
    @Test
    public void shouldIncludeAuhtorizationHeader() throws IntegrationException {
        build(apiUri, USERNAME, PASSWORD, FROM_NUMBER, restTemplate).send("a", "+2");

        verify(restTemplate).postForLocation(anyObject(), httpEntityCaptor.capture());

        HttpEntity<MultiValueMap<String, String>> entity = httpEntityCaptor.getValue();
        assertNotNull(entity);

        HttpHeaders headers = entity.getHeaders();
        assertNotNull(headers);

        assertEquals("Basic VXNlcm5hbWU6UGFzc3dvcmQ=", headers.getFirst("Authorization"));
    }

    @Test
    public void shouldRaiseIntegrationExceptionIfPostFails() throws IntegrationException {
        thrown.expect(IntegrationException.class);
        
        Mockito.when(restTemplate.postForLocation(anyObject(), anyObject())).thenThrow(new RestClientException("Dummy Exception"));
        build(apiUri, USERNAME, PASSWORD, FROM_NUMBER, restTemplate).send("SMS Text Body", "+2");
        verify(restTemplate).postForLocation(anyObject(), anyObject());
    }
    
    @Test
    public void shouldPostToApiIfDataIsCorrect() throws IntegrationException {
        build(apiUri, USERNAME, PASSWORD, FROM_NUMBER, restTemplate).send("SMS Text Body", "+2");

        verify(restTemplate).postForLocation(eq(apiUri), httpEntityCaptor.capture());

        HttpEntity<MultiValueMap<String, String>> entity = httpEntityCaptor.getValue();
        assertNotNull(entity);

        MultiValueMap<String, String> body = entity.getBody();
        assertEquals(FROM_NUMBER, body.getFirst("From"));
        assertEquals("+2", body.getFirst("To"));
        assertEquals("SMS Text Body", body.getFirst("Body"));
    }

}
