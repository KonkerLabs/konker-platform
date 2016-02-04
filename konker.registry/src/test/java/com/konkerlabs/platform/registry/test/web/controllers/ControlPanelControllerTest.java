package com.konkerlabs.platform.registry.test.web.controllers;

import com.konkerlabs.platform.registry.config.WebMvcConfig;
import com.konkerlabs.platform.registry.test.base.WebLayerTestContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {WebMvcConfig.class})
public class ControlPanelControllerTest extends WebLayerTestContext {

    @Test
    public void shouldShowControlPanelHome() throws Exception {
        getMockMvc().perform(get("/"))
                .andExpect(view().name("panel/index"));
    }

}
