package com.konkerlabs.platform.registry.test.web.controllers;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.konkerlabs.platform.registry.business.services.api.SmsDestinationService;
import com.konkerlabs.platform.registry.config.WebMvcConfig;
import com.konkerlabs.platform.registry.test.base.SecurityTestConfiguration;
import com.konkerlabs.platform.registry.test.base.WebLayerTestContext;
import com.konkerlabs.platform.registry.test.base.WebTestConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = { WebMvcConfig.class, WebTestConfiguration.class, SecurityTestConfiguration.class,
        SmsDestinationControllerDisabledTest.SmsDestinationTestContextConfig.class })
public class SmsDestinationControllerDisabledTest extends WebLayerTestContext {

    @Autowired
    private SmsDestinationService smsDestinationService;


    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {
        Mockito.reset(smsDestinationService);
    }

    @Test
    public void shouldCheckSmsIsDisabled() throws Exception {
        getMockMvc().perform(get("/destinations/sms"))
                .andExpect(status().is4xxClientError());
    }

    @Configuration
    static class SmsDestinationTestContextConfig {
        @Bean
        public SmsDestinationService restDestinationService() {
            return mock(SmsDestinationService.class);
        }
    }
}