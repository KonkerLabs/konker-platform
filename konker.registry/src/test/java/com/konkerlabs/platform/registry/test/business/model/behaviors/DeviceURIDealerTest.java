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

    String DEVICE_URI_SCHEME = "device";
    String DEVICE_ROUTE_URI_TEMPLATE = DEVICE_URI_SCHEME + "://{0}/{1}";

    private String tenantDomain;
    private String deviceGuid;
    private DeviceURIDealer subject;
    private URI uri;

    @Before
    public void setUp() throws Exception {
        tenantDomain = "tenantDomain";
        deviceGuid = "22821842-7438-4c46-8bb2-5a2f56cd8923";

        subject = new DeviceURIDealer() {};

        uri = URI.create(MessageFormat.format(DEVICE_ROUTE_URI_TEMPLATE,tenantDomain,deviceGuid));
    }

    @Test
    public void shouldRaiseAnExceptionIfTenantDomainIsNull() throws Exception {
        tenantDomain = null;

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Tenant domain cannot be null or empty");

        subject.toDeviceRouteURI(tenantDomain,deviceGuid);
    }
    @Test
    public void shouldRaiseAnExceptionIfTenantDomainIsEmpty() throws Exception {
        tenantDomain = "";

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Tenant domain cannot be null or empty");

        subject.toDeviceRouteURI(tenantDomain,deviceGuid);
    }
    @Test
    public void shouldRaiseAnExceptionIfDeviceIdIsNull() throws Exception {
        deviceGuid = null;

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Device GUID cannot be null or empty");

        subject.toDeviceRouteURI(tenantDomain,deviceGuid);
    }
    @Test
    public void shouldRaiseAnExceptionIfDeviceIdIsEmpty() throws Exception {
        deviceGuid = "";

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Device GUID cannot be null or empty");

        subject.toDeviceRouteURI(tenantDomain,deviceGuid);
    }
    @Test
    public void shouldGenerateTheURI() throws Exception {
        assertThat(subject.toDeviceRouteURI(tenantDomain,deviceGuid),equalTo(uri));
    }
}