package com.konkerlabs.platform.registry.api.web.controller;

import com.konkerlabs.platform.registry.api.exceptions.BadServiceResponseException;
import com.konkerlabs.platform.registry.api.exceptions.NotFoundResponseException;
import com.konkerlabs.platform.registry.api.model.GatewayInputVO;
import com.konkerlabs.platform.registry.api.model.GatewayVO;
import com.konkerlabs.platform.registry.api.model.RestResponse;
import com.konkerlabs.platform.registry.business.model.Application;
import com.konkerlabs.platform.registry.business.model.Gateway;
import com.konkerlabs.platform.registry.business.model.Location;
import com.konkerlabs.platform.registry.business.model.Tenant;
import com.konkerlabs.platform.registry.business.services.api.GatewayService;
import com.konkerlabs.platform.registry.business.services.api.GatewayService.Validations;
import com.konkerlabs.platform.registry.business.services.api.LocationService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@Scope("request")
@RequestMapping(value = "/{application}/gateways")
@Api(tags = "gateways")
public class GatewayRestController extends AbstractRestController implements InitializingBean {

    @Autowired
    private GatewayService gatewayService;

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

        ServiceResponse<List<Gateway>> gatewayResponse = gatewayService.getAll(tenant, application);

        if (!gatewayResponse.isOk()) {
            throw new BadServiceResponseException(user, gatewayResponse, validationsCode);
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
            throw new NotFoundResponseException(user, gatewayResponse);
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
            throw new NotFoundResponseException(user, gatewayResponse);
        } else {
            gateway = gatewayResponse.getResult();
        }

        ServiceResponse<OAuth2AccessToken> accessTokenServiceResponse = oAuth2AccessTokenService.getGatewayAccessToken(tenant, application, gateway);
        if (accessTokenServiceResponse.isOk()) {
            return accessTokenServiceResponse.getResult();
        } else {
            throw new BadServiceResponseException(user, accessTokenServiceResponse, validationsCode);
        }

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
            throw new BadServiceResponseException(user, gatewayResponse, validationsCode);
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
            throw new BadServiceResponseException(user, gatewayResponse, validationsCode);
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
            throw new BadServiceResponseException(user, updateResponse, validationsCode);
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
                throw new NotFoundResponseException(user, gatewayResponse);
            } else {
                throw new BadServiceResponseException(user, gatewayResponse, validationsCode);
            }
        }

    }

    @Override
    public void afterPropertiesSet() throws Exception {

        for (Validations value : Validations.values()) {
            validationsCode.add(value.getCode());
        }

        for (Gateway.Validations value : Gateway.Validations.values()) {
            validationsCode.add(value.getCode());
        }

        for (LocationService.Validations value : LocationService.Validations.values()) {
            validationsCode.add(value.getCode());
        }
        
        validationsCode.add(LocationService.Messages.LOCATION_NOT_FOUND.getCode());

    }

}
