package com.konkerlabs.platform.registry.api.web.controller;

import com.konkerlabs.platform.registry.api.exceptions.BadServiceResponseException;
import com.konkerlabs.platform.registry.api.exceptions.NotAuthorizedResponseException;
import com.konkerlabs.platform.registry.api.exceptions.NotFoundResponseException;
import com.konkerlabs.platform.registry.api.model.DeviceVO;
import com.konkerlabs.platform.registry.api.model.TransformationVO;
import com.konkerlabs.platform.registry.business.model.Device;
import com.konkerlabs.platform.registry.business.model.Transformation;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.services.api.DeviceRegisterService;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.TransformationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

@RestController
@Scope("request")
@RequestMapping(
        value = "/restTransformations"
)
@Api(tags = "restTransformations")
public class TransformationsRestController implements InitializingBean {


    @Autowired
    private TransformationService transformationService;

    @Autowired
    private User user;

    private Set<String> validationsCode = new HashSet<>();


    @GetMapping(path = "/")
    @PreAuthorize("hasAuthority('LIST_DEVICES')")
    @ApiOperation(
            value = "List all transformations by organization")
    public List<TransformationVO> list() throws BadServiceResponseException, NotFoundResponseException {
        ServiceResponse<List<Transformation>> response
                = transformationService .getAll(user.getTenant());

        if(!response.isOk()) {
            if(response.getResponseMessages().containsKey(TransformationService.Validations.TRANSFORMATION_NOT_FOUND)){
                throw new NotFoundResponseException(user, response);
            } else {
                throw new BadServiceResponseException(
                        user,
                        response,
                        validationsCode);
            }
        }
        return new TransformationVO().apply(response.getResult());

    }

    @GetMapping(path = "/{guid}")
    @PreAuthorize("hasAuthority('SHOW_TRANSFORMATION')")
    @ApiOperation(
            value = "Get the transformation by guid")
    public TransformationVO get(@PathVariable("guid") String guid) throws NotFoundResponseException, BadServiceResponseException, NotAuthorizedResponseException {
        ServiceResponse<Transformation> response =
                transformationService.get(user.getTenant(), guid);

        if(!response.isOk()) {
            if(response.getResponseMessages()
                    .containsKey(TransformationService.Validations.TRANSFORMATION_NOT_FOUND)){
                throw new NotFoundResponseException(user, response);
            } else if(response.getResponseMessages()
                    .containsKey(TransformationService.Validations.TRANSFORMATION_BELONG_ANOTHER_TENANT)){
                throw new NotAuthorizedResponseException(
                        user,
                        response,
                        validationsCode);
            }
            else {
                throw new BadServiceResponseException(
                        user,
                        response,
                        validationsCode);
            }
        }
        return new TransformationVO().apply(response.getResult());

    }

    @PutMapping(path = "/{guid}")
    @PreAuthorize("hasAuthority('EDIT_TRANSFORMATION')")
    @ApiOperation(
            value = "Edit the transformation by guid")
    public void put(@PathVariable("guid") String guid, @RequestBody TransformationVO vo)
            throws BadServiceResponseException, NotFoundResponseException, NotAuthorizedResponseException {
        if(!guid.equals(vo.getGuid())){
            throw new BadServiceResponseException(user, null, validationsCode);
        }
        ServiceResponse<Transformation> fromDB =
                transformationService.get(user.getTenant(), guid);

        if(!fromDB.isOk() || fromDB.getResult() == null){
            throw new NotFoundResponseException(user, fromDB);
        }

        Transformation toDB = vo.applyDB(fromDB.getResult());
        ServiceResponse<Transformation> response =
                transformationService.update(user.getTenant(), guid, toDB);

        if(!response.isOk()){
            if(response.getResponseMessages()
                    .containsKey(TransformationService.Validations
                            .TRANSFORMATION_BELONG_ANOTHER_TENANT)){
                throw new NotAuthorizedResponseException(user, response, validationsCode);
            }
        }

    }


    @Override
    public void afterPropertiesSet() throws Exception {

        for (com.konkerlabs.platform.registry.business.services.api.TransformationService.Validations value :
                TransformationService.Validations.values()) {
            validationsCode.add(value.getCode());
        }

        for (com.konkerlabs.platform.registry.business.model.Transformation.Validations value :
                Transformation.Validations.values()) {
            validationsCode.add(value.getCode());
        }

    }
}
