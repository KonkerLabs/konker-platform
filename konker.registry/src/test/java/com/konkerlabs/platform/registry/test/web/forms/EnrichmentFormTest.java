package com.konkerlabs.platform.registry.test.web.forms;


import com.konkerlabs.platform.registry.business.model.DataEnrichmentExtension;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.behaviors.DeviceURIDealer;
import com.konkerlabs.platform.registry.web.forms.EnrichmentForm;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.URI;
import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class EnrichmentFormTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private EnrichmentForm form;
    private DataEnrichmentExtension model;
    private DeviceURIDealer deviceUriDealer;
    private Tenant tenant;
    private String deviceId = "1";


    @Before
    public void setUp() {
        form = new EnrichmentForm();
        form.setName("EnrichmentTest1");
        form.setDescription("Testing the enrichment form.");
        form.setType("REST");
        form.setIncomingAuthority(deviceId);
        form.setParameters(new HashMap<String, String>(){{put("URL", "http://my.enriching.service.com");put("User", "admin");put("Password", "secret");}});
        form.setContainerKey("fieldTest");
        form.setActive(true);

        tenant = Tenant.builder().name("tenantName").domainName("tenantDomain").build();

        form.setAdditionalSupplier(() -> tenant.getDomainName());

        deviceUriDealer = new DeviceURIDealer() {};

        model = DataEnrichmentExtension.builder()
                .name(form.getName())
                .description(form.getDescription())
                .type(DataEnrichmentExtension.EnrichmentType.REST)
                .incoming(deviceUriDealer.toDeviceRuleURI(tenant.getDomainName(), deviceId))
                .parameters(form.getParameters())
                .containerKey(form.getContainerKey())
                .active(form.isActive())
                .build();
    }

    @Test
    public void shouldRaiseAnExceptionIfTenantDomainNameSupplierIsNull() throws Exception {
        form.setAdditionalSupplier(null);

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Tenant domain name supplier cannot be null");

        form.toModel();
    }

    @Test
    public void shouldRaiseAnExceptionIfTenantDomainNameSupplierReturnsNull() throws Exception {
        form.setAdditionalSupplier(() -> null);

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Tenant domain name supplier cannot return null or empty");

        form.toModel();
    }

    @Test
    public void shouldRaiseAnExceptionIfTenantDomainNameSupplierReturnsAnEmptyString() throws Exception {
        form.setAdditionalSupplier(() -> "");

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Tenant domain name supplier cannot return null or empty");

        form.toModel();
    }

    @Test
    public void shouldTranslateFromFormToModel() {
        form.setAdditionalSupplier(() -> tenant.getDomainName());

        assertThat(form.toModel(),equalTo(model));
    }

    @Test
    public void shouldTranslateFromModelToForm() {
        assertThat(new EnrichmentForm().fillFrom(model),equalTo(form));
    }
}
