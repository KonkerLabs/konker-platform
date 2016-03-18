package com.konkerlabs.platform.registry.test.web.forms;

import com.konkerlabs.platform.registry.business.model.EventRoute;
import com.konkerlabs.platform.registry.business.model.EventRoute.RouteActor;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.Transformation;
import com.konkerlabs.platform.registry.business.model.behaviors.DeviceURIDealer;
import com.konkerlabs.platform.registry.business.model.behaviors.RESTDestinationURIDealer;
import com.konkerlabs.platform.registry.business.model.behaviors.SmsDestinationURIDealer;
import com.konkerlabs.platform.registry.business.services.publishers.EventPublisherSms;
import com.konkerlabs.platform.registry.web.forms.EventRouteForm;
import org.apache.commons.collections.map.HashedMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collections;
import java.util.HashMap;

import static com.konkerlabs.platform.registry.business.services.publishers.EventPublisherMqtt.DEVICE_MQTT_CHANNEL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class EventRouteFormTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private EventRouteForm form;
    private EventRoute model;
    private DeviceURIDealer deviceUriDealer;
    private SmsDestinationURIDealer smsDestinationUriDealer;
    private RESTDestinationURIDealer restDestinationURIDealer;
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
        smsDestinationUriDealer = new SmsDestinationURIDealer() {};
        restDestinationURIDealer = new RESTDestinationURIDealer() {};
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
                RouteActor.builder()
                        .uri(deviceUriDealer.toDeviceRouteURI(tenant.getDomainName(),form.getIncomingAuthority()))
                        .data(new HashedMap())
                        .build()
        );
        model.getIncoming().getData().put(DEVICE_MQTT_CHANNEL,form.getIncomingChannel());
        model.setOutgoing(
                RouteActor.builder()
                        .uri(deviceUriDealer.toDeviceRouteURI(tenant.getDomainName(),form.getOutgoingDeviceAuthority()))
                        .data(new HashedMap())
                        .build()
        );
        model.getOutgoing().getData().put(DEVICE_MQTT_CHANNEL,form.getOutgoingDeviceChannel());

        assertThat(form.toModel(),equalTo(model));
    }
    @Test
    public void shouldTranslateFromSMSRouteFormToModel() throws Exception {
        form.setOutgoingScheme("sms");
        form.setOutgoingSmsDestinationGuid("+5511987654321");

        model.setIncoming(RouteActor.builder()
                .uri(deviceUriDealer.toDeviceRouteURI(tenant.getDomainName(),form.getIncomingAuthority()))
                .data(new HashMap())
                .build());
        model.getIncoming().getData().put(DEVICE_MQTT_CHANNEL,form.getIncomingChannel());
        model.setOutgoing(RouteActor.builder()
                .uri(smsDestinationUriDealer.toSmsURI(tenant.getDomainName(), form.getOutgoingSmsDestinationGuid()))
                .data(new HashMap<String, String>(){{put("messageStrategy", null);put("messageTemplate", null);put(EventPublisherSms.SMS_MESSAGE_STRATEGY_PARAMETER_NAME, "forward");}})
                .build());

        assertThat(form.toModel(),equalTo(model));
    }

    @Test
    public void shouldTranslateFromRestDestinationRouteFormToModel() throws Exception {
        form.setOutgoingScheme("rest");
        form.setOutgoingRestDestinationGuid("dda64780-eb81-11e5-958b-a73dab8b32ee");

        model.setIncoming(RouteActor.builder()
                .uri(deviceUriDealer.toDeviceRouteURI(tenant.getDomainName(),form.getIncomingAuthority()))
                .data(new HashedMap())
                .build());
        model.getIncoming().getData().put(DEVICE_MQTT_CHANNEL,form.getIncomingChannel());
        model.setOutgoing(RouteActor.builder()
                .uri(restDestinationURIDealer.toRestDestinationURI(tenant.getDomainName(), form.getOutgoingRestDestinationGuid()))
                .data(new HashedMap())
                .build());

        assertThat(form.toModel(),equalTo(model));
    }

    @Test
    public void shouldTranslateToModelWithOptionalTransformation() throws Exception {
        form.setOutgoingScheme("device");
        form.setOutgoingDeviceAuthority("0000000000000005");
        form.setOutgoingDeviceChannel("in");

        model.setIncoming(RouteActor.builder()
                .uri(deviceUriDealer.toDeviceRouteURI(tenant.getDomainName(),form.getIncomingAuthority()))
                .data(new HashedMap())
                .build()
        );
        model.getIncoming().getData().put(DEVICE_MQTT_CHANNEL,form.getIncomingChannel());
        model.setOutgoing(RouteActor.builder()
                .uri(deviceUriDealer.toDeviceRouteURI(tenant.getDomainName(),form.getOutgoingDeviceAuthority()))
                .data(new HashedMap())
                .build()
        );
        model.getOutgoing().getData().put(DEVICE_MQTT_CHANNEL,form.getOutgoingDeviceChannel());

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

        model.setIncoming(RouteActor.builder()
                .uri(deviceUriDealer.toDeviceRouteURI(tenant.getDomainName(),form.getIncomingAuthority()))
                .data(new HashedMap())
                .build());
        model.getIncoming().getData().put(DEVICE_MQTT_CHANNEL,form.getIncomingChannel());
        model.setOutgoing(RouteActor.builder()
                .uri(deviceUriDealer.toDeviceRouteURI(tenant.getDomainName(),form.getOutgoingDeviceAuthority()))
                .data(new HashedMap())
                .build());
        model.getOutgoing().getData().put(DEVICE_MQTT_CHANNEL,form.getOutgoingDeviceChannel());

        assertThat(new EventRouteForm().fillFrom(model),equalTo(form));
    }

    @Test
    public void shouldTranslateFromSMSRouteModelToForm() throws Exception {
        form.setAdditionalSupplier(null);

        form.setOutgoingScheme("sms");
        form.setOutgoingSmsDestinationGuid("+5511987654321");

        model.setIncoming(RouteActor.builder()
                .uri(deviceUriDealer.toDeviceRouteURI(tenant.getDomainName(),form.getIncomingAuthority()))
                .data(new HashedMap())
                .build());
        model.getIncoming().getData().put(DEVICE_MQTT_CHANNEL,form.getIncomingChannel());
        model.setOutgoing(RouteActor.builder()
                .uri(smsDestinationUriDealer.toSmsURI(tenant.getDomainName(),form.getOutgoingSmsDestinationGuid()))
                .data(Collections.singletonMap(EventPublisherSms.SMS_MESSAGE_STRATEGY_PARAMETER_NAME, EventPublisherSms.SMS_MESSAGE_FORWARD_STRATEGY_PARAMETER_VALUE))
                .build());

        assertThat(new EventRouteForm().fillFrom(model),equalTo(form));
    }

    @Test
    public void shouldTranslateFromRestDestinationRouteModelToForm() throws Exception {
        form.setAdditionalSupplier(null);

        form.setOutgoingScheme("rest");
        form.setOutgoingRestDestinationGuid("dda64780-eb81-11e5-958b-a73dab8b32ee");

        model.setIncoming(RouteActor.builder()
                .uri(deviceUriDealer.toDeviceRouteURI(tenant.getDomainName(),form.getIncomingAuthority()))
                .data(new HashedMap())
                .build());
        model.getIncoming().getData().put(DEVICE_MQTT_CHANNEL,form.getIncomingChannel());
        model.setOutgoing(RouteActor.builder()
                .uri(restDestinationURIDealer.toRestDestinationURI(tenant.getDomainName(), form.getOutgoingRestDestinationGuid()))
                .data(new HashedMap())
                .build());

        assertThat(new EventRouteForm().fillFrom(model),equalTo(form));
    }

    @Test
    public void shouldTranslateFromModelToFormWithOptionalTransformation() throws Exception {
        form.setAdditionalSupplier(null);

        form.setOutgoingScheme("device");
        form.setOutgoingDeviceAuthority("0000000000000005");
        form.setOutgoingDeviceChannel("in");

        model.setIncoming(RouteActor.builder()
                .uri(deviceUriDealer.toDeviceRouteURI(tenant.getDomainName(),form.getIncomingAuthority()))
                .data(new HashedMap())
                .build());
        model.getIncoming().getData().put(DEVICE_MQTT_CHANNEL,form.getIncomingChannel());
        model.setOutgoing(RouteActor.builder()
                .uri(deviceUriDealer.toDeviceRouteURI(tenant.getDomainName(),form.getOutgoingDeviceAuthority()))
                .data(new HashedMap())
                .build());
        model.getOutgoing().getData().put(DEVICE_MQTT_CHANNEL,form.getOutgoingDeviceChannel());

        assertThat(new EventRouteForm().fillFrom(model),equalTo(form));

        //There is no transformation associated with this route

        model.setTransformation(null);
        form.setTransformation(null);

        assertThat(new EventRouteForm().fillFrom(model),equalTo(form));
    }
}