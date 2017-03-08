package com.konkerlabs.platform.registry.api.web.controller;

import com.konkerlabs.platform.registry.api.exceptions.BadServiceResponseException;
import com.konkerlabs.platform.registry.api.exceptions.NotAuthorizedResponseException;
import com.konkerlabs.platform.registry.api.exceptions.NotFoundResponseException;
import com.konkerlabs.platform.registry.api.model.RestTransformationVO;
import com.konkerlabs.platform.registry.business.model.RestTransformationStep;
import com.konkerlabs.platform.registry.business.model.Transformation;
import com.konkerlabs.platform.registry.business.model.User;
import com.konkerlabs.platform.registry.business.services.api.ServiceResponse;
import com.konkerlabs.platform.registry.business.services.api.TransformationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@Scope("request")
@RequestMapping(
        value = "/restTransformations"
)
@Api(tags = "rest transformations")
public class TransformationsRestController implements InitializingBean {


    @Autowired
    private TransformationService transformationService;
    @Autowired
    private User user;
    private Set<String> validationsCode = new HashSet<>();

    @GetMapping(path = "/")
    @PreAuthorize("hasAuthority('LIST_TRANSFORMATION')")
    @ApiOperation(
            value = "List all transformations by organization")
    public List<RestTransformationVO> list() throws BadServiceResponseException, NotFoundResponseException {
        ServiceResponse<List<Transformation>> response
                = transformationService.getAll(user.getTenant());

        if (!response.isOk()) {
            if (response.getResponseMessages().containsKey(TransformationService.Validations.TRANSFORMATION_NOT_FOUND)) {
                throw new NotFoundResponseException(user, response);
            } else {
                throw new BadServiceResponseException(
                        user,
                        response,
                        validationsCode);
            }
        }
        return new RestTransformationVO().apply(response.getResult());

    }

    @GetMapping(path = "/{guid}")
    @PreAuthorize("hasAuthority('SHOW_TRANSFORMATION')")
    @ApiOperation(
            value = "Get the transformation by guid")
    public RestTransformationVO read(@PathVariable("guid") String guid) throws NotFoundResponseException, BadServiceResponseException, NotAuthorizedResponseException {
        ServiceResponse<Transformation> response =
                transformationService.get(user.getTenant(), guid);

        if (!response.isOk() || response.getResult() == null) {
            if (response.getResponseMessages()
                    .containsKey(TransformationService.Validations.TRANSFORMATION_NOT_FOUND.getCode())) {
                throw new NotFoundResponseException(user, response);
            } else if (response.getResponseMessages()
                    .containsKey(TransformationService.Validations.TRANSFORMATION_BELONG_ANOTHER_TENANT.getCode())) {
                throw new NotAuthorizedResponseException(
                        user,
                        response,
                        validationsCode);
            } else {
                throw new BadServiceResponseException(
                        user,
                        response,
                        validationsCode);
            }
        }
        return new RestTransformationVO().apply(response.getResult());

    }

    @PutMapping(path = "/{guid}")
    @PreAuthorize("hasAuthority('EDIT_TRANSFORMATION')")
    @ApiOperation(
            value = "Edit the transformation by guid")
    public void update(@PathVariable("guid") String guid, @RequestBody RestTransformationVO vo)
            throws BadServiceResponseException, NotFoundResponseException, NotAuthorizedResponseException {
        if (!guid.equals(vo.getGuid())) {
            validationsCode.add(TransformationService.Validations.TRANSFORMATION_NOT_FOUND.getCode());
            throw new BadServiceResponseException(user, null, validationsCode);
        }
        ServiceResponse<Transformation> fromDB =
                transformationService.get(user.getTenant(), guid);

        if (!fromDB.isOk() || fromDB.getResult() == null) {
            throw new NotFoundResponseException(user, fromDB);
        }

        Transformation toDB = vo.patchDB(fromDB.getResult());
        ServiceResponse<Transformation> response =
                transformationService.update(user.getTenant(), guid, toDB);

        if (!response.isOk()) {
            if (response.getResponseMessages()
                    .containsKey(TransformationService.Validations
                            .TRANSFORMATION_BELONG_ANOTHER_TENANT.getCode())) {
                throw new NotAuthorizedResponseException(user, response, validationsCode);
            } else {
                throw new NotAuthorizedResponseException(user, response, validationsCode);
            }

        }
    }

    @PostMapping(path = "/")
    @PreAuthorize("hasAuthority('CREATE_TRANSFORMATION')")
    @ApiOperation(
            value = "Create the transformation")
    public RestTransformationVO create(@RequestBody RestTransformationVO vo)
            throws BadServiceResponseException, NotFoundResponseException, NotAuthorizedResponseException {

        Transformation toDB = vo.patchDB(Transformation.builder().build());
        ServiceResponse<Transformation> response =
                transformationService.register(user.getTenant(), toDB);

        if (!response.isOk()) {
            throw new BadServiceResponseException(user, response, validationsCode);
        }

        return new RestTransformationVO().apply(response.getResult());
    }


    @DeleteMapping("/{guid}")
    @PreAuthorize("hasAuthority('REMOVE_TRANSFORMATION')")
    @ApiOperation(
            value = "Remove the transformation by guid")
    public void delete(@PathVariable("guid") String guid) throws BadServiceResponseException, NotAuthorizedResponseException {

        if (StringUtils.isEmpty(guid)) {
            validationsCode.add(TransformationService.Validations.TRANSFORMATION_NOT_FOUND.getCode());
            throw new BadServiceResponseException(user, null, validationsCode);
        }
        ServiceResponse<Transformation> response = transformationService.remove(user.getTenant(), guid);
        if (!response.isOk()) {
            if (response.getResponseMessages()
                    .containsKey(TransformationService.Validations
                            .TRANSFORMATION_BELONG_ANOTHER_TENANT.getCode())) {
                throw new NotAuthorizedResponseException(user, response, validationsCode);
            } else {
                throw new BadServiceResponseException(user, response, validationsCode);
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
