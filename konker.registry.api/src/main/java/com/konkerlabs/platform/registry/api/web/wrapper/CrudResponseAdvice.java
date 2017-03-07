package com.konkerlabs.platform.registry.api.web.wrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.Order;
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

        String name = returnType.getMethod().getName();

        HttpStatus httpStatus = HttpStatus.OK;

        if (name.equals("delete")) {
            httpStatus = HttpStatus.NO_CONTENT;
        } else if (name.equals("create")) {
            httpStatus = HttpStatus.CREATED;
        }

        response.setStatusCode(httpStatus);
        return RestResponseBuilder.ok().withHttpStatus(httpStatus).withResult(body).getResponse();

    }

    @ExceptionHandler(BadServiceResponseException.class)
    public ResponseEntity<?> exception(BadServiceResponseException e) {

        if (e.hasValidationsError()) {
            return RestResponseBuilder.error().withHttpStatus(HttpStatus.BAD_REQUEST).withMessages(getI18NMessages(e.getResponseMessages(), e.getLocale())).build();

        } else {
            return RestResponseBuilder.error().withHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR).withMessages(getI18NMessages(e.getResponseMessages(), e.getLocale())).build();

        }

    }

    @ExceptionHandler(NotFoundResponseException.class)
    public ResponseEntity<?> exception(NotFoundResponseException e) {

         return RestResponseBuilder.error().withHttpStatus(HttpStatus.NOT_FOUND).withMessages(getI18NMessages(e.getResponseMessages(), e.getLocale())).build();

    }

    private List<String> getI18NMessages(Map<String, Object[]> map, Locale locale) {

        List<String> messages = new ArrayList<>();

        for (Entry<String, Object[]> v:  map.entrySet()) {
            messages.add(messageSource.getMessage(v.getKey(), v.getValue(), locale));
        }

        return messages;

    }

}
