package com.konkerlabs.platform.registry.data.core.integration.converters.helper;

import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.DeviceModel;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.data.core.integration.converters.JsonConverter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.stereotype.Component;

@Component
public class ConverterHelper {

    @Autowired
    private BeanFactory beans;

    public ServiceResponse<byte[]> getJsonPayload(Device device, String payloadJson) {

        DeviceModel.ContentType contentType = DeviceModel.ContentType.APPLICATION_JSON;
        if (device.getDeviceModel() != null &&
                device.getDeviceModel().getContentType() != null) {
            contentType = device.getDeviceModel().getContentType();
        }

        JsonConverter jsonConverter = BeanFactoryAnnotationUtils.qualifiedBeanOfType(beans, JsonConverter.class, contentType.getValue());
        ServiceResponse<byte[]> jsonConverterResponse = jsonConverter.fromJson(payloadJson);

        return jsonConverterResponse;

    }
}
