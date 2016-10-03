package com.konkerlabs.platform.registry.web.controllers;

import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.DeviceEventService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.EventSchemaService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.web.forms.DeviceVisualizationForm;

@Controller
@Scope("request")
@RequestMapping(value = "visualization")
public class DeviceVisualizationController implements ApplicationContextAware {

    public enum Messages {
        DEVICE_REGISTERED_SUCCESSFULLY("controller.device.registered.success");

        public String getCode() {
            return code;
        }

        private String code;

        Messages(String code) {
            this.code = code;
        }
    }
    
    private DeviceRegisterService deviceRegisterService;
    private DeviceEventService deviceEventService;
    private Tenant tenant;
    private ApplicationContext applicationContext;
    private EventSchemaService eventSchemaService;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Autowired
    public DeviceVisualizationController(DeviceRegisterService deviceRegisterService, DeviceEventService deviceEventService, Tenant tenant,
    		EventSchemaService eventSchemaService) {
        this.deviceRegisterService = deviceRegisterService;
        this.deviceEventService = deviceEventService;
        this.tenant = tenant;
        this.eventSchemaService = eventSchemaService;
    }

    @RequestMapping
    public ModelAndView index() {
        List<Device> all = deviceRegisterService.findAll(tenant).getResult();
        DeviceVisualizationForm deviceVisualizationForm = new DeviceVisualizationForm();
		return new ModelAndView("visualization/index", "devices", all).addObject("visualization", deviceVisualizationForm);
    }
    
    @RequestMapping(path = "/load/{dateStart}/{dateEnd}/{online}/{deviceGuid}/{channel}/{metric}")
    public ModelAndView load(@PathVariable String dateStart,
    		@PathVariable String dateEnd,
    		@PathVariable String online,
    		@PathVariable String deviceGuid,
    		@PathVariable String channel,
    		@PathVariable String metric,
    		@ModelAttribute("visualization") DeviceVisualizationForm deviceVisualizationForm) {
    	
		return new ModelAndView("visualization/chart-line", "chart-line", null).addObject("visualization", deviceVisualizationForm);
    	
    }
    
    @RequestMapping("/loading/channel/{deviceGuid}")
    public ModelAndView loadChannels(@PathVariable String deviceGuid, 
    					@ModelAttribute("visualization") DeviceVisualizationForm deviceVisualizationForm) {
    	ServiceResponse<List<String>> channels = eventSchemaService.findKnownIncomingChannelsBy(tenant, deviceGuid);
    	
    	return new ModelAndView("visualization/channels", "channels", channels.getResult()).addObject("visualization", deviceVisualizationForm);
    }
}
