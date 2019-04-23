package com.konkerlabs.platform.registry.test.integration.endpoints;

import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.services.api.DeviceConfigSetupService;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.data.config.WebMvcConfig;
import com.konkerlabs.platform.registry.data.upload.UploadRepository;
import com.konkerlabs.platform.registry.integration.endpoints.DeviceUploadRestEndpoint;
import com.konkerlabs.platform.registry.data.core.integration.processors.DeviceEventProcessor;
import com.konkerlabs.platform.registry.test.data.base.BusinessDataTestConfiguration;
import com.konkerlabs.platform.registry.test.data.base.SecurityTestConfiguration;
import com.konkerlabs.platform.registry.test.data.base.WebLayerTestContext;
import com.konkerlabs.platform.registry.test.data.base.WebTestConfiguration;
import com.konkerlabs.platform.utilities.parsers.json.JsonParsingService;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {
        BusinessDataTestConfiguration.class,
        WebMvcConfig.class,
        WebTestConfiguration.class,
        SecurityTestConfiguration.class,
        DeviceUploadRestEndpointTest.DeviceEventRestEndpointTestContextConfig.class
})
public class DeviceUploadRestEndpointTest extends WebLayerTestContext {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public DeviceUploadRestEndpoint deviceUploadRestEndpoint;

    @Autowired
    private DeviceEventProcessor deviceEventProcessor;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private UploadRepository uploadRepository;

    @Autowired
    private JsonParsingService jsonParsingService;

    @Before
    public void setUp() {
        deviceEventProcessor = mock(DeviceEventProcessor.class);
        deviceUploadRestEndpoint = new DeviceUploadRestEndpoint(
                applicationContext,
                deviceEventProcessor,
                uploadRepository,
                jsonParsingService);
    }

	@After
	public void tearDown() {
		Mockito.reset(uploadRepository);
	}

    @Test
    public void shouldUploadImage() throws Exception {
        Device device = Device.builder().deviceId("tug6g6essh4m")
                .active(true)
                .apiKey("e4399b2ed998")
                .guid("7d51c242-81db-11e6-a8c2-0746f010e945")
                .description("test")
                .deviceId("device_id")
                .guid("67014de6-81db-11e6-a5bc-3f99b38315c6")
                .tenant(Tenant.builder().domainName("konker").name("Konker").build())
                .application(Application.builder().name("SmartAC").build())
                .deviceModel(DeviceModel.builder().name("SensorTemp").build())
                .location(Location.builder().name("sp_br").build())
                .build();

        SecurityContext context = SecurityContextHolder.getContext();
        Authentication auth = new UsernamePasswordAuthenticationToken(device, null);
        context.setAuthentication(auth);

        getMockMvc().perform(MockMvcRequestBuilders
                .fileUpload("/upload/"+ device.getApiKey() + "/temp")
                .file(new MockMultipartFile("file", "photo.jpg", "image/jpeg", "00000".getBytes()))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is("200")))
        ;

    }

    @Test
    public void shouldUploadImageWithMetaData() throws Exception {
        Device device = Device.builder().deviceId("tug6g6essh4m")
                .active(true)
                .apiKey("e4399b2ed998")
                .guid("7d51c242-81db-11e6-a8c2-0746f010e945")
                .description("test")
                .deviceId("device_id")
                .guid("67014de6-81db-11e6-a5bc-3f99b38315c6")
                .tenant(Tenant.builder().domainName("konker").name("Konker").build())
                .application(Application.builder().name("SmartAC").build())
                .deviceModel(DeviceModel.builder().name("SensorTemp").build())
                .location(Location.builder().name("sp_br").build())
                .build();

        SecurityContext context = SecurityContextHolder.getContext();
        Authentication auth = new UsernamePasswordAuthenticationToken(device, null);
        context.setAuthentication(auth);

        getMockMvc().perform(MockMvcRequestBuilders
                .fileUpload("/upload/"+ device.getApiKey() + "/temp")
                .file(new MockMultipartFile("file", "photo.jpg", "image/jpeg", "00000".getBytes()))
                .param("meta-data", "{\"location\": \"SP\", \"temp\": 22.0}")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.code", is("200")))
                .andExpect(jsonPath("$.payload.metaData.location", is("SP")))
                .andExpect(jsonPath("$.payload.metaData.temp", is(22.0)))
        ;

    }

    @Configuration
    static class DeviceEventRestEndpointTestContextConfig {

        @Bean
        public DeviceRegisterService deviceRegisterService() {
            return Mockito.mock(DeviceRegisterService.class);
        }

        @Bean
        public DeviceEventService deviceEventService() {
            return Mockito.mock(DeviceEventService.class);
        }

        @Bean
        public DeviceEventProcessor deviceEventProcessor() {
            return Mockito.mock(DeviceEventProcessor.class);
        }

        @Bean
        public UploadRepository uploadRepository() {
            return Mockito.mock(UploadRepository.class);
        }

        @Bean
        public DeviceConfigSetupService deviceConfigSetupService() {
        	return Mockito.mock(DeviceConfigSetupService.class);
        }
    }
}
