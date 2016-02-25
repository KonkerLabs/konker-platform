package com.konkerlabs.platform.registry.test.business.model.behaviors;

import com.konkerlabs.platform.registry.business.model.behaviors.DeviceURIDealer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.URI;
import java.text.MessageFormat;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class DeviceURIDealerTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private String tenantDomain;
    private String deviceId;
    private DeviceURIDealer subject;
    private URI uri;

    @Before
    public void setUp() throws Exception {
        tenantDomain = "tenantDomain";
        deviceId = "0000000000000004";

        subject = new DeviceURIDealer() {};

        uri = URI.create(MessageFormat.format(DeviceURIDealer.DEVICE_RULE_URI_TEMPLATE,tenantDomain,deviceId));
    }

    @Test
    public void shouldRaiseAnExceptionIfTenantDomainIsNull() throws Exception {
        tenantDomain = null;

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Tenant domain cannot be null or empty");

        subject.toDeviceRuleURI(tenantDomain,deviceId);
    }
    @Test
    public void shouldRaiseAnExceptionIfTenantDomainIsEmpty() throws Exception {
        tenantDomain = "";

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Tenant domain cannot be null or empty");

        subject.toDeviceRuleURI(tenantDomain,deviceId);
    }
    @Test
    public void shouldRaiseAnExceptionIfDeviceIdIsNull() throws Exception {
        deviceId = null;

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Device ID cannot be null or empty");

        subject.toDeviceRuleURI(tenantDomain,deviceId);
    }
    @Test
    public void shouldRaiseAnExceptionIfDeviceIdIsEmpty() throws Exception {
        deviceId = "";

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Device ID cannot be null or empty");

        subject.toDeviceRuleURI(tenantDomain,deviceId);
    }
    @Test
    public void shouldGenerateTheURI() throws Exception {
        assertThat(subject.toDeviceRuleURI(tenantDomain,deviceId),equalTo(uri));
    }
}