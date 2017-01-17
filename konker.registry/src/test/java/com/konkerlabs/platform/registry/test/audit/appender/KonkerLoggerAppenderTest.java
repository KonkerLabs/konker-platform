package com.konkerlabs.platform.registry.test.audit.appender;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import com.konkerlabs.platform.registry.audit.KonkerLoggerAppender;
import com.konkerlabs.platform.registry.audit.repositories.TenantLogRepository;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.enumerations.LogLevel;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.test.base.MongoTestConfiguration;
import com.konkerlabs.platform.registry.test.base.RedisTestConfiguration;
import org.apache.http.ExceptionLogger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;




@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {BusinessTestConfiguration.class,
        MongoTestConfiguration.class,
        RedisTestConfiguration.class})
public class KonkerLoggerAppenderTest {


    private Tenant tenant;
    private Device device;
    private Logger LOG = (Logger) LoggerFactory.getLogger(KonkerLoggerAppenderTest.class);
    private KonkerLoggerAppender konkerLoggerAppender;
    @Autowired
    private TenantLogRepository tenantLogRepository;

    @Before
    public void setup() throws NoSuchFieldException, IllegalAccessException {
        tenant = Tenant.builder().domainName("testDomain").build();
        device = Device.builder()
                .deviceId("1")
                .guid("testGuid")
                .apiKey("testKey")
                .active(true)
                .tenant(tenant)
                .logLevel(LogLevel.INFO).build();


        konkerLoggerAppender = Mockito.spy(new KonkerLoggerAppender(tenantLogRepository));

    }

    @After
    public void tearDown() {

    }

    @Test
    public void shouldNotCaptureLogTentantAwareLogWhenEventLevelIsDifferentFromDeviceLevel() {
        ILoggingEvent logEvent = new LoggingEvent(
                LOG.getName(),
                LOG,
                Level.DEBUG,
                "Test Message",
                null,
                new Object[]{device.toURI(), device.getLogLevel()}
        );

        konkerLoggerAppender.append(logEvent);
        Mockito.verify(konkerLoggerAppender, Mockito.never())
                .store(Mockito.any(),
                        Mockito.anyString(),
                        Mockito.anyString());
    }

    @Test
    public void shouldNotCaptureTenantAwareLogWhenEventHasNoURI(){
        ILoggingEvent logEvent = new LoggingEvent(
                LOG.getName(),
                LOG,
                Level.DEBUG,
                "Test Message",
                null,
                new Object[]{device.getLogLevel()}
        );

        konkerLoggerAppender.append(logEvent);
        Mockito.verify(konkerLoggerAppender, Mockito.never())
                .store(Mockito.any(),
                        Mockito.anyString(),
                        Mockito.anyString());
    }

    @Test
    public void shouldNotCaptureTenantAwareLogWhenEventHasNoContextLogLevel(){
        ILoggingEvent logEvent = new LoggingEvent(
                LOG.getName(),
                LOG,
                Level.DEBUG,
                "Test Message",
                null,
                new Object[]{device.toURI()}
        );

        konkerLoggerAppender.append(logEvent);
        Mockito.verify(konkerLoggerAppender, Mockito.never())
                .store(Mockito.any(),
                        Mockito.anyString(),
                        Mockito.anyString());
    }


    @Test
    public void shouldCaptureTenantAwareLog() {
        ILoggingEvent logEvent = new LoggingEvent(
                LOG.getName(),
                LOG,
                Level.INFO,
                "Test Message",
                null,
                new Object[]{device.toURI(), device.getLogLevel()}
                );

        konkerLoggerAppender.append(logEvent);
        Mockito.verify(konkerLoggerAppender, Mockito.atLeastOnce())
                .store(Mockito.any(),
                        Mockito.anyString(),
                        Mockito.anyString());


    }
}
