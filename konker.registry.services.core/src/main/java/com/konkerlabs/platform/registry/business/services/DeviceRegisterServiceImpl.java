package com.konkerlabs.platform.registry.business.services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.EventRoute;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.ApplicationRepository;
import com.konkerlabs.platform.registry.business.repositories.DeviceRepository;
import com.konkerlabs.platform.registry.business.repositories.EventRouteRepository;
import com.konkerlabs.platform.registry.business.repositories.TenantRepository;
import com.konkerlabs.platform.registry.business.repositories.events.api.EventRepository;
import com.konkerlabs.platform.registry.business.services.api.ApplicationService;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponseBuilder;
import com.konkerlabs.platform.registry.config.PubServerConfig;
import com.konkerlabs.platform.security.exceptions.SecurityException;
import com.konkerlabs.platform.security.managers.PasswordManager;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DeviceRegisterServiceImpl implements DeviceRegisterService {

    private Logger LOGGER = LoggerFactory.getLogger(DeviceRegisterServiceImpl.class);

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private EventRouteRepository eventRouteRepository;

    @Autowired @Qualifier("mongoEvents")
    private EventRepository eventRepository;

    private PubServerConfig pubServerConfig = new PubServerConfig();

    @Override
    public ServiceResponse<Device> register(Tenant tenant, Application application, Device device) {

        if (!Optional.ofNullable(tenant).isPresent()) {
            Device noDevice = Device.builder().guid("NULL").tenant(
			        Tenant.builder().domainName("unknow_domain").build()).build();
			LOGGER.debug(CommonValidations.TENANT_NULL.getCode(),
                    noDevice.toURI(),
                    noDevice.getTenant().getLogLevel());
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode())
                    .build();
        }

        if (!Optional.ofNullable(application).isPresent()) {
        	Device noDevice = Device.builder()
        			.guid("NULL")
        			.tenant(tenant)
        			.application(Application.builder().name("unknowapp").tenant(tenant).build())
        			.build();
        	LOGGER.debug(ApplicationService.Validations.APPLICATION_NULL.getCode(),
        			noDevice.toURI(),
        			noDevice.getTenant().getLogLevel());
        	return ServiceResponseBuilder.<Device>error()
        			.withMessage(ApplicationService.Validations.APPLICATION_NULL.getCode())
        			.build();
        }

        if (!Optional.ofNullable(device).isPresent()) {
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(CommonValidations.RECORD_NULL.getCode())
                    .build();
        }

        if (!tenantRepository.exists(tenant.getId())) {
            LOGGER.debug("device cannot exists",
                    Device.builder().guid("NULL").tenant(tenant).build().toURI(),
                    device.getLogLevel());
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode())
                    .build();
        }
        
        Long count = (long) deviceRepository.findAllByTenant(tenant.getId()).size();
        if (Optional.ofNullable(tenant.getDevicesLimit()).isPresent() && count.compareTo(tenant.getDevicesLimit()) >= 0) {
        	LOGGER.debug("Devices limit is exceeded.",
                    Device.builder().guid("NULL").tenant(tenant).build().toURI(),
                    device.getLogLevel());
        	return ServiceResponseBuilder.<Device>error()
                    .withMessage(Validations.DEVICE_TENANT_LIMIT.getCode())
                    .build();
        }

        if (!applicationRepository.exists(application.getName())) {
        	LOGGER.debug("device cannot exists",
                    Device.builder().guid("NULL").tenant(tenant).build().toURI(),
                    device.getLogLevel());
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode())
                    .build();
        }

        device.onRegistration();
        device.setGuid(UUID.randomUUID().toString());

        if (Optional.ofNullable(deviceRepository.findByApiKey(device.getApiKey())).isPresent()) {
            LOGGER.debug("device api key cannot exists",
                    Device.builder().guid("NULL").tenant(tenant).build().toURI(),
                    device.getLogLevel());
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(CommonValidations.GENERIC_ERROR.getCode())
                    .build();
        }

        device.setTenant(tenant);
        device.setApplication(application);
		device.setLogLevel(tenant.getLogLevel());

        Optional<Map<String, Object[]>> validations = device.applyValidations();

        if (validations.isPresent()) {
            LOGGER.debug("error saving device",
                    Device.builder().guid("NULL").tenant(tenant).build().toURI(),
                    device.getLogLevel());
            return ServiceResponseBuilder.<Device>error()
                    .withMessages(validations.get())
                    .build();
        }

        if (deviceRepository.findByTenantIdAndDeviceId(tenant.getId(), device.getDeviceId()) != null) {
            LOGGER.debug("error saving device",
                    Device.builder().guid("NULL").tenant(tenant).build().toURI(),
                    device.getLogLevel());
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(Validations.DEVICE_ID_ALREADY_REGISTERED.getCode())
                    .build();
        }

        LOGGER.info("Device created. Id: {}", device.getDeviceId(), tenant.toURI(), tenant.getLogLevel());

        Device saved = deviceRepository.save(device);

        return ServiceResponseBuilder.<Device>ok().withResult(saved).build();
    }

    @Override
    public ServiceResponse<List<Device>> findAll(Tenant tenant, Application application) {

        if (!Optional.ofNullable(tenant).isPresent()) {
            Device noDevice = Device.builder().guid("NULL").tenant(
                    Tenant.builder().domainName("unknow_domain").build()).build();
            LOGGER.debug(CommonValidations.TENANT_NULL.getCode(),
                    noDevice.toURI(),
                    noDevice.getTenant().getLogLevel());
            return ServiceResponseBuilder.<List<Device>>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode())
                    .build();
        }

        if (!Optional.ofNullable(application).isPresent()) {
            Device noDevice = Device.builder()
                    .guid("NULL")
                    .tenant(tenant)
                    .application(Application.builder().name("unknowapp").tenant(tenant).build())
                    .build();
            LOGGER.debug(ApplicationService.Validations.APPLICATION_NULL.getCode(),
                    noDevice.toURI(),
                    noDevice.getTenant().getLogLevel());
            return ServiceResponseBuilder.<List<Device>>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_NULL.getCode())
                    .build();
        }

        List<Device> all = deviceRepository.findAllByTenantIdAndApplicationName(tenant.getId(), application.getName());
        return ServiceResponseBuilder.<List<Device>>ok().withResult(all).build();
    }


    @Override
    public Device findByApiKey(String apiKey) {
        return deviceRepository.findByApiKey(apiKey);
    }

    @Override
    public Device findByTenantDomainNameAndDeviceGuid(String tenantDomainName, String deviceGuid) {
        return deviceRepository.findByTenantAndGuid(
                tenantRepository.findByDomainName(tenantDomainName).getId(),
                deviceGuid
        );
    }

    @Override
    public ServiceResponse<Device> switchEnabledDisabled(Tenant tenant, Application application, String guid) {
        if (!Optional.ofNullable(guid).isPresent())
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(Validations.DEVICE_GUID_NULL.getCode())
                    .build();

        Device found = getByDeviceGuid(tenant, application, guid).getResult();

        if (!Optional.ofNullable(found).isPresent())
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(Validations.DEVICE_GUID_DOES_NOT_EXIST.getCode())
                    .build();

        found.setActive(!found.isActive());

        Device updated = deviceRepository.save(found);

        return ServiceResponseBuilder.<Device>ok()
                .withResult(updated)
                .build();
    }

    @Override
    public ServiceResponse<DeviceSecurityCredentials> generateSecurityPassword(Tenant tenant, Application application, String guid) {
        ServiceResponse<Device> serviceResponse = getByDeviceGuid(tenant, application, guid);

        if (serviceResponse.isOk()) {
            try {
                Device existingDevice = serviceResponse.getResult();
                PasswordManager passwordManager = new PasswordManager();
                String randomPassword = passwordManager.generateRandomPassword(12);
                existingDevice.setSecurityHash(passwordManager.createHash(randomPassword));
                existingDevice.regenerateApiKey();
                Device saved = deviceRepository.save(existingDevice);

                LOGGER.info("Password generated for device id: {}", existingDevice.getDeviceId(), tenant.toURI(), tenant.getLogLevel());

                return ServiceResponseBuilder.<DeviceSecurityCredentials>ok()
                        .withResult(new DeviceSecurityCredentials(saved,randomPassword)).build();
            } catch (SecurityException e) {
                return ServiceResponseBuilder.<DeviceSecurityCredentials>error()
                        .withMessage(CommonValidations.GENERIC_ERROR.getCode()).build();
            }

        } else
            return ServiceResponseBuilder.<DeviceSecurityCredentials>error()
                    .withMessages(serviceResponse.getResponseMessages()).build();
    }


    @Override
    public ServiceResponse<Device> update(Tenant tenant, Application application, String guid, Device updatingDevice) {
        if (!Optional.ofNullable(tenant).isPresent())
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode())
                    .build();

        if (!Optional.ofNullable(application).isPresent()) {
        	return ServiceResponseBuilder.<Device>error()
        			.withMessage(ApplicationService.Validations.APPLICATION_NULL.getCode())
        			.build();
        }

        if (!Optional.ofNullable(guid).isPresent())
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(Validations.DEVICE_GUID_NULL.getCode())
                    .build();

        if (!Optional.ofNullable(updatingDevice).isPresent())
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(CommonValidations.RECORD_NULL.getCode())
                    .build();

        Device deviceFromDB = getByDeviceGuid(tenant, application, guid).getResult();
        if (deviceFromDB == null) {
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(Validations.DEVICE_GUID_DOES_NOT_EXIST.getCode())
                    .build();
        }

        // modify "modifiable" fields
        deviceFromDB.setDescription(updatingDevice.getDescription());
        deviceFromDB.setName(updatingDevice.getName());
        deviceFromDB.setActive(updatingDevice.isActive());

        Optional<Map<String, Object[]>> validations = deviceFromDB.applyValidations();

        if (validations.isPresent()) {
            return ServiceResponseBuilder.<Device>error()
                    .withMessages(validations.get())
                    .build();
        }

        Device saved = deviceRepository.save(deviceFromDB);

        LOGGER.info("Device updated. Id: {}", deviceFromDB.getDeviceId(), tenant.toURI(), tenant.getLogLevel());

        return ServiceResponseBuilder.<Device>ok()
                .withResult(saved)
                .build();
    }

    @Override
    public ServiceResponse<Device> remove(Tenant tenant, Application application, String guid) {

        if(!Optional.ofNullable(guid).isPresent())
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(Validations.DEVICE_GUID_NULL.getCode())
                    .build();

        if (!Optional.ofNullable(tenant).isPresent())
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode())
                    .build();

        if (!Optional.ofNullable(application).isPresent())
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_NULL.getCode())
                    .build();

        //find device
        Device device = deviceRepository.findByTenantAndApplicationAndGuid(tenant.getId(), application.getName(), guid);

        if(!Optional.ofNullable(device).isPresent()){
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(Validations.DEVICE_GUID_DOES_NOT_EXIST.getCode())
                    .build();
        }
        //find dependencies
        List<EventRoute> incomingEvents =
                eventRouteRepository.findByIncomingUri(device.toURI());

        List<EventRoute> outgoingEvents =
                eventRouteRepository.findByOutgoingUri(device.toURI());

        ServiceResponse<Device> response = null;

        if(Optional.ofNullable(incomingEvents).isPresent() && incomingEvents.size() > 0 ||
                Optional.ofNullable(outgoingEvents).isPresent() && outgoingEvents.size() > 0) {
            if(response == null){
                response = ServiceResponseBuilder.<Device>error()
                        .withMessage(Validations.DEVICE_HAVE_EVENTROUTES.getCode())
                        .build();
            } else {
                response.setStatus(ServiceResponse.Status.ERROR);
                response.getResponseMessages().put(Validations.DEVICE_HAVE_EVENTROUTES.getCode(), null);
            }

        }

        if(Optional.ofNullable(response).isPresent()) return response;

        try {
            eventRepository.removeBy(tenant, application, device.getGuid());
            deviceRepository.delete(device);
        } catch (BusinessException e){
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(Messages.DEVICE_REMOVED_UNSUCCESSFULLY.getCode())
                    .withResult(device)
                    .build();
        }

        LOGGER.info("Device removed. Id: {}", device.getDeviceId(), tenant.toURI(), tenant.getLogLevel());

        return ServiceResponseBuilder.<Device>ok()
                .withMessage(Messages.DEVICE_REMOVED_SUCCESSFULLY.getCode())
                .withResult(device)
                .build();
    }


	@Override
	public ServiceResponse<Device> getByDeviceGuid(Tenant tenant, Application application, String guid) {
		if (!Optional.ofNullable(tenant).isPresent())
			return ServiceResponseBuilder.<Device> error().withMessage(CommonValidations.TENANT_NULL.getCode())
					.build();

		if (!Optional.ofNullable(application).isPresent())
			return ServiceResponseBuilder.<Device> error()
					.withMessage(ApplicationService.Validations.APPLICATION_NULL.getCode())
					.build();

        if (!Optional.ofNullable(guid).isPresent())
			return ServiceResponseBuilder.<Device> error().withMessage(Validations.DEVICE_GUID_NULL.getCode())
					.build();

		Tenant existingTenant = tenantRepository.findByName(tenant.getName());

		if (!Optional.ofNullable(tenant).isPresent())
			return ServiceResponseBuilder.<Device> error()
					.withMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()).build();

		if (!applicationRepository.exists(application.getName())) {
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode())
                    .build();
        }

		Device device = deviceRepository.findByTenantAndApplicationAndGuid(existingTenant.getId(), application.getName(), guid);
		if (!Optional.ofNullable(device).isPresent()) {
			return ServiceResponseBuilder.<Device> error()
					.withMessage(Validations.DEVICE_GUID_DOES_NOT_EXIST.getCode()).build();
		}

		return ServiceResponseBuilder.<Device> ok().withResult(device).build();
	}

    /**
     * Generate an encoded base64 String with qrcode image
     * @param credentials
     * @param width
     * @param height
     * @return String
     * @throws Exception
     */
    @Override
    public ServiceResponse<String> generateQrCodeAccess(DeviceSecurityCredentials credentials, int width, int height) {
        try {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Base64OutputStream encoded = new Base64OutputStream(baos);
            StringBuilder content = new StringBuilder();
            content.append("{\"user\":\"" + credentials.getDevice().getUsername());
            content.append("\",\"pass\":\"" + credentials.getPassword());
            if (!StringUtils.isEmpty(pubServerConfig.getHttpHostname())
                    && !pubServerConfig.getHttpHostname().equals("localhost")) {
                content.append("\",\"host\":\"" + pubServerConfig.getHttpHostname());
            } else {
                content.append("\",\"host\":\"" + "<IP>");
            }
            content.append("\",\"ctx\":\"" + pubServerConfig.getHttpCtx());
            if (!StringUtils.isEmpty(pubServerConfig.getMqttHostName())
                    && !pubServerConfig.getMqttHostName().equals("localhost")) {
                content.append("\",\"host-mqtt\":\"" + pubServerConfig.getMqttHostName());
            } else {
                content.append("\",\"host-mqtt\":\"" + "<IP>");
            }
            content.append("\",\"http\":\"" + pubServerConfig.getHttpPort());
            content.append("\",\"https\":\"" + pubServerConfig.getHttpsPort());
            content.append("\",\"mqtt\":\"" + pubServerConfig.getMqttPort());
            content.append("\",\"mqtt-tls\":\"" + pubServerConfig.getMqttTlsPort());
            content.append("\",\"pub\":\"pub/"+ credentials.getDevice().getUsername());
            content.append("\",\"sub\":\"sub/"+ credentials.getDevice().getUsername() +"\"}");

            BitMatrix bitMatrix = new QRCodeWriter().encode(
                    content.toString(),
                    BarcodeFormat.QR_CODE, width, height,
                    Collections.unmodifiableMap(
                            Stream.of(
                                    new AbstractMap.SimpleEntry<>(EncodeHintType.MARGIN, 0),
                                    new AbstractMap.SimpleEntry<>(EncodeHintType.CHARACTER_SET, "UTF-8")
                            ).collect(Collectors.toMap((item) ->item.getKey(), (item) -> item.getValue()))));

            MatrixToImageWriter.writeToStream(bitMatrix, "png", encoded);
            String result = "data:image/png;base64," + new String(baos.toByteArray(), 0, baos.size(), "UTF-8");
            return ServiceResponseBuilder.<String> ok().withResult(result).build();
        } catch (Exception e){
            return ServiceResponseBuilder.<String> error()
                    .withMessage(Messages.DEVICE_QRCODE_ERROR.getCode())
                    .build();
        }
    }

    @Override
    public ServiceResponse<DeviceDataURLs> getDeviceDataURLs(Tenant tenant, Application application, Device device, Locale locale) {

        if (!Optional.ofNullable(tenant).isPresent()) {
            return ServiceResponseBuilder.<DeviceDataURLs>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode())
                    .build();
        }

        if (!Optional.ofNullable(application).isPresent()) {
            return ServiceResponseBuilder.<DeviceDataURLs>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_NULL.getCode())
                    .build();
        }

        if (!Optional.ofNullable(device).isPresent()) {
            return ServiceResponseBuilder.<DeviceDataURLs>error()
                    .withMessage(CommonValidations.RECORD_NULL.getCode())
                    .build();
        }

        if (!device.getTenant().getGuid().equals(tenant.getGuid())) {
            return ServiceResponseBuilder.<DeviceDataURLs>error()
                    .withMessage(Validations.DEVICE_GUID_DOES_NOT_EXIST.getCode())
                    .build();
        }

        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:/messages/devices");
        messageSource.setDefaultEncoding("UTF-8");

        String httpHostname = pubServerConfig.getHttpHostname();
        String mqttHostName = pubServerConfig.getMqttHostName();
        String channelMsg = messageSource.getMessage("model.device.channel", null, locale);
        String username = device.getApiKey();

        if (httpHostname.equalsIgnoreCase("localhost")) {
            httpHostname = "<ip>";
        }
        if (mqttHostName.equalsIgnoreCase("localhost")) {
            mqttHostName = "<ip>";
        }
        if (StringUtils.hasText(tenant.getDataApiDomain())) {
            httpHostname = tenant.getDataApiDomain();
            mqttHostName = tenant.getDataApiDomain();
        }

        DeviceDataURLs deviceDataURLs = DeviceRegisterService.DeviceDataURLs.builder()
            .httpURLPub(MessageFormat.format("http://{0}:{1}/pub/{2}/<{3}>", httpHostname, pubServerConfig.getHttpPort(), username, channelMsg))
            .httpURLSub(MessageFormat.format("http://{0}:{1}/sub/{2}/<{3}>", httpHostname, pubServerConfig.getHttpPort(), username, channelMsg))
            .httpsURLPub(MessageFormat.format("https://{0}:{1}/pub/{2}/<{3}>", httpHostname, pubServerConfig.getHttpsPort(), username, channelMsg))
            .httpsURLSub(MessageFormat.format("https://{0}:{1}/sub/{2}/<{3}>", httpHostname, pubServerConfig.getHttpsPort(), username, channelMsg))
            .mqttURL(MessageFormat.format("mqtt://{0}:{1}", mqttHostName, pubServerConfig.getMqttPort()))
            .mqttsURL(MessageFormat.format("mqtts://{0}:{1}", mqttHostName, pubServerConfig.getMqttTlsPort()))
            .mqttPubTopic(MessageFormat.format("pub/{0}/<{1}>", username, channelMsg))
            .mqttSubTopic(MessageFormat.format("sub/{0}/<{1}>", username, channelMsg))
            .build();

        return ServiceResponseBuilder.<DeviceDataURLs>ok().withResult(deviceDataURLs).build();

    }

}
