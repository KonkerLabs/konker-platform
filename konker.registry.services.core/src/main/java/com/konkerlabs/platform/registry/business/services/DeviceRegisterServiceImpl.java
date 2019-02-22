package com.konkerlabs.platform.registry.business.services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.konkerlabs.platform.registry.business.exceptions.BusinessException;
import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.model.validation.CommonValidations;
import com.konkerlabs.platform.registry.business.repositories.*;
import com.konkerlabs.platform.registry.business.repositories.events.api.EventRepository;
import com.konkerlabs.platform.registry.business.services.api.*;
import com.konkerlabs.platform.registry.config.EventStorageConfig;
import com.konkerlabs.platform.registry.type.EventStorageConfigType;
import com.konkerlabs.platform.security.exceptions.SecurityException;
import com.konkerlabs.platform.security.managers.PasswordManager;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.time.Instant;
import java.util.*;

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
    private DeviceSearchRepository deviceSearchRepository;

    @Autowired
    private EventRouteRepository eventRouteRepository;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private EventStorageConfig eventStorageConfig;
    private EventRepository eventRepository;

    @Autowired
    private LocationSearchService locationSearchService;

    @Autowired
    private DeviceModelService deviceModelService;

    @Autowired
    private AlertTriggerService alertTriggerService;
    
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init() {
        try {
            eventRepository =
                    (EventRepository) applicationContext.getBean(
                            eventStorageConfig.getEventRepositoryBean()
                    );
        } catch (Exception e) {
            eventRepository =
                    (EventRepository) applicationContext.getBean(
                            EventStorageConfigType.MONGODB.bean()
                    );
        }
    }

    @Override
    public ServiceResponse<Device> register(Tenant tenant, Application application, Device device) {

        ServiceResponse<Device> validationResponse = validate(tenant, application);
        if (!validationResponse.isOk()) {
            return validationResponse;
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

        setDefaultModelAndLocation(tenant, application, device);

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

        if (deviceRepository.findByTenantIdAndApplicationAndDeviceId(tenant.getId(), application.getName(), device.getDeviceId()) != null) {
            LOGGER.debug("error saving device",
                    Device.builder().guid("NULL").tenant(tenant).build().toURI(),
                    device.getLogLevel());
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(Validations.DEVICE_ID_ALREADY_REGISTERED.getCode())
                    .build();
        }

        LOGGER.info("Device created. Id: {}", device.getDeviceId(), tenant.toURI(), tenant.getLogLevel());

        Device saved = deviceRepository.save(device);

        ServiceResponse<AlertTrigger> response = alertTriggerService.findByLocationDeviceModelAndType(
                device.getTenant(),
                device.getApplication(),
                device.getLocation(),
                device.getDeviceModel(),
                AlertTrigger.AlertTriggerType.SILENCE);

        if (response.isOk() &&
                !Optional.ofNullable(response.getResult()).isPresent()) {
            alertTriggerService.save(
                    device.getTenant(),
                    device.getApplication(),
                    AlertTrigger.builder()
                            .tenant(device.getTenant())
                            .application(device.getApplication())
                            .name("defaultTrigger")
                            .location(device.getLocation())
                            .deviceModel(device.getDeviceModel())
                            .type(AlertTrigger.AlertTriggerType.SILENCE)
                            .minutes(10)
                            .build());
        }

        return ServiceResponseBuilder.<Device>ok().withResult(saved).build();
    }

    private void setDefaultModelAndLocation(Tenant tenant, Application application, Device device) {

        if (device.getLocation() == null) {
            ServiceResponse<Location> locationResponse = locationSearchService.findDefault(tenant, application);
            if (locationResponse.isOk()) {
                device.setLocation(locationResponse.getResult());
            } else {
                LOGGER.error("error getting default application location",
                        Device.builder().guid("NULL").tenant(tenant).build().toURI(),
                        device.getLogLevel());
            }
        }

        if (device.getDeviceModel() == null) {
            ServiceResponse<DeviceModel> response = deviceModelService.findDefault(tenant, application);
            if (response.isOk()) {
                device.setDeviceModel(response.getResult());
            } else {
                LOGGER.error("error getting default application device model",
                        Device.builder().guid("NULL").tenant(tenant).build().toURI(),
                        device.getLogLevel());
            }
        }

    }

    @Override
    public ServiceResponse<List<Device>> findAll(Tenant tenant, Application application) {

        ServiceResponse<List<Device>> validationResponse = validate(tenant, application);
        if (!validationResponse.isOk()) {
            return validationResponse;
        }

        List<Device> all = deviceRepository.findAllByTenantIdAndApplicationName(tenant.getId(), application.getName());

        return ServiceResponseBuilder.<List<Device>>ok().withResult(all).build();
    }

    @Override
    public ServiceResponse<Page<Device>> search(Tenant tenant, Application application, User user, String tag, int page, int size) {

        ServiceResponse<Page<Device>> validationResponse = validate(tenant, application);
        if (!validationResponse.isOk()) {
            return validationResponse;
        }

        if ((Optional.ofNullable(user).isPresent()
                && Optional.ofNullable(user.getApplication()).isPresent()
                && !application.equals(user.getApplication()))
                || !Optional.ofNullable(user).isPresent()) {

            Device noDevice = Device.builder()
                    .guid("NULL")
                    .tenant(tenant)
                    .application(
                            (Optional.ofNullable(user)
                                    .orElse(User
                                            .builder()
                                            .application(Application.builder().name("default").build())
                                            .build()))
                                    .getApplication()
                    ).build();
            LOGGER.debug(ApplicationService.Validations.APPLICATION_HAS_NO_PERMISSION.getCode(),
                    noDevice.toURI(),
                    noDevice.getTenant().getLogLevel());
            return ServiceResponseBuilder.<Page<Device>>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_HAS_NO_PERMISSION.getCode())
                    .build();
        }

        if (size <= 0) {
            return ServiceResponseBuilder.<Page<Device>>error()
                    .withMessage(CommonValidations.SIZE_ELEMENT_PAGE_INVALID.getCode())
                    .build();
        }

        page = page > 0 ? page - 1 : 0;
        Page<Device> all = deviceSearchRepository.search(
                tenant.getId(),
                application.getName(),
                Optional.ofNullable(user.getLocation())
                        .orElse(Location.builder().build())
                        .getId(),
                tag,
                page,
                size);

        return ServiceResponseBuilder.<Page<Device>>ok().withResult(all).build();

    }

    @Override
    public ServiceResponse<Long> countAll(Tenant tenant, Application application) {

        ServiceResponse<Long> validationResponse = validate(tenant, application);
        if (!validationResponse.isOk()) {
            return validationResponse;
        }

        Long all = deviceRepository.countAllByTenantIdAndApplicationName(tenant.getId(), application.getName());
        return ServiceResponseBuilder.<Long>ok().withResult(all).build();

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
                        .withResult(new DeviceSecurityCredentials(saved, randomPassword)).build();
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

        ServiceResponse<Device> validationResponse = validate(tenant, application);
        if (!validationResponse.isOk()) {
            return validationResponse;
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

        setDefaultModelAndLocation(tenant, application, updatingDevice);

        // modify "modifiable" fields
        deviceFromDB.setDescription(updatingDevice.getDescription());
        deviceFromDB.setTags(updatingDevice.getTags());
        deviceFromDB.setName(updatingDevice.getName());
        deviceFromDB.setLocation(updatingDevice.getLocation());
        deviceFromDB.setDeviceModel(updatingDevice.getDeviceModel());
        deviceFromDB.setActive(updatingDevice.isActive());
        deviceFromDB.setDebug(updatingDevice.isDebug());
        deviceFromDB.setLastModificationDate(Instant.now());

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

        ServiceResponse<Device> validationResponse = validate(tenant, application);
        if (!validationResponse.isOk()) {
            return validationResponse;
        }

        if (!Optional.ofNullable(guid).isPresent())
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(Validations.DEVICE_GUID_NULL.getCode())
                    .build();

        //find device
        Device device = deviceRepository.findByTenantAndApplicationAndGuid(tenant.getId(), application.getName(), guid);

        if (!Optional.ofNullable(device).isPresent()) {
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(Validations.DEVICE_GUID_DOES_NOT_EXIST.getCode())
                    .build();
        }

        //find dependencies
        ServiceResponse<Device> dependenciesResponse = findDeviceDependencies(device);
        if (!dependenciesResponse.isOk()) {
            return dependenciesResponse;
        }

        Map<String, String> deviceRemovedMap = new HashMap<>();
        deviceRemovedMap.put("tenantDomain", device.getTenant().getDomainName());
        deviceRemovedMap.put("applicationName", device.getApplication().getName());
        deviceRemovedMap.put("deviceGuid", device.getGuid());
       	rabbitTemplate.convertAndSend("device.removed", deviceRemovedMap);
        deviceRepository.delete(device);

        LOGGER.info("Device removed. Id: {}", device.getDeviceId(), tenant.toURI(), tenant.getLogLevel());

        return ServiceResponseBuilder.<Device>ok()
                .withMessage(Messages.DEVICE_REMOVED_SUCCESSFULLY.getCode())
                .withResult(device)
                .build();
    }

    private ServiceResponse<Device> findDeviceDependencies(Device device) {

        List<EventRoute> incomingEventsRoutes =
                eventRouteRepository.findByIncomingUri(device.toURI());

        List<EventRoute> outgoingEventsRoutes =
                eventRouteRepository.findByOutgoingUri(device.toURI());

        if ((Optional.ofNullable(incomingEventsRoutes).isPresent() && incomingEventsRoutes.size() > 0) ||
                (Optional.ofNullable(outgoingEventsRoutes).isPresent() && outgoingEventsRoutes.size() > 0)) {
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(Validations.DEVICE_HAVE_EVENTROUTES.getCode())
                    .build();
        }

        return ServiceResponseBuilder.<Device>ok().build();

    }

    @Override
    public ServiceResponse<Device> getByDeviceGuid(Tenant tenant, Application application, String guid) {

        ServiceResponse<Device> validationResponse = validate(tenant, application);
        if (!validationResponse.isOk()) {
            return validationResponse;
        }

        if (!Optional.ofNullable(guid).isPresent())
            return ServiceResponseBuilder.<Device>error().withMessage(Validations.DEVICE_GUID_NULL.getCode())
                    .build();

        Tenant existingTenant = tenantRepository.findByDomainName(tenant.getDomainName());

        if (!Optional.ofNullable(existingTenant).isPresent())
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(CommonValidations.TENANT_DOES_NOT_EXIST.getCode()).build();

        if (!applicationRepository.exists(application.getName())) {
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode())
                    .build();
        }

        Device device = deviceRepository.findByTenantAndApplicationAndGuid(existingTenant.getId(), application.getName(), guid);
        if (!Optional.ofNullable(device).isPresent()) {
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(Validations.DEVICE_GUID_DOES_NOT_EXIST.getCode()).build();
        }

        return ServiceResponseBuilder.<Device>ok().withResult(device).build();
    }

    /**
     * Generate an encoded base64 String with qrcode image
     *
     * @param credentials
     * @param width
     * @param height
     * @return String
     * @throws Exception
     */
    @Override
    public ServiceResponse<String> generateQrCodeAccess(DeviceSecurityCredentials credentials, int width, int height, Locale locale) {
        try {

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Base64OutputStream encoded = new Base64OutputStream(baos);
            StringBuilder content = new StringBuilder();
            content.append("{\"user\":\"").append(credentials.getDevice().getUsername());
            content.append("\",\"pass\":\"").append(credentials.getPassword());
            
            DeviceDataURLs deviceDataURLs = new DeviceDataURLs(credentials.getDevice(), locale);
           	content.append("\",\"host\":\"").append(deviceDataURLs.getHttpHostName());
            content.append("\",\"ctx\":\"").append(deviceDataURLs.getContext());
            content.append("\",\"host-mqtt\":\"").append(deviceDataURLs.getMqttHostName());
            
            content.append("\",\"http\":\"").append(deviceDataURLs.getHttpPort());
            content.append("\",\"https\":\"").append(deviceDataURLs.getHttpsPort());
            content.append("\",\"mqtt\":\"").append(deviceDataURLs.getMqttPort());
            content.append("\",\"mqtt-tls\":\"").append(deviceDataURLs.getMqttTlsPort());
            content.append("\",\"pub\":\"pub/").append(credentials.getDevice().getUsername());
            content.append("\",\"sub\":\"sub/").append(credentials.getDevice().getUsername()).append("\"}");

            Map<EncodeHintType, Serializable> map = new HashMap<>();
            for (AbstractMap.SimpleEntry<EncodeHintType, ? extends Serializable> item : Arrays.<AbstractMap.SimpleEntry<EncodeHintType, ? extends Serializable>>asList(
                    new AbstractMap.SimpleEntry<>(EncodeHintType.MARGIN, 0),
                    new AbstractMap.SimpleEntry<>(EncodeHintType.CHARACTER_SET, "UTF-8")
            )) {
                if (map.put(item.getKey(), item.getValue()) != null) {
                    throw new IllegalStateException("Duplicate key");
                }
            }
            BitMatrix bitMatrix = new QRCodeWriter().encode(
                    content.toString(),
                    BarcodeFormat.QR_CODE, width, height,
                    Collections.unmodifiableMap(map));
            MatrixToImageWriter.writeToStream(bitMatrix, "png", encoded);
            String result = "data:image/png;base64," + new String(baos.toByteArray(), 0, baos.size(), "UTF-8");
            return ServiceResponseBuilder.<String>ok().withResult(result).build();
        } catch (Exception e) {
            return ServiceResponseBuilder.<String>error()
                    .withMessage(Messages.DEVICE_QRCODE_ERROR.getCode())
                    .build();
        }
    }

    @Override
    public ServiceResponse<Device> move(Tenant tenant, Application originApplication, String guid, Application destApplication) {

        ServiceResponse<Device> validationResponse = validate(tenant, originApplication);
        if (!validationResponse.isOk()) {
            return validationResponse;
        }

        if (!Optional.ofNullable(guid).isPresent()) {
            return ServiceResponseBuilder.<Device>error().withMessage(Validations.DEVICE_GUID_NULL.getCode())
                    .build();
        }

        if (!Optional.ofNullable(destApplication).isPresent()) {
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_NULL.getCode())
                    .build();
        }

        if (originApplication.getName().equals(destApplication.getName())) {
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(Validations.EQUALS_ORIGIN_DESTINATION_APPLICATIONS.getCode())
                    .build();
        }

        if (applicationRepository.findByTenantAndName(tenant.getId(), destApplication.getName()) == null) {
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_DOES_NOT_EXIST.getCode())
                    .build();
        }

        Device originDevice = deviceRepository.findByTenantAndApplicationAndGuid(tenant.getId(), originApplication.getName(), guid);

        if (!Optional.ofNullable(originDevice).isPresent()) {
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(Validations.DEVICE_GUID_DOES_NOT_EXIST.getCode())
                    .build();
        }

        if (deviceRepository.findByTenantIdAndApplicationAndDeviceId(tenant.getId(), destApplication.getName(), originDevice.getDeviceId()) != null) {
            LOGGER.debug("error saving device",
                    Device.builder().guid("NULL").tenant(tenant).build().toURI(),
                    originDevice.getLogLevel());
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(Validations.DEVICE_ID_ALREADY_REGISTERED.getCode())
                    .build();
        }

        setDefaultModelAndLocation(tenant, destApplication, originDevice);

        ServiceResponse<Device> dependenciesResponse = findDeviceDependencies(originDevice);
        if (!dependenciesResponse.isOk()) {
            return dependenciesResponse;
        }

        // create the new devices
        ServiceResponse<Device> cloneResponse = clone(tenant, destApplication, originDevice);
        if (!cloneResponse.isOk()) {
            return cloneResponse;
        }

        // changes the origin key to stop receiving messages from the old device
        originDevice = deviceRepository.findByTenantAndApplicationAndGuid(tenant.getId(), originApplication.getName(), guid);
        originDevice.setApiKey(String.format("%s-TOBEDELETED", originDevice.getApiKey()));
        deviceRepository.save(originDevice);

        // copy the events to the new device
        Device newDevice = cloneResponse.getResult();
        try {
            eventRepository.copy(tenant, originDevice, newDevice);
        } catch (BusinessException e) {
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(Messages.DEVICE_REMOVED_UNSUCCESSFULLY.getCode())
                    .withResult(originDevice)
                    .build();
        }

        // finally remove the old device
        ServiceResponse<Device> removeResponse = remove(tenant, originApplication, guid);
        if (!removeResponse.isOk()) {
            return ServiceResponseBuilder.<Device>error()
                    .withMessages(removeResponse.getResponseMessages())
                    .build();
        }

        return ServiceResponseBuilder.<Device>ok().withResult(cloneResponse.getResult()).build();

    }

    private ServiceResponse<Device> clone(Tenant tenant, Application destApplication, Device device) {

        // search for a location of same name at the new application
        ServiceResponse<Location> locationServiceResponse = locationSearchService.findByName(tenant, destApplication, device.getLocation().getName(), false);
        if (locationServiceResponse.isOk()) {
            device.setLocation(locationServiceResponse.getResult());
        } else {
            locationServiceResponse = locationSearchService.findDefault(tenant, destApplication);
            if (locationServiceResponse.isOk()) {
                device.setLocation(locationServiceResponse.getResult());
            } else {
                return ServiceResponseBuilder.<Device>error()
                        .withMessages(locationServiceResponse.getResponseMessages())
                        .build();
            }
        }

        // search for a device model of same name at the new application
        ServiceResponse<DeviceModel> deviceModelServiceResponse = deviceModelService.getByTenantApplicationAndName(tenant, destApplication, device.getDeviceModel().getName());
        if (deviceModelServiceResponse.isOk()) {
            device.setDeviceModel(deviceModelServiceResponse.getResult());
        } else {
            deviceModelServiceResponse = deviceModelService.findDefault(tenant, destApplication);
            if (deviceModelServiceResponse.isOk()) {
                device.setDeviceModel(deviceModelServiceResponse.getResult());
            } else {
                return ServiceResponseBuilder.<Device>error()
                        .withMessages(deviceModelServiceResponse.getResponseMessages())
                        .build();
            }
        }

        device.setId(null); // force to save new device instead of updating
        device.setGuid(UUID.randomUUID().toString());
        device.setApplication(destApplication);

        LOGGER.info("Device created. Id: {}", device.getDeviceId(), tenant.toURI(), tenant.getLogLevel());

        Device saved = deviceRepository.save(device);

        return ServiceResponseBuilder.<Device>ok().withResult(saved).build();

    }

    @Override
    public ServiceResponse<DeviceDataURLs> getDeviceDataURLs(Tenant tenant, Application application, Device device, Locale locale) {

        ServiceResponse<DeviceDataURLs> validationResponse = validate(tenant, application);
        if (!validationResponse.isOk()) {
            return validationResponse;
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

        DeviceDataURLs deviceDataURLs = new DeviceDataURLs(device, locale);
        return ServiceResponseBuilder.<DeviceDataURLs>ok().withResult(deviceDataURLs).build();

    }

    private <T> ServiceResponse<T> validate(Tenant tenant, Application application) {

        if (!Optional.ofNullable(tenant).isPresent()) {
            Device noDevice = Device.builder().guid("NULL").tenant(
                    Tenant.builder().domainName("unknown_domain").build()).build();
            LOGGER.debug(CommonValidations.TENANT_NULL.getCode(),
                    noDevice.toURI(),
                    noDevice.getTenant().getLogLevel());
            return ServiceResponseBuilder.<T>error()
                    .withMessage(CommonValidations.TENANT_NULL.getCode())
                    .build();
        }

        if (!Optional.ofNullable(application).isPresent()) {
            Device noDevice = Device.builder()
                    .guid("NULL")
                    .tenant(tenant)
                    .application(Application.builder().name("unknown_app").tenant(tenant).build())
                    .build();
            LOGGER.debug(ApplicationService.Validations.APPLICATION_NULL.getCode(),
                    noDevice.toURI(),
                    noDevice.getTenant().getLogLevel());
            return ServiceResponseBuilder.<T>error()
                    .withMessage(ApplicationService.Validations.APPLICATION_NULL.getCode())
                    .build();
        }

        return ServiceResponseBuilder.<T>ok().build();

    }

	@Override
	public ServiceResponse<Device> findByDeviceId(Tenant tenant, Application application, String deviceId) {
		ServiceResponse<Device> validationResponse = validate(tenant, application);
        if (!validationResponse.isOk()) {
            return validationResponse;
        }

        Device device = deviceRepository.findAllByTenantIdApplicationNameDeviceId(tenant.getId(), application.getName(), deviceId);
        if (device == null) {
            return ServiceResponseBuilder.<Device>error()
                    .withMessage(Validations.DEVICE_ID_DOES_NOT_EXIST.getCode()).build();
        } else {
            return ServiceResponseBuilder.<Device>ok().withResult(device).build();
        }
	}

}