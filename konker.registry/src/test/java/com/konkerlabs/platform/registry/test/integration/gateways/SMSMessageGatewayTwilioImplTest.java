package com.konkerlabs.platform.registry.test.integration.gateways;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.net.URI;
import java.util.function.Supplier;

import com.konkerlabs.platform.registry.integration.gateways.HttpGateway;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;

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
    private HttpGateway httpGateway;

    @Captor
    private ArgumentCaptor<Supplier<MultiValueMap<String, String>>> formCaptor;

    private SMSMessageGatewayTwilioImpl build(URI uri, String username, String password, String fromNumber,
            HttpGateway httpGateway) {
        SMSMessageGatewayTwilioImpl s = new SMSMessageGatewayTwilioImpl();
        s.setApiUri(uri);
        s.setUsername(username);
        s.setFromPhoneNumber(fromNumber);
        s.setPassword(password);
        s.setHttpGateway(httpGateway);
        return s;
    }

    @Before
    public void setUp() throws Exception {
        apiUri = new URI("http://a");
    }

    @Test
    public void shouldFailWithExceptionIfRestTemplateNotDefined() throws IntegrationException {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("HTTP gateway must be provided");

        build(apiUri, USERNAME, PASSWORD, FROM_NUMBER, null).send("a", "+111111111");

        verifyZeroInteractions(httpGateway);
    }

    @Test
    public void shouldFailWithExceptionIfUriIsNull() throws IntegrationException {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("API URI must be provided");

        build(null, USERNAME, PASSWORD, FROM_NUMBER, httpGateway).send("a", "+111111111");

        verifyZeroInteractions(httpGateway);
    }

    @Test
    public void shouldFailWithExceptionIfUsernameIsNull() throws IntegrationException {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("API Username must be provided");

        build(apiUri, null, PASSWORD, FROM_NUMBER, httpGateway).send("a", "+111111111");

        verifyZeroInteractions(httpGateway);
    }

    @Test
    public void shouldFailWithExceptionIfUsernameIsEmpty() throws IntegrationException {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("API Username must be provided");

        build(apiUri, "", PASSWORD, FROM_NUMBER, httpGateway).send("a", "+111111111");

        verifyZeroInteractions(httpGateway);
    }

    @Test
    public void shouldFailWithExceptionIfPasswordIsNull() throws IntegrationException {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("API Password must be provided");

        build(apiUri, USERNAME, null, FROM_NUMBER, httpGateway).send("a", "+111111111");

        verifyZeroInteractions(httpGateway);
    }

    @Test
    public void shouldFailWithExceptionIfPasswordIsEmpty() throws IntegrationException {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("API Password must be provided");

        build(apiUri, USERNAME, "", FROM_NUMBER, httpGateway).send("a", "+111111111");

        verifyZeroInteractions(httpGateway);
    }

    @Test
    public void shouldFailWithExceptionIfFromIsNull() throws IntegrationException {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("From Phone Number must be provided");

        build(apiUri, USERNAME, PASSWORD, null, httpGateway).send("a", "+111111111");

        verifyZeroInteractions(httpGateway);
    }

    @Test
    public void shouldFailWithExceptionIfFromIsEmpty() throws IntegrationException {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("From Phone Number must be provided");

        build(apiUri, USERNAME, PASSWORD, "", httpGateway).send("a", "+111111111");

        verifyZeroInteractions(httpGateway);
    }

    @Test
    public void shouldFailWithExceptionIfDestinationIsEmpty() throws IntegrationException {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Destination Number must be provided");

        build(apiUri, USERNAME, PASSWORD, FROM_NUMBER, httpGateway).send("a", "");

        verifyZeroInteractions(httpGateway);
    }


    @Test
    public void shouldFailWithExceptionIfDestinationIsNull() throws IntegrationException {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Destination Number must be provided");

        build(apiUri, USERNAME, PASSWORD, FROM_NUMBER, httpGateway).send("a", null);

        verifyZeroInteractions(httpGateway);
    }


    @Test
    public void shouldFailWithExceptionIfBodyIsNull() throws IntegrationException {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("SMS Body must be provided");

        build(apiUri, USERNAME, PASSWORD, FROM_NUMBER, httpGateway).send(null, "+1");

        verifyZeroInteractions(httpGateway);
    }


    @Test
    public void shouldFailWithExceptionIfBodyIsEmpty() throws IntegrationException {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("SMS Body must be provided");

        build(apiUri, USERNAME, PASSWORD, FROM_NUMBER, httpGateway).send("", "+1");

        verifyZeroInteractions(httpGateway);
    }

    @Test
    public void shouldRaiseIntegrationExceptionIfPostFails() throws IntegrationException {
        thrown.expect(IntegrationException.class);

        Mockito.when(httpGateway.request(eq(HttpMethod.POST),eq(apiUri),anyObject(),eq(USERNAME),eq(PASSWORD),eq(HttpStatus.CREATED)))
                .thenThrow(new RestClientException("Dummy Exception"));
        build(apiUri, USERNAME, PASSWORD, FROM_NUMBER, httpGateway).send("SMS Text Body", "+2");
    }

    @Test
    public void shouldPostToApiIfDataIsCorrect() throws IntegrationException {
        build(apiUri, USERNAME, PASSWORD, FROM_NUMBER, httpGateway).send("SMS Text Body", "+2");

        verify(httpGateway).request(eq(HttpMethod.POST),anyObject(),formCaptor.capture(),eq(USERNAME),eq(PASSWORD),eq(HttpStatus.CREATED));

        Supplier<MultiValueMap<String, String>> entity = formCaptor.getValue();
        assertNotNull(entity);

        MultiValueMap<String, String> body = entity.get();
        assertEquals(FROM_NUMBER, body.getFirst("From"));
        assertEquals("+2", body.getFirst("To"));
        assertEquals("SMS Text Body", body.getFirst("Body"));
    }
}