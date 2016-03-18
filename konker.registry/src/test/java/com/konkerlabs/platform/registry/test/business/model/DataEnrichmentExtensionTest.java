package com.konkerlabs.platform.registry.test.business.model;

import com.konkerlabs.platform.registry.business.model.DataEnrichmentExtension;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.enumerations.IntegrationType;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class DataEnrichmentExtensionTest {
    private DataEnrichmentExtension dee;

    private static final String A_DATA_ENRICHMENT_EXTENSION_NAME = "Product Details Enrichment";
    private static final String A_DATA_ENRICHMENT_EXTENSION_DESCRIPTION = "Enrich Data using a External REST Service based on device ID with the Product Details";
    private static final String A_DATA_ENRICHMENT_EXTENSION_CONTAINER_KEY = "productData";

    private URI incomingDeviceUri;
    private Map<String, String> parameters;

    @Before
    public void setUp() throws URISyntaxException {

        Tenant tenant = Tenant.builder().id(UUID.randomUUID().toString()).name("Tenant name").build();

        incomingDeviceUri = new URI("device://mydeviceid");
        parameters = new HashMap<String, String>();
        parameters.put("URL", "http://www.google.com/${device.id}/");
        parameters.put("User", "user");
        parameters.put("Password", "pass");

        dee = DataEnrichmentExtension.builder().name(A_DATA_ENRICHMENT_EXTENSION_NAME)
                .description(A_DATA_ENRICHMENT_EXTENSION_DESCRIPTION).tenant(tenant).active(true)
                .containerKey(A_DATA_ENRICHMENT_EXTENSION_CONTAINER_KEY).incoming(incomingDeviceUri).type(IntegrationType.REST)
                .build();

        dee.setParameters(parameters);
    }

    @Test
    public void shouldReturnAValidationMessageIfNameIsNull() throws Exception {
        dee.setName(null);

        String expectedMessage = "Name cannot be null or empty";

        assertThat(dee.applyValidations(), hasItem(expectedMessage));
    }

    @Test
    public void shouldReturnAValidationMessageIfNameIsEmpty() throws Exception {
        dee.setName("");

        String expectedMessage = "Name cannot be null or empty";

        assertThat(dee.applyValidations(), hasItem(expectedMessage));
    }

    @Test
    public void shouldNotReturnAValidationMessageIfDescriptionIsNull() throws Exception {
        dee.setDescription(null);

        assertThat(dee.applyValidations(), emptyIterable());
    }

    @Test
    public void shouldReturnAValidationMessageIfTenantIsNull() throws Exception {
        dee.setTenant(null);

        String expectedMessage = "Tenant cannot be null";

        assertThat(dee.applyValidations(), hasItem(expectedMessage));
    }

    @Test
    public void shouldReturnAValidationMessageIfIncomingDeviceIsNull() throws Exception {
        dee.setIncoming(null);

        String expectedMessage = "Incoming device cannot be null";

        assertThat(dee.applyValidations(), hasItem(expectedMessage));
    }

    @Test
    public void shouldReturnAValidationMessageIfIncomingDeviceUriSchemeIsNotDevice() throws Exception {
        dee.setIncoming(new URI("sms://+55555555555555555555555"));

        String expectedMessage = "Incoming must be a device";

        assertThat(dee.applyValidations(), hasItem(expectedMessage));
    }

    @Test
    public void shouldReturnAValidationMessageIfParametersIsNull() throws Exception {
        dee.setParameters(null);

        String expectedMessage = "Parameters cannot be null";

        assertThat(dee.applyValidations(), hasItem(expectedMessage));
    }

    @Test
    public void shouldReturnAValidationMessageIfURIIsNull() throws Exception {
        dee.getParameters().put(DataEnrichmentExtension.URL,null);

        String expectedMessage = "Service URL cannot be null or empty";

        assertThat(dee.applyValidations(), hasItem(expectedMessage));
    }

    @Test
    public void shouldReturnAValidationMessageIfURIIsEmpty() throws Exception {
        dee.getParameters().put(DataEnrichmentExtension.URL,"");

        String expectedMessage = "Service URL cannot be null or empty";

        assertThat(dee.applyValidations(), hasItem(expectedMessage));
    }

    @Test
    public void shouldReturnAValidationMessageIfPassowrdIsSetAndUsernameIsNull() throws Exception {
        dee.getParameters().put(DataEnrichmentExtension.USERNAME,null);
        assertThat(dee.applyValidations(), hasItem("Password is set but username is empty"));
    }

    @Test
    public void shouldReturnAValidationMessageIfPassowrdIsSetAndUsernameIsEmpty() throws Exception {
        dee.getParameters().put(DataEnrichmentExtension.USERNAME,"   ");
        assertThat(dee.applyValidations(), hasItem("Password is set but username is empty"));
    }

    @Test
    public void shouldHaveNoValidationMessagesIfUsernameIsSetButPasswordIsNull() throws Exception {
        dee.getParameters().put(DataEnrichmentExtension.PASSWORD,null);
        assertThat(dee.applyValidations(), empty());
    }

    @Test
    public void shouldReturnAValidationMessageIfInterpolatesHost() throws Exception {
        dee.getParameters().put(DataEnrichmentExtension.URL,"http://@{#var}/");
        assertThat(dee.applyValidations(), not(empty()));
    }

    @Test
    public void shouldHaveNoValidationMessagesIfInterpolatesPath() throws Exception {
        dee.getParameters().put(DataEnrichmentExtension.URL,"http://host/@{#var}/");
        assertThat(dee.applyValidations(), empty());
    }

    @Test
    public void shouldHaveNoValidationMessagesIfUsernameIsSetButPasswordIsEmpty() throws Exception {
        dee.getParameters().put(DataEnrichmentExtension.PASSWORD,"   ");
        assertThat(dee.applyValidations(), empty());
    }

    @Test
    public void shouldHaveNoValidationMessagesIfUsernameAndPasswordAreNull() throws Exception {
        dee.getParameters().put(DataEnrichmentExtension.USERNAME,null);
        dee.getParameters().put(DataEnrichmentExtension.PASSWORD,null);
        assertThat(dee.applyValidations(), empty());
    }

    @Test
    public void shouldHaveNoValidationMessagesIfUsernameAndPasswordAreEmpty() throws Exception {
        dee.getParameters().put(DataEnrichmentExtension.USERNAME,"");
        dee.getParameters().put(DataEnrichmentExtension.PASSWORD,"");
        assertThat(dee.applyValidations(), empty());
    }

    @Test
    public void shouldReturnAValidationMessageIfContainerKeyIsNull() throws Exception {
        dee.setContainerKey(null);

        String expectedMessage = "Container key cannot be null or empty";

        assertThat(dee.applyValidations(), hasItem(expectedMessage));
    }

    @Test
    public void shouldReturnAValidationMessageIfContainerKeyIsEmpty() throws Exception {
        dee.setContainerKey("");

        String expectedMessage = "Container key cannot be null or empty";

        assertThat(dee.applyValidations(), hasItem(expectedMessage));
    }
}