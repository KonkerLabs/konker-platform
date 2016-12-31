package com.konkerlabs.platform.registry.test.business.model.behaviors;

import com.konkerlabs.platform.registry.business.model.SmsDestination;
import com.konkerlabs.platform.registry.business.model.behaviors.SmsDestinationURIDealer;
import com.konkerlabs.platform.registry.business.model.behaviors.URIDealer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.URI;
import java.text.MessageFormat;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class SmsDestinationURIDealerTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private String guid;
    private URI uri;
    private URIDealer subject;
    private String tenantDomain;

    @Before
    public void setUp() throws Exception {
        tenantDomain = "domain";

        guid = UUID.randomUUID().toString();

        uri = URI.create(
            MessageFormat.format(
                    SmsDestination.URI_TEMPLATE,
                    SmsDestination.URI_SCHEME, tenantDomain,
                    guid)
        );

        subject = new URIDealer() {
            @Override
            public String getUriScheme() {
                return SmsDestination.URI_SCHEME;
            }

            @Override
            public String getContext() {
                return tenantDomain;
            }

            @Override
            public String getGuid() {
                return guid;
            }
        };
    }
    @Test
    public void shouldRaiseAnExceptionIfPhoneNumberIsNull() throws Exception {
        guid = null;

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("GUID cannot be null or empty");

        subject.toURI();
    }
    @Test
    public void shouldRaiseAnExceptionIfPhoneNumberIsEmpty() throws Exception {
        guid = "";

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("GUID cannot be null or empty");

        subject.toURI();
    }

    @Test
    public void shouldRaiseAnExceptionIfTenantDomainIsNull() throws Exception {
        tenantDomain = null;

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("CONTEXT cannot be null or empty");

        subject.toURI();
    }
    @Test
    public void shouldRaiseAnExceptionIfTenantDomainIsEmpty() throws Exception {
        tenantDomain = "";

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("CONTEXT cannot be null or empty");

        subject.toURI();
    }

    @Test
    public void shouldGenerateTheSmsURI() throws Exception {
        assertThat(subject.toURI(),equalTo(uri));
    }

}