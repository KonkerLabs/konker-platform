package com.konkerlabs.platform.registry.test.web.services;

import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.konkerlabs.platform.registry.test.base.BusinessTestConfiguration;
import com.konkerlabs.platform.registry.web.services.CaptchaRestClient;
import com.konkerlabs.platform.registry.web.services.api.CaptchaService;

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
    private CaptchaRestClient captchaRestClient;

    private static final String JSON_SUCCESSFUL = "{\"success\" : \"True\"}";
    private static final String JSON_UNSUCCESSFUL = "{\"success\" : \"False\"}";

    @Test
    public void shouldBeSuccessfulCaptchaValidation() throws IOException{
        when(captchaRestClient.request(Mockito.any(URI.class)))
                .thenReturn(new String(JSON_SUCCESSFUL));

        Assert.assertTrue(captchaService.validateCaptcha("", "", "").getResult());
    }

    @Test (expected = URISyntaxException.class)
    @SuppressWarnings("unchecked")
    public void shouldBeUnsuccessfulCaptchaValidation() throws IOException{

        Assert.assertFalse(captchaService.validateCaptcha(null, null, null).getResult());

        when(captchaRestClient.request(Mockito.any(URI.class)))
                .thenThrow(IOException.class);
        Assert.assertFalse(captchaService.validateCaptcha("", "", "").getResult());

        when(captchaRestClient.request(Mockito.any(URI.class)))
                .thenReturn(new String(JSON_UNSUCCESSFUL));
        Assert.assertFalse(captchaService.validateCaptcha("", "", "").getResult());

    }

    @Configuration
    static class CaptchaTestContextConfig {
        @Bean
        public CaptchaRestClient captchaRestClient() {
            return Mockito.mock(CaptchaRestClient.class);
        }
    }

}
