package com.konkerlabs.platform.registry.business.services.api;

import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.config.PubServerConfig;

import lombok.*;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;

import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.util.StringUtils;

public interface DeviceRegisterService {

	enum Validations {
		DEVICE_ID_NULL("service.device.id.not_null"),
		DEVICE_GUID_NULL("service.device.guid.not_null"),
		DEVICE_ID_ALREADY_REGISTERED("service.device.id.already_registered"),
		DEVICE_ID_DOES_NOT_EXIST("service.device.id.does_not_exist"),
		DEVICE_GUID_DOES_NOT_EXIST("service.device.guid.does_not_exist"),
		DEVICE_HAVE_EVENTROUTES("service.device.have_eventroutes"),
		DEVICE_TENANT_LIMIT("service.device.tenant.limit"),
		EQUALS_ORIGIN_DESTINATION_APPLICATIONS("service.device.equals_origin_destination_applications");

		public String getCode() {
			return code;
		}

		private String code;

		Validations(String code) {
			this.code = code;
		}
	}

    enum Messages {
        DEVICE_REGISTERED_SUCCESSFULLY("controller.device.registered.success"),
        DEVICE_REMOVED_SUCCESSFULLY("controller.device.removed.succesfully"),
        DEVICE_REMOVED_UNSUCCESSFULLY("controller.device.removed.unsuccesfully"),
        DEVICE_QRCODE_ERROR("service.device.qrcode.have_errors"),
        DEVICE_TITLE_MAP_DETAIL("devices.title.map.detail"),
        DEVICE_LAST_DATA_LABEL("devices.payload.label"),
        DEVICE_LAST_INGESTED_TIME_LABEL("devices.ingested.time.label");

        public String getCode() {
            return code;
        }

        private String code;

        Messages(String code) {
            this.code = code;
        }
    }

    @RequiredArgsConstructor
    @Getter
    class DeviceSecurityCredentials {
        @NonNull
        private Device device;
        @NonNull
        private String password;
    }

    @Data
    class DeviceDataURLs {
    	
    	private PubServerConfig pubServerConfig = new PubServerConfig();
    	private String username;
    	private String httpHostName;
    	private String mqttHostName;
    	private String context;
    	private String httpPort;
    	private String httpsPort;
    	private String mqttPort;
    	private String mqttTlsPort;
    	private String channel;
    	
    	public DeviceDataURLs(Device device, Locale locale) {
    		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
            messageSource.setBasename("classpath:/messages/devices");
            messageSource.setDefaultEncoding("UTF-8");

            httpHostName = pubServerConfig.getHttpHostname();
            httpPort = pubServerConfig.getHttpPort();
            httpsPort = pubServerConfig.getHttpsPort();
            mqttHostName = pubServerConfig.getMqttHostName();
            mqttPort = pubServerConfig.getMqttPort();
            mqttTlsPort = pubServerConfig.getMqttTlsPort();
            context = pubServerConfig.getHttpCtx();
            channel = messageSource.getMessage("model.device.channel", null, locale);
            username = device.getApiKey();

            if (httpHostName.equalsIgnoreCase("localhost")) {
                this.httpHostName = "<ip>";
            }
            if (mqttHostName.equalsIgnoreCase("localhost")) {
            	mqttHostName = "<ip>";
            }
            if (StringUtils.hasText(device.getApplication().getDataApiDomain())) {
            	httpHostName = device.getApplication().getDataApiDomain();
            }        
            if (StringUtils.hasText(device.getApplication().getDataMqttDomain())) {
            	mqttHostName = device.getApplication().getDataMqttDomain();
            }
    	}
        
        public String getHttpURLPub() {
        	return MessageFormat.format("http://{0}:{1}/pub/{2}/<{3}>", httpHostName, httpPort, username, channel);
        }
        
        public String getHttpURLSub() {
        	return MessageFormat.format("http://{0}:{1}/sub/{2}/<{3}>", httpHostName, httpPort, username, channel);
        }
        
        public String getHttpsURLPub() {
        	return MessageFormat.format("https://{0}:{1}/pub/{2}/<{3}>", httpHostName, httpsPort, username, channel);
        }
        
        public String getHttpsURLSub() {
        	return MessageFormat.format("https://{0}:{1}/sub/{2}/<{3}>", httpHostName, httpsPort, username, channel);
        }
        
        public String getMqttURL() {
        	return MessageFormat.format("mqtt://{0}:{1}", mqttHostName, mqttPort);
        }
        
        public String getMqttsURL() {
        	return MessageFormat.format("mqtts://{0}:{1}", mqttHostName, mqttTlsPort);
        }
        
        public String getMqttPubTopic() {
        	return MessageFormat.format("data/{0}/pub/<{1}>", username, channel);
        }
        
        public String getMqttSubTopic() {
        	return MessageFormat.format("data/{0}/sub/<{1}>", username, channel);
        }
    }

	/**
	 * Persists a new Device.
	 *
	 * If the deviceGuid already exists in this tenant, an error is created. The
	 * tenant must exist.
	 *
	 * @param tenant
	 * @param device
	 * @return
	 */
	ServiceResponse<Device> register(Tenant tenant, Application application, Device device);

	/**
	 * Updates an already existent Tenant.
	 *
	 * If the deviceGuid does not exist in this tenant, an error is created. The
	 * tenant must exist.
	 *
	 * @param tenant
	 * @param device
	 * @return
	 */
	ServiceResponse<Device> update(Tenant tenant, Application application, String guid, Device device);

	/**
	 * TODO @andre implement throwable flow Remove a device in logical way
	 *
	 * @param guid
	 * @return ServiceResponse<Device>
	 */
	ServiceResponse<Device> remove(Tenant tenant, Application application, String guid);

	/**
	 * Returns all devices (enabled or disabled) owned by the provided tenant.
	 *
	 * If no device is owned by this tenant, returns an empty List instead.
	 *
	 * @param tenant
	 * @return
	 */
	ServiceResponse<List<Device>> findAll(Tenant tenant, Application application);

	ServiceResponse<List<Device>> search(Tenant tenant, Application application, String tag);

	ServiceResponse<Long> countAll(Tenant tenant, Application application);

	/**
	 * Returns a device by its deviceGuid and tenant.
	 *
	 * If the device does not exist, returns an error
	 *
	 * @param tenant
	 * @param guid
	 * @return
	 */
	ServiceResponse<Device> getByDeviceGuid(Tenant tenant, Application application, String guid);

	/**
	 * Returns a device associated with the provided API Key.
	 *
	 * If the device does not exist, returns an error
	 *
	 * @param apiKey
	 * @return
	 */
	// TODO: should be moved to a KEYs service
	Device findByApiKey(String apiKey);


	/**
	 * Returns a device by its deviceGuid and tenant name.
	 *
	 * If the device does not exist, returns an error
	 *
	 * @param tenantDomainName
	 * @param deviceGuid
	 * @return
	 */
	Device findByTenantDomainNameAndDeviceGuid(String tenantDomainName, String deviceGuid);

	/**
	 * Enables or disables a device
	 *
	 * If the device does not exist, returns an error
	 *
	 * @param tenant
	 * @param guid
	 * @return
	 */
	ServiceResponse<Device> switchEnabledDisabled(Tenant tenant, Application application, String guid);

	/**
	 * Generates a security token for an existing device
	 *
	 * @param tenant
	 * @param guid
	 * @return A random password used to create the token
	 */
	ServiceResponse<DeviceSecurityCredentials> generateSecurityPassword(Tenant tenant, Application application, String guid);

	/**
	 * Return device URLs for publish and subscribe events
	 *
	 * @param tenant
	 * @param device
	 * @param locale
	 * @return
	 */
	ServiceResponse<DeviceDataURLs> getDeviceDataURLs(Tenant tenant, Application application, Device device, Locale locale);

	/**
	 * Generates a security token for an existing device
	 *
	 * @param credentials
	 * @param width
	 * @param height
	 * @return A random password used to create the token
	 */
	ServiceResponse<String> generateQrCodeAccess(DeviceSecurityCredentials credentials, int width, int height, Locale locale);

	/**
	 * Copies the device to another application from the same tenant and removes it from the previous application
	 *
	 * @param tenant
	 * @param originApplication
	 * @param guid
	 * @param destApplication
	 * @return The new device created
	 */
	ServiceResponse<Device> move(Tenant tenant, Application originApplication, String guid, Application destApplication);
	
	
	ServiceResponse<Device> findByDeviceId(Tenant tenant, Application application, String deviceId);

}