package com.konkerlabs.platform.registry.test.base;

import org.junit.Before;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

public class WebLayerTestContext {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    public MockMvc getMockMvc() {
        return mockMvc;
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }
}
