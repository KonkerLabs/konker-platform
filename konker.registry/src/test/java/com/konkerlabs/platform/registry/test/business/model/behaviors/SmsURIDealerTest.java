package com.konkerlabs.platform.registry.test.business.model.behaviors;

import com.konkerlabs.platform.registry.business.model.behaviors.SmsURIDealer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.URI;
import java.text.MessageFormat;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class SmsURIDealerTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private String phoneNumber;
    private URI uri;
    private SmsURIDealer subject;

    @Before
    public void setUp() throws Exception {
        phoneNumber = "+5599987654321";

        uri = URI.create(
            MessageFormat.format(SmsURIDealer.SMS_URI_TEMPLATE,phoneNumber)
        );

        subject = new SmsURIDealer() {};
    }
    @Test
    public void shouldRaiseAnExceptionIfPhoneNumberIsNull() throws Exception {
        phoneNumber = null;

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("SMS Phone number cannot be null or empty");

        subject.toSmsURI(phoneNumber);
    }
    @Test
    public void shouldRaiseAnExceptionIfPhoneNumberIsEmpty() throws Exception {
        phoneNumber = "";

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("SMS Phone number cannot be null or empty");

        subject.toSmsURI(phoneNumber);
    }
    @Test
    public void shouldGenerateTheSmsURI() throws Exception {
        assertThat(subject.toSmsURI(phoneNumber),equalTo(uri));
    }

}