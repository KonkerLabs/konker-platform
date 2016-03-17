package com.konkerlabs.platform.registry.test.business.model.behaviors;

import com.konkerlabs.platform.registry.business.model.behaviors.SmsDestinationURIDealer;
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

    String SMS_URI_SCHEME = "sms";
    String SMS_URI_TEMPLATE = SMS_URI_SCHEME + "://{0}/{1}";

    private String guid;
    private URI uri;
    private SmsDestinationURIDealer subject;
    private String tenantDomain;

    @Before
    public void setUp() throws Exception {
        tenantDomain = "domain";

        guid = UUID.randomUUID().toString();

        uri = URI.create(
            MessageFormat.format(SMS_URI_TEMPLATE,tenantDomain, guid)
        );

        subject = new SmsDestinationURIDealer() {};
    }
    @Test
    public void shouldRaiseAnExceptionIfPhoneNumberIsNull() throws Exception {
        guid = null;

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("SMS GUID cannot be null or empty");

        subject.toSmsURI(tenantDomain, guid);
    }
    @Test
    public void shouldRaiseAnExceptionIfPhoneNumberIsEmpty() throws Exception {
        guid = "";

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("SMS GUID cannot be null or empty");

        subject.toSmsURI(tenantDomain, guid);
    }

    @Test
    public void shouldRaiseAnExceptionIfTenantDomainIsNull() throws Exception {
        tenantDomain = null;

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("SMS Destination tenant domain cannot be null or empty");

        subject.toSmsURI(tenantDomain, guid);
    }
    @Test
    public void shouldRaiseAnExceptionIfTenantDomainIsEmpty() throws Exception {
        tenantDomain = "";

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("SMS Destination tenant domain cannot be null or empty");

        subject.toSmsURI(tenantDomain, guid);
    }

    @Test
    public void shouldGenerateTheSmsURI() throws Exception {
        assertThat(subject.toSmsURI(tenantDomain, guid),equalTo(uri));
    }

}