package com.konkerlabs.platform.registry.api.web.controller;

import com.konkerlabs.platform.registry.api.exceptions.BadRequestResponseException;
import com.konkerlabs.platform.registry.api.exceptions.BadServiceResponseException;
import com.konkerlabs.platform.registry.api.exceptions.NotFoundResponseException;
import com.konkerlabs.platform.registry.api.model.*;
import com.konkerlabs.platform.registry.business.model.*;
import com.konkerlabs.platform.registry.business.services.api.*;
import com.konkerlabs.platform.registry.business.services.api.GatewayService.Validations;
import com.konkerlabs.platform.registry.idm.services.OAuth2AccessTokenService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@Scope("request")
@RequestMapping(value = "/{application}/gateways")
@Api(tags = "gateways")
public class GatewayRestController extends AbstractRestController implements InitializingBean {

    @Autowired
    private GatewayService gatewayService;

    @Autowired
    private DeviceRegisterService deviceRegisterService;

    @Autowired
    private OAuth2AccessTokenService oAuth2AccessTokenService;

    private Set<String> validationsCode = new HashSet<>();

    @GetMapping(path = "/")
    @PreAuthorize("hasAuthority('LIST_GATEWAYS')")
    @ApiOperation(
            value = "List all gateways by application",
            response = GatewayVO.class)
    public List<GatewayVO> list(@PathVariable("application") String applicationId) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        ServiceResponse<List<Gateway>> gatewayResponse = gatewayService.getAll(tenant, application, user.getParentUser());

        if (!gatewayResponse.isOk()) {
            throw new BadServiceResponseException( gatewayResponse, validationsCode);
        } else {
            List<GatewayVO> gatewaysVO = new ArrayList<>();

            gatewaysVO.addAll(new GatewayVO().apply(gatewayResponse.getResult()));

            return gatewaysVO;
        }

    }

    @GetMapping(path = "/{gatewayGuid}")
    @PreAuthorize("hasAuthority('SHOW_GATEWAY')")
    @ApiOperation(
            value = "Get a gateway by guid",
            response = RestResponse.class
    )
    public GatewayVO read(@PathVariable("application") String applicationId,
                             @PathVariable("gatewayGuid") String gatewayGuid) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        ServiceResponse<Gateway> gatewayResponse = gatewayService.getByGUID(tenant, application, gatewayGuid);

        if (!gatewayResponse.isOk()) {
            throw new NotFoundResponseException(gatewayResponse);
        } else {
            return new GatewayVO().apply(gatewayResponse.getResult());
        }

    }

    @GetMapping(path = "/{gatewayGuid}/token")
    @PreAuthorize("hasAuthority('EDIT_GATEWAY')")
    @ApiOperation(
            value = "Requests a OAuth token for the gateway",
            response = RestResponse.class
    )
    public OAuth2AccessToken token(@PathVariable("application") String applicationId,
                                   @PathVariable("gatewayGuid") String gatewayGuid) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        ServiceResponse<Gateway> gatewayResponse = gatewayService.getByGUID(tenant, application, gatewayGuid);
        Gateway gateway;

        if (!gatewayResponse.isOk()) {
            throw new NotFoundResponseException(gatewayResponse);
        } else {
            gateway = gatewayResponse.getResult();
        }

        ServiceResponse<OAuth2AccessToken> accessTokenServiceResponse = oAuth2AccessTokenService.getGatewayAccessToken(tenant, application, gateway);
        if (accessTokenServiceResponse.isOk()) {
            return accessTokenServiceResponse.getResult();
        } else {
            throw new BadServiceResponseException( accessTokenServiceResponse, validationsCode);
        }

    }

    @PostMapping(path = "/{gatewayGuid}/devices")
    @PreAuthorize("hasAuthority('EDIT_GATEWAY')")
    @ApiOperation(
            value = "Create devices associated with gateway and return its credentials",
            response = RestResponse.class
    )
    public List<DeviceRegisterGatewayVO> createDevices(@PathVariable("application") String applicationId,
                                                           @PathVariable("gatewayGuid") String gatewayGuid,
                                                           @ApiParam(name = "body", required = true)
                                                           @RequestBody List<DeviceInputVO> devices) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);
        Gateway gateway = gatewayService.getByGUID(tenant, application, gatewayGuid).getResult();
        return devices.stream().map(deviceInputVO ->
            deviceRegisterService.register(
                    tenant,
                    application,
                    Device.builder()
                            .deviceId(deviceInputVO.getId())
                            .name(deviceInputVO.getName())
                            .application(application)
                            .location(gateway.getLocation())
                            .active(true)
                            .build())
        ).filter(deviceRegisterServiceResponse -> deviceRegisterServiceResponse.isOk()
        ).map(response ->
            deviceRegisterService.generateSecurityPassword(
                    tenant,
                    application,
                    response.getResult().getGuid())
        ).map(responseCredential ->
            new DeviceRegisterGatewayVO(
                    responseCredential.getResult(),
                    deviceRegisterService.getDeviceDataURLs(
                            tenant,
                            application,
                            responseCredential.getResult().getDevice(),
                            user.getLanguage().getLocale()
                    ).getResult()
            )
        ).collect(Collectors.toList());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_GATEWAY')")
    @ApiOperation(
            value = "Create a gateway",
            response = GatewayVO.class
            )
    public GatewayVO create(
            @PathVariable("application") String applicationId,
            @ApiParam(name = "body", required = true)
            @RequestBody GatewayInputVO gatewayForm) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);
        Location location = getLocation(tenant, application, gatewayForm.getLocationName());

        Gateway gateway = Gateway.builder()
                .name(gatewayForm.getName())
                .description(gatewayForm.getDescription())
                .location(location)
                .active(true)
                .build();

        ServiceResponse<Gateway> gatewayResponse = gatewayService.save(tenant, application, gateway);

        if (!gatewayResponse.isOk()) {
            throw new BadServiceResponseException( gatewayResponse, validationsCode);
        } else {
            return new GatewayVO().apply(gatewayResponse.getResult());
        }

    }

    @PutMapping(path = "/{gatewayGuid}")
    @PreAuthorize("hasAuthority('EDIT_GATEWAY')")
    @ApiOperation(value = "Update a gateway")
    public void update(
            @PathVariable("application") String applicationId,
            @PathVariable("gatewayGuid") String gatewayGuid,
            @ApiParam(name = "body", required = true)
            @RequestBody GatewayInputVO gatewayForm) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);
        Location location = getLocation(tenant, application, gatewayForm.getLocationName());

        Gateway gatewayFromDB;
        ServiceResponse<Gateway> gatewayResponse = gatewayService.getByGUID(tenant, application, gatewayGuid);

        if (!gatewayResponse.isOk()) {
            throw new BadServiceResponseException( gatewayResponse, validationsCode);
        } else {
            gatewayFromDB = gatewayResponse.getResult();
        }

        // update fields
        gatewayFromDB.setName(gatewayForm.getName());
        gatewayFromDB.setDescription(gatewayForm.getDescription());
        gatewayFromDB.setLocation(location);
        gatewayFromDB.setActive(gatewayForm.isActive());

        ServiceResponse<Gateway> updateResponse = gatewayService.update(tenant, application, gatewayGuid, gatewayFromDB);

        if (!updateResponse.isOk()) {
            throw new BadServiceResponseException( updateResponse, validationsCode);
        }

    }

    @DeleteMapping(path = "/{gatewayGuid}")
    @PreAuthorize("hasAuthority('REMOVE_GATEWAY')")
    @ApiOperation(value = "Delete a gateway")
    public void delete(
            @PathVariable("application") String applicationId,
            @PathVariable("gatewayGuid") String gatewayGuid) throws BadServiceResponseException, NotFoundResponseException {

        Tenant tenant = user.getTenant();
        Application application = getApplication(applicationId);

        ServiceResponse<Gateway> gatewayResponse = gatewayService.remove(tenant, application, gatewayGuid);

        if (!gatewayResponse.isOk()) {
            if (gatewayResponse.getResponseMessages().containsKey(Validations.GATEWAY_NOT_FOUND.getCode())) {
                throw new NotFoundResponseException(gatewayResponse);
            } else {
                throw new BadServiceResponseException( gatewayResponse, validationsCode);
            }
        }

    }

    @Override
    public void afterPropertiesSet() {

        for (Validations value : Validations.values()) {
            validationsCode.add(value.getCode());
        }

        for (Gateway.Validations value : Gateway.Validations.values()) {
            validationsCode.add(value.getCode());
        }

        for (LocationService.Validations value : LocationService.Validations.values()) {
            validationsCode.add(value.getCode());
        }

        for (ApplicationService.Validations value : ApplicationService.Validations.values()) {
            validationsCode.add(value.getCode());
        }
        
        validationsCode.add(LocationService.Messages.LOCATION_NOT_FOUND.getCode());

    }

}
