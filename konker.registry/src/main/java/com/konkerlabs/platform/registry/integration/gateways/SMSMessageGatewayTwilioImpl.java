package com.konkerlabs.platform.registry.integration.gateways;

import java.net.URI;
import java.text.MessageFormat;
import java.util.Optional;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.konkerlabs.platform.registry.integration.exceptions.IntegrationException;

@Data
public class SMSMessageGatewayTwilioImpl implements SMSMessageGateway {
    private static final Logger LOGGER = LoggerFactory.getLogger(SMSMessageGatewayTwilioImpl.class);

//    private RestTemplate restTemplate;
    private HttpGateway httpGateway;
    private String username;
    private String password;
    private URI apiUri;
    private String fromPhoneNumber;

//    public RestTemplate getRestTemplate() {
//        return restTemplate;
//    }
//
//    public void setRestTemplate(RestTemplate restTemplate) {
//        this.restTemplate = restTemplate;
//    }

//    public String getUsername() {
//        return username;
//    }
//
//    public void setUsername(String username) {
//        this.username = username;
//    }
//
//    public String getPassword() {
//        return password;
//    }
//
//    public void setPassword(String password) {
//        this.password = password;
//    }
//
//    public URI getApiUri() {
//        return apiUri;
//    }
//
//    public void setApiUri(URI apiUri) {
//        this.apiUri = apiUri;
//    }
//
//    public String getFromPhoneNumber() {
//        return fromPhoneNumber;
//    }
//
//    public void setFromPhoneNumber(String fromPhoneNumber) {
//        this.fromPhoneNumber = fromPhoneNumber;
//    }

    private void addAuthorizationHeaders(MultiValueMap<String, String> headers) {
        String encodedCredentials = Base64Utils
                .encodeToString(MessageFormat.format("{0}:{1}", username, password).getBytes());

        headers.add("Authorization", MessageFormat.format("Basic {0}", encodedCredentials));
    }

    @Override
    public void send(final String text, final String destinationPhoneNumber) throws IntegrationException {

        Optional.ofNullable(destinationPhoneNumber).filter(n -> n.trim().length() > 0)
                .orElseThrow(() -> new IllegalArgumentException("Destination Number must be provided"));

        Optional.ofNullable(text).filter(n -> n.trim().length() > 0)
                .orElseThrow(() -> new IllegalArgumentException("SMS Body must be provided"));

        Optional.ofNullable(username).filter(n -> n.trim().length() > 0)
                .orElseThrow(() -> new IllegalStateException("API Username must be provided"));

        Optional.ofNullable(password).filter(n -> n.trim().length() > 0)
                .orElseThrow(() -> new IllegalStateException("API Password must be provided"));

        Optional.ofNullable(fromPhoneNumber).filter(n -> n.trim().length() > 0)
                .orElseThrow(() -> new IllegalStateException("From Phone Number must be provided"));

        Optional.ofNullable(apiUri).orElseThrow(() -> new IllegalStateException("API URI must be provided"));

        Optional.ofNullable(httpGateway)
                .orElseThrow(() -> new IllegalStateException("HTTP gateway must be provided"));

        try {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>();
            form.add("To", destinationPhoneNumber);
            form.add("Body", text);
            form.add("From", fromPhoneNumber);

//            MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
//            addAuthorizationHeaders(headers);
//
//            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<MultiValueMap<String, String>>(form,
//                    headers);

            LOGGER.debug("Sending SMS Message [{}] to [{}].", text, destinationPhoneNumber);

//            restTemplate.postForLocation(apiUri, entity);
            httpGateway.request(HttpMethod.POST,apiUri,() -> form,username,password);

        } catch (RestClientException rce) {
            throw new IntegrationException(
                    MessageFormat.format("Exception while sending {0} to {1}", text, destinationPhoneNumber), rce);
        }
    }
}
