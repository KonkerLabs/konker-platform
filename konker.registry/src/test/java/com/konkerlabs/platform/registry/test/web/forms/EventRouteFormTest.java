package com.konkerlabs.platform.registry.test.web.forms;

import com.konkerlabs.platform.registry.business.model.EventRoute;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.Transformation;
import com.konkerlabs.platform.registry.business.model.behaviors.DeviceURIDealer;
import com.konkerlabs.platform.registry.business.model.behaviors.SmsURIDealer;
import com.konkerlabs.platform.registry.business.services.routes.EventRouteExecutorImpl;
import com.konkerlabs.platform.registry.web.forms.EventRouteForm;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class EventRouteFormTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private EventRouteForm form;
    private EventRoute model;
    private DeviceURIDealer deviceUriDealer;
    private SmsURIDealer smsUriDealer;
    private Tenant tenant;

    @Before
    public void setUp() {
        form = new EventRouteForm();
        form.setName("route_name");
        form.setDescription("route_description");
        form.setIncomingAuthority("0000000000000004");
        form.setIncomingChannel("command");
        form.setFilteringExpression("#command.type == 'ButtonPressed'");
        form.setTransformation("trans_id");
        form.setActive(true);

        tenant = Tenant.builder().name("tenantName").domainName("tenantDomain").build();

        form.setAdditionalSupplier(() -> tenant.getDomainName());

        model = EventRoute.builder()
                .name(form.getName())
                .description(form.getDescription())
                .filteringExpression("#command.type == 'ButtonPressed'")
                .transformation(Transformation.builder().id("trans_id").build())
                .active(form.isActive()).build();

        deviceUriDealer = new DeviceURIDealer() {};
        smsUriDealer = new SmsURIDealer() {};
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
    public void shouldTranslateFromDeviceRouteFormToModel() throws Exception {
        form.setOutgoingScheme("device");
        form.setOutgoingDeviceAuthority("0000000000000005");
        form.setOutgoingDeviceChannel("in");

        model.setIncoming(
            new EventRoute.RuleActor(deviceUriDealer.toDeviceRouteURI(
                tenant.getDomainName(),form.getIncomingAuthority()
            ))
        );
        model.getIncoming().getData().put("channel",form.getIncomingChannel());
        model.setOutgoing(
            new EventRoute.RuleActor(deviceUriDealer.toDeviceRouteURI(
                tenant.getDomainName(),form.getOutgoingDeviceAuthority()
            ))
        );
        model.getOutgoing().getData().put("channel",form.getOutgoingDeviceChannel());

        assertThat(form.toModel(),equalTo(model));
    }
    @Test
    public void shouldTranslateFromSMSRouteFormToModel() throws Exception {
        form.setOutgoingScheme("sms");
        form.setOutgoingSmsPhoneNumber("+5511987654321");

        model.setIncoming(new EventRoute.RuleActor(
            deviceUriDealer.toDeviceRouteURI(tenant.getDomainName(),form.getIncomingAuthority())
        ));
        model.getIncoming().getData().put("channel",form.getIncomingChannel());
        model.setOutgoing(new EventRoute.RuleActor(
            smsUriDealer.toSmsURI(form.getOutgoingSmsPhoneNumber())
        ));

        assertThat(form.toModel(),equalTo(model));
    }

    @Test
    public void shouldTranslateToModelWithOptionalTransformation() throws Exception {
        form.setOutgoingScheme("device");
        form.setOutgoingDeviceAuthority("0000000000000005");
        form.setOutgoingDeviceChannel("in");

        model.setIncoming(
                new EventRoute.RuleActor(deviceUriDealer.toDeviceRouteURI(
                        tenant.getDomainName(),form.getIncomingAuthority()
                ))
        );
        model.getIncoming().getData().put("channel",form.getIncomingChannel());
        model.setOutgoing(
                new EventRoute.RuleActor(deviceUriDealer.toDeviceRouteURI(
                        tenant.getDomainName(),form.getOutgoingDeviceAuthority()
                ))
        );
        model.getOutgoing().getData().put("channel",form.getOutgoingDeviceChannel());

        assertThat(form.toModel(),equalTo(model));

        //No transformation is selected

        form.setTransformation(null);
        model.setTransformation(null);

        assertThat(form.toModel(),equalTo(model));
    }

    @Test
    public void shouldTranslateFromDeviceRouteModelToForm() throws Exception {
        form.setAdditionalSupplier(null);

        form.setOutgoingScheme("device");
        form.setOutgoingDeviceAuthority("0000000000000005");
        form.setOutgoingDeviceChannel("in");

        model.setIncoming(new EventRoute.RuleActor(
            deviceUriDealer.toDeviceRouteURI(tenant.getDomainName(),form.getIncomingAuthority())
        ));
        model.getIncoming().getData().put("channel",form.getIncomingChannel());
        model.setOutgoing(new EventRoute.RuleActor(
            deviceUriDealer.toDeviceRouteURI(tenant.getDomainName(),form.getOutgoingDeviceAuthority())
        ));
        model.getOutgoing().getData().put("channel",form.getOutgoingDeviceChannel());

        assertThat(new EventRouteForm().fillFrom(model),equalTo(form));
    }

    @Test
    public void shouldTranslateFromSMSRouteModelToForm() throws Exception {
        form.setAdditionalSupplier(null);

        form.setOutgoingScheme("sms");
        form.setOutgoingSmsPhoneNumber("+5511987654321");

        model.setIncoming(new EventRoute.RuleActor(
            deviceUriDealer.toDeviceRouteURI(tenant.getDomainName(),form.getIncomingAuthority())
        ));
        model.getIncoming().getData().put("channel",form.getIncomingChannel());
        model.setOutgoing(new EventRoute.RuleActor(
            smsUriDealer.toSmsURI(form.getOutgoingSmsPhoneNumber())
        ));

        assertThat(new EventRouteForm().fillFrom(model),equalTo(form));
    }

    @Test
    public void shouldTranslateFromModelToFormWithOptionalTransformation() throws Exception {
        form.setAdditionalSupplier(null);

        form.setOutgoingScheme("device");
        form.setOutgoingDeviceAuthority("0000000000000005");
        form.setOutgoingDeviceChannel("in");

        model.setIncoming(new EventRoute.RuleActor(
                deviceUriDealer.toDeviceRouteURI(tenant.getDomainName(),form.getIncomingAuthority())
        ));
        model.getIncoming().getData().put("channel",form.getIncomingChannel());
        model.setOutgoing(new EventRoute.RuleActor(
                deviceUriDealer.toDeviceRouteURI(tenant.getDomainName(),form.getOutgoingDeviceAuthority())
        ));
        model.getOutgoing().getData().put("channel",form.getOutgoingDeviceChannel());

        assertThat(new EventRouteForm().fillFrom(model),equalTo(form));

        //There is no transformation associated with this route

        model.setTransformation(null);
        form.setTransformation(null);

        assertThat(new EventRouteForm().fillFrom(model),equalTo(form));
    }
}