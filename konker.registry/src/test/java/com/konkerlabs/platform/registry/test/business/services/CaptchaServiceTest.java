package com.konkerlabs.platform.registry.test.business.services;

import com.konkerlabs.platform.registry.integration.exceptions.IntegrationException;
import com.konkerlabs.platform.registry.integration.gateways.HttpGateway;
import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.web.services.api.*;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URISyntaxException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Created by Felipe on 04/01/17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        BusinessTestConfiguration.class,
        CaptchaServiceTest.CaptchaTestContextConfig.class
})
public class CaptchaServiceTest {
    @Autowired
    CaptchaService captchaService;

    @Autowired
    private HttpGateway httpGateway;

    private static final String JSON_SUCCESSFUL = "{\"success\" : \"True\"}";
    private static final String JSON_UNSUCCESSFUL = "{\"success\" : \"False\"}";

    @Test
    public void shouldBeSuccessfulCaptchaValidation(){
        try {
            when(httpGateway.request(any(), any(), any(), any(), any(),  any(), any()))
                    .thenReturn(JSON_SUCCESSFUL);
        } catch (IntegrationException e) {
            e.printStackTrace();
        }

        Assert.assertTrue(captchaService.validateCaptcha("", "", "").getResult());
    }

    @Test (expected = URISyntaxException.class)
    public void shouldBeUnsuccessfulCaptchaValidation(){
        try {
            Assert.assertFalse(captchaService.validateCaptcha(null, null, null).getResult());

            when(httpGateway.request(any(), any(), any(), any(), any(),  any(), any()))
                    .thenThrow(URISyntaxException.class);
            Assert.assertFalse(captchaService.validateCaptcha("", "", "").getResult());

            when(httpGateway.request(any(), any(), any(), any(), any(),  any(), any()))
                    .thenReturn(JSON_UNSUCCESSFUL);
            Assert.assertFalse(captchaService.validateCaptcha("", "", "").getResult());
        } catch (IntegrationException e) {
            e.printStackTrace();
        }
    }

    @Configuration
    static class CaptchaTestContextConfig {
        @Bean
        public HttpGateway httpGateway() {
            return Mockito.mock(HttpGateway.class);
        }
    }

}
