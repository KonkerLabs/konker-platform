package com.konkerlabs.platform.registry.api.web.wrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import com.konkerlabs.platform.registry.api.exceptions.BadRequestResponseException;
import com.konkerlabs.platform.registry.api.exceptions.NotAuthorizedResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.konkerlabs.platform.registry.api.exceptions.BadServiceResponseException;
import com.konkerlabs.platform.registry.api.exceptions.NotFoundResponseException;
import com.konkerlabs.platform.registry.api.model.RestResponseBuilder;

@ControllerAdvice(basePackages = "com.konkerlabs.platform.registry.api.web.controller")
@Order(1)
public class CrudResponseAdvice implements ResponseBodyAdvice<Object> {

    @Autowired
    private MessageSource messageSource;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
                                  ServerHttpResponse response) {

        HttpStatus httpStatus = HttpStatus.OK;

        if (request.getMethod().equals(HttpMethod.DELETE)) {
            httpStatus = HttpStatus.NO_CONTENT;
        } else if (request.getMethod().equals(HttpMethod.POST)) {
            httpStatus = HttpStatus.CREATED;
        } else if (request.getMethod().equals(HttpMethod.PUT)) {
            httpStatus = HttpStatus.OK;
        } else if (request.getMethod().equals(HttpMethod.GET)) {
            if (body == null) {
                httpStatus = HttpStatus.NOT_FOUND;
            }
        }
        response.setStatusCode(httpStatus);
        return RestResponseBuilder.ok().withHttpStatus(httpStatus).withResult(body).getResponse();

    }

    @ExceptionHandler(BadServiceResponseException.class)
    public ResponseEntity<?> exception(BadServiceResponseException e) {

        if (e.hasValidationsError()) {
            return RestResponseBuilder.error().withHttpStatus(HttpStatus.BAD_REQUEST).withMessages(getI18NMessages(e.getResponseMessages())).build();

        } else {
            return RestResponseBuilder.error().withHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR).withMessages(getI18NMessages(e.getResponseMessages())).build();

        }

    }

    @ExceptionHandler(NotFoundResponseException.class)
    public ResponseEntity<?> exception(NotFoundResponseException e) {

        return RestResponseBuilder.error().withHttpStatus(HttpStatus.NOT_FOUND).withMessages(getI18NMessages(e.getResponseMessages())).build();

    }

    @ExceptionHandler(NotAuthorizedResponseException.class)
    public ResponseEntity<?> exception(NotAuthorizedResponseException e) {

        return RestResponseBuilder.error()
                .withHttpStatus(HttpStatus.FORBIDDEN)
                .withMessages(getI18NMessages(e.getResponseMessages())).build();

    }

    @ExceptionHandler(BadRequestResponseException.class)
    public ResponseEntity<?> exception(BadRequestResponseException e) {

        if (e.getMessage() != null) {

            return RestResponseBuilder.error()
                    .withHttpStatus(HttpStatus.BAD_REQUEST)
                    .withMessage(e.getMessage()).build();

        } else {

            return RestResponseBuilder.error()
                    .withHttpStatus(HttpStatus.BAD_REQUEST)
                    .withMessages(getI18NMessages(e.getResponseMessages())).build();

        }

    }


    private List<String> getI18NMessages(Map<String, Object[]> map) {

        List<String> messages = new ArrayList<>();

        if (map != null) {


            for (Entry<String, Object[]> v : map.entrySet()) {
                messages.add(messageSource.getMessage(v.getKey(), v.getValue(), Locale.ENGLISH));
            }
        }

        return messages;

    }

}
