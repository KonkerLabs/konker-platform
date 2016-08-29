package com.konkerlabs.platform.registry.test.business.model;

import com.konkerlabs.platform.registry.business.model.DataEnrichmentExtension;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.enumerations.IntegrationType;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.utilities.validations.InterpolableURIValidator;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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

        String expectedMessage = DataEnrichmentExtension.Validations.NAME_NULL.getCode();
        Optional<Map<String,Object[]>> validations = dee.applyValidations();

        assertThat(validations.isPresent(),is(true));
        assertThat(validations.get(), hasEntry(expectedMessage,null));
    }

    @Test
    public void shouldReturnAValidationMessageIfNameIsEmpty() throws Exception {
        dee.setName("");

        String expectedMessage = DataEnrichmentExtension.Validations.NAME_NULL.getCode();
        Optional<Map<String,Object[]>> validations = dee.applyValidations();

        assertThat(validations.isPresent(),is(true));
        assertThat(validations.get(), hasEntry(expectedMessage,null));
    }

    @Test
    public void shouldNotReturnAValidationMessageIfDescriptionIsNull() throws Exception {
        dee.setDescription(null);

        Optional<Map<String,Object[]>> validations = dee.applyValidations();

        assertThat(validations.isPresent(),is(false));
    }

    @Test
    public void shouldReturnAValidationMessageIfTenantIsNull() throws Exception {
        dee.setTenant(null);

        String expectedMessage = CommonValidations.TENANT_NULL.getCode();
        Optional<Map<String,Object[]>> validations = dee.applyValidations();

        assertThat(validations.isPresent(),is(true));
        assertThat(validations.get(), hasEntry(expectedMessage,null));
    }

    @Test
    public void shouldReturnAValidationMessageIfIncomingDeviceIsNull() throws Exception {
        dee.setIncoming(null);

        String expectedMessage = DataEnrichmentExtension.Validations.INCOMING_DEVICE_NULL.getCode();
        Optional<Map<String,Object[]>> validations = dee.applyValidations();

        assertThat(validations.isPresent(),is(true));
        assertThat(validations.get(), hasEntry(expectedMessage,null));
    }

    @Test
    public void shouldReturnAValidationMessageIfIncomingDeviceUriSchemeIsNotDevice() throws Exception {
        dee.setIncoming(new URI("sms://+55555555555555555555555"));

        String expectedMessage = DataEnrichmentExtension.Validations.INCOMING_URI_NOT_A_DEVICE.getCode();
        Optional<Map<String,Object[]>> validations = dee.applyValidations();

        assertThat(validations.isPresent(),is(true));
        assertThat(validations.get(), hasEntry(expectedMessage,null));
    }

    @Test
    public void shouldReturnAValidationMessageIfParametersIsNull() throws Exception {
        dee.setParameters(null);

        String expectedMessage = DataEnrichmentExtension.Validations.PARAMETERS_NULL.getCode();
        Optional<Map<String,Object[]>> validations = dee.applyValidations();

        assertThat(validations.isPresent(),is(true));
        assertThat(validations.get(), hasEntry(expectedMessage,null));
    }

    @Test
    public void shouldReturnAValidationMessageIfURIIsNull() throws Exception {
        dee.getParameters().put(DataEnrichmentExtension.URL,null);

        String expectedMessage = DataEnrichmentExtension.Validations.SERVICE_URL_MISSING.getCode();
        Optional<Map<String,Object[]>> validations = dee.applyValidations();

        assertThat(validations.isPresent(),is(true));
        assertThat(validations.get(), hasEntry(expectedMessage,null));
    }

    @Test
    public void shouldReturnAValidationMessageIfURIIsEmpty() throws Exception {
        dee.getParameters().put(DataEnrichmentExtension.URL,"");

        String expectedMessage = DataEnrichmentExtension.Validations.SERVICE_URL_MISSING.getCode();
        Optional<Map<String,Object[]>> validations = dee.applyValidations();

        assertThat(validations.isPresent(),is(true));
        assertThat(validations.get(), hasEntry(expectedMessage,null));
    }

    @Test
    public void shouldReturnAValidationMessageIfPassowrdIsSetAndUsernameIsNull() throws Exception {
        dee.getParameters().put(DataEnrichmentExtension.USERNAME,null);

        String expectedMessage = DataEnrichmentExtension.Validations.SERVICE_USERNAME_WITHOUT_PASSWORD.getCode();
        Optional<Map<String,Object[]>> validations = dee.applyValidations();

        assertThat(validations.isPresent(),is(true));
        assertThat(validations.get(), hasEntry(expectedMessage,null));
    }

    @Test
    public void shouldReturnAValidationMessageIfPassowrdIsSetAndUsernameIsEmpty() throws Exception {
        dee.getParameters().put(DataEnrichmentExtension.USERNAME,"   ");

        String expectedMessage = DataEnrichmentExtension.Validations.SERVICE_USERNAME_WITHOUT_PASSWORD.getCode();
        Optional<Map<String,Object[]>> validations = dee.applyValidations();

        assertThat(validations.isPresent(),is(true));
        assertThat(validations.get(), hasEntry(expectedMessage,null));
    }

    @Test
    public void shouldHaveNoValidationMessagesIfUsernameIsSetButPasswordIsNull() throws Exception {
        dee.getParameters().put(DataEnrichmentExtension.PASSWORD,null);
        assertThat(dee.applyValidations().isPresent(),is(false));
    }

    @Test
    public void shouldReturnAValidationMessageIfInterpolatesHost() throws Exception {
        String url = "http://@{#var}/";
        dee.getParameters().put(DataEnrichmentExtension.URL,url);

        String expectedMessage = InterpolableURIValidator.Validations.URI_MALFORMED.getCode();
        Optional<Map<String,Object[]>> validations = dee.applyValidations();

        assertThat(validations.isPresent(),is(true));
        assertThat(validations.get(), hasEntry(expectedMessage,new Object[] {url}));
    }

    @Test
    public void shouldHaveNoValidationMessagesIfInterpolatesPath() throws Exception {
        dee.getParameters().put(DataEnrichmentExtension.URL,"http://host/@{#var}/");

        assertThat(dee.applyValidations().isPresent(),is(false));
    }

    @Test
    public void shouldHaveNoValidationMessagesIfUsernameIsSetButPasswordIsEmpty() throws Exception {
        dee.getParameters().put(DataEnrichmentExtension.PASSWORD,"   ");
        assertThat(dee.applyValidations().isPresent(),is(false));
    }

    @Test
    public void shouldHaveNoValidationMessagesIfUsernameAndPasswordAreNull() throws Exception {
        dee.getParameters().put(DataEnrichmentExtension.USERNAME,null);
        dee.getParameters().put(DataEnrichmentExtension.PASSWORD,null);
        assertThat(dee.applyValidations().isPresent(),is(false));
    }

    @Test
    public void shouldHaveNoValidationMessagesIfUsernameAndPasswordAreEmpty() throws Exception {
        dee.getParameters().put(DataEnrichmentExtension.USERNAME,"");
        dee.getParameters().put(DataEnrichmentExtension.PASSWORD,"");
        assertThat(dee.applyValidations().isPresent(),is(false));
    }

    @Test
    public void shouldReturnAValidationMessageIfContainerKeyIsNull() throws Exception {
        dee.setContainerKey(null);

        String expectedMessage = DataEnrichmentExtension.Validations.CONTAINER_KEY_NOT_EMPTY.getCode();
        Optional<Map<String,Object[]>> validations = dee.applyValidations();

        assertThat(validations.isPresent(),is(true));
        assertThat(validations.get(), hasEntry(expectedMessage,null));
    }

    @Test
    public void shouldReturnAValidationMessageIfContainerKeyIsEmpty() throws Exception {
        dee.setContainerKey("");

        String expectedMessage = DataEnrichmentExtension.Validations.CONTAINER_KEY_NOT_EMPTY.getCode();
        Optional<Map<String,Object[]>> validations = dee.applyValidations();

        assertThat(validations.isPresent(),is(true));
        assertThat(validations.get(), hasEntry(expectedMessage,null));
    }
}