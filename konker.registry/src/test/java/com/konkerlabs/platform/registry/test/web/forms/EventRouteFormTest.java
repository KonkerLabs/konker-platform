package com.konkerlabs.platform.registry.test.web.forms;

import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.model.EventRoute.RouteActor;
import com.konkerlabs.platform.registry.business.model.behaviors.RESTDestinationURIDealer;
import com.konkerlabs.platform.registry.business.model.behaviors.SmsDestinationURIDealer;
import com.konkerlabs.platform.registry.business.model.behaviors.URIDealer;
import com.konkerlabs.platform.registry.web.forms.EventRouteForm;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class EventRouteFormTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private EventRouteForm form;
    private EventRoute model;
    private Tenant tenant;

    @Before
    public void setUp() {
        form = new EventRouteForm();
        form.setName("route_name");
        form.setDescription("route_description");
        form.getIncoming().setAuthorityId("0000000000000004");
        form.getIncoming().getAuthorityData().put("channel", "command");
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
        form.getOutgoing().setAuthorityId("0000000000000005");
        form.getOutgoing().getAuthorityData().put("channel", "in");

        model.setIncoming(
                RouteActor.builder()
                        .uri(new URIDealer() {
                            @Override
                            public String getUriScheme() {
                                return Device.URI_SCHEME;
                            }

                            @Override
                            public String getContext() {
                                return tenant.getDomainName();
                            }

                            @Override
                            public String getGuid() {
                                return form.getIncoming().getAuthorityId();
                            }
                        }.toURI())
                        .data(form.getIncoming().getAuthorityData())
                        .build()
        );
        model.setOutgoing(
                RouteActor.builder()
                        .uri(new URIDealer() {
                            @Override
                            public String getUriScheme() {
                                return Device.URI_SCHEME;
                            }

                            @Override
                            public String getContext() {
                                return tenant.getDomainName();
                            }

                            @Override
                            public String getGuid() {
                                return form.getOutgoing().getAuthorityId();
                            }
                        }.toURI())
                        .data(form.getOutgoing().getAuthorityData())
                        .build()
        );

        assertThat(form.toModel(), equalTo(model));
    }

    @Test
    public void shouldTranslateFromSMSRouteFormToModel() throws Exception {
        form.setOutgoingScheme("sms");
        form.getOutgoing().setAuthorityId("407902f6-5b85-4767-ad83-b8b3e847bd92");
        form.getOutgoing().getAuthorityData().put("messageStrategy", "custom");
        form.getOutgoing().getAuthorityData().put("messageTemplate", "A given message template");

        model.setIncoming(RouteActor.builder()
                .uri(new URIDealer() {
                    @Override
                    public String getUriScheme() {
                        return Device.URI_SCHEME;
                    }

                    @Override
                    public String getContext() {
                        return tenant.getDomainName();
                    }

                    @Override
                    public String getGuid() {
                        return form.getIncoming().getAuthorityId();
                    }
                }.toURI())
                .data(form.getIncoming().getAuthorityData())
                .build());
        model.setOutgoing(RouteActor.builder()
                .uri(new URIDealer() {
                    @Override
                    public String getUriScheme() {
                        return SmsDestinationURIDealer.SMS_URI_SCHEME;
                    }

                    @Override
                    public String getContext() {
                        return tenant.getDomainName();
                    }

                    @Override
                    public String getGuid() {
                        return form.getOutgoing().getAuthorityId();
                    }
                }.toURI())
                .data(new HashMap<String, String>() {{
                    put(EventRoute.SMS_MESSAGE_STRATEGY_PARAMETER_NAME, "custom");
                    put(EventRoute.SMS_MESSAGE_TEMPLATE_PARAMETER_NAME, "A given message template");
                }})
                .build());

        assertThat(form.toModel(), equalTo(model));
    }

    @Test
    public void shouldTranslateFromRestDestinationRouteFormToModel() throws Exception {
        form.setOutgoingScheme("rest");
        form.getOutgoing().setAuthorityId("dda64780-eb81-11e5-958b-a73dab8b32ee");

        model.setIncoming(RouteActor.builder()
                .uri(new URIDealer() {
                    @Override
                    public String getUriScheme() {
                        return Device.URI_SCHEME;
                    }

                    @Override
                    public String getContext() {
                        return tenant.getDomainName();
                    }

                    @Override
                    public String getGuid() {
                        return form.getIncoming().getAuthorityId();
                    }
                }.toURI())
                .data(form.getIncoming().getAuthorityData())
                .build());
        model.setOutgoing(RouteActor.builder()
                .uri(new URIDealer() {
                    @Override
                    public String getUriScheme() {
                        return RESTDestinationURIDealer.REST_DESTINATION_URI_SCHEME;
                    }

                    @Override
                    public String getContext() {
                        return tenant.getDomainName();
                    }

                    @Override
                    public String getGuid() {
                        return form.getOutgoing().getAuthorityId();
                    }
                }.toURI())
                .data(form.getOutgoing().getAuthorityData())
                .build());

        assertThat(form.toModel(), equalTo(model));
    }

    @Test
    public void shouldTranslateToModelWithOptionalTransformation() throws Exception {
        form.setOutgoingScheme("device");
        form.getOutgoing().setAuthorityId("0000000000000005");
        form.getOutgoing().getAuthorityData().put("channel", "in");

        model.setIncoming(RouteActor.builder()
                .uri(new URIDealer() {
                    @Override
                    public String getUriScheme() {
                        return Device.URI_SCHEME;
                    }

                    @Override
                    public String getContext() {
                        return tenant.getDomainName();
                    }

                    @Override
                    public String getGuid() {
                        return form.getIncoming().getAuthorityId();
                    }
                }.toURI())
                .data(form.getIncoming().getAuthorityData())
                .build()
        );
        model.setOutgoing(RouteActor.builder()
                .uri(new URIDealer() {
                    @Override
                    public String getUriScheme() {
                        return Device.URI_SCHEME;
                    }

                    @Override
                    public String getContext() {
                        return tenant.getDomainName();
                    }

                    @Override
                    public String getGuid() {
                        return form.getOutgoing().getAuthorityId();
                    }
                }.toURI())
                .data(form.getOutgoing().getAuthorityData())
                .build()
        );

        assertThat(form.toModel(), equalTo(model));

        //No transformation is selected

        form.setTransformation(null);
        model.setTransformation(null);

        assertThat(form.toModel(), equalTo(model));
    }

    @Test
    public void shouldTranslateFromDeviceRouteModelToForm() throws Exception {
        form.setOutgoingScheme("device");
        form.getOutgoing().setAuthorityId("0000000000000005");
        form.getOutgoing().getAuthorityData().put("channel", "in");

        model.setIncoming(RouteActor.builder()
                .uri(new URIDealer() {
                    @Override
                    public String getUriScheme() {
                        return Device.URI_SCHEME;
                    }

                    @Override
                    public String getContext() {
                        return tenant.getDomainName();
                    }

                    @Override
                    public String getGuid() {
                        return form.getIncoming().getAuthorityId();
                    }
                }.toURI())
                .data(form.getIncoming().getAuthorityData())
                .build());
        model.setOutgoing(RouteActor.builder()
                .uri(new URIDealer() {
                    @Override
                    public String getUriScheme() {
                        return Device.URI_SCHEME;
                    }

                    @Override
                    public String getContext() {
                        return tenant.getDomainName();
                    }

                    @Override
                    public String getGuid() {
                        return form.getOutgoing().getAuthorityId();
                    }
                }.toURI())
                .data(form.getOutgoing().getAuthorityData())
                .build());

        assertThat(new EventRouteForm().fillFrom(model), equalTo(form));
    }

    @Test
    public void shouldTranslateFromSMSRouteModelToForm() throws Exception {
        form.setOutgoingScheme("sms");
        form.getOutgoing().setAuthorityId("407902f6-5b85-4767-ad83-b8b3e847bd92");
        form.getOutgoing().getAuthorityData().put("smsMessageStrategy", "custom");
        form.getOutgoing().getAuthorityData().put("smsMessageTemplate", "A given message template");

        model.setIncoming(RouteActor.builder()
                .uri(new URIDealer() {
                    @Override
                    public String getUriScheme() {
                        return Device.URI_SCHEME;
                    }

                    @Override
                    public String getContext() {
                        return tenant.getDomainName();
                    }

                    @Override
                    public String getGuid() {
                        return form.getIncoming().getAuthorityId();
                    }
                }.toURI())
                .data(form.getIncoming().getAuthorityData())
                .build());
        model.setOutgoing(RouteActor.builder()
                .uri(new URIDealer() {
                    @Override
                    public String getUriScheme() {
                        return SmsDestination.URI_SCHEME;
                    }

                    @Override
                    public String getContext() {
                        return tenant.getDomainName();
                    }

                    @Override
                    public String getGuid() {
                        return form.getOutgoing().getAuthorityId();
                    }
                }.toURI())
                .data(form.getOutgoing().getAuthorityData())
                .build());

        assertThat(new EventRouteForm().fillFrom(model), equalTo(form));
    }

    @Test
    public void shouldTranslateFromRestDestinationRouteModelToForm() throws Exception {
        form.setOutgoingScheme("rest");
        form.getOutgoing().setAuthorityId("dda64780-eb81-11e5-958b-a73dab8b32ee");

        model.setIncoming(RouteActor.builder()
                .uri(new URIDealer() {
                    @Override
                    public String getUriScheme() {
                        return Device.URI_SCHEME;
                    }

                    @Override
                    public String getContext() {
                        return tenant.getDomainName();
                    }

                    @Override
                    public String getGuid() {
                        return form.getIncoming().getAuthorityId();
                    }
                }.toURI())
                .data(form.getIncoming().getAuthorityData())
                .build());
        model.setOutgoing(RouteActor.builder()
                .uri(new URIDealer() {
                    @Override
                    public String getUriScheme() {
                        return RestDestination.URI_SCHEME;
                    }

                    @Override
                    public String getContext() {
                        return tenant.getDomainName();
                    }

                    @Override
                    public String getGuid() {
                        return form.getOutgoing().getAuthorityId();
                    }
                }.toURI())
                .data(form.getOutgoing().getAuthorityData())
                .build());

        assertThat(new EventRouteForm().fillFrom(model), equalTo(form));
    }

    @Test
    public void shouldTranslateFromModelToFormWithOptionalTransformation() throws Exception {
        form.setOutgoingScheme("device");
        form.getOutgoing().setAuthorityId("0000000000000005");
        form.getOutgoing().getAuthorityData().put("channel", "in");

        model.setIncoming(RouteActor.builder()
                .uri(new URIDealer() {
                    @Override
                    public String getUriScheme() {
                        return Device.URI_SCHEME;
                    }

                    @Override
                    public String getContext() {
                        return tenant.getDomainName();
                    }

                    @Override
                    public String getGuid() {
                        return form.getIncoming().getAuthorityId();
                    }
                }.toURI())
                .data(form.getIncoming().getAuthorityData())
                .build());
        model.setOutgoing(RouteActor.builder()
                .uri(new URIDealer() {
                    @Override
                    public String getUriScheme() {
                        return Device.URI_SCHEME;
                    }

                    @Override
                    public String getContext() {
                        return tenant.getDomainName();
                    }

                    @Override
                    public String getGuid() {
                        return form.getOutgoing().getAuthorityId();
                    }
                }.toURI())
                .data(form.getOutgoing().getAuthorityData())
                .build());

        assertThat(new EventRouteForm().fillFrom(model), equalTo(form));

        //There is no transformation associated with this route

        model.setTransformation(null);
        form.setTransformation(null);

        assertThat(new EventRouteForm().fillFrom(model), equalTo(form));
    }
}