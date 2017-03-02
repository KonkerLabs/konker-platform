package com.konkerlabs.platform.registry.api.web.wrapper;

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
import com.konkerlabs.platform.registry.api.model.RestResponseBuilder;

@ControllerAdvice
@Order(1)
public class CrudResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {

        String name = returnType.getMethod().getName();

        if (name.equals("list")) {
            return true;
        } else if (name.equals("delete")) {
            return true;
        } else if (name.equals("read")) {
            return true;
        }

        return false;

    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
            ServerHttpResponse response) {

            String name = returnType.getMethod().getName();

            if (name.equals("delete")) {
                return RestResponseBuilder.ok().withHttpStatus(HttpStatus.NO_CONTENT).withResult(body).getResponse();
            } else {
                return RestResponseBuilder.ok().withHttpStatus(HttpStatus.OK).withResult(body).getResponse();
            }

    }

    @ExceptionHandler(BadServiceResponseException.class)
    public ResponseEntity<?> exception(BadServiceResponseException e) {

        if (e.hasValidationsError()) {
            return RestResponseBuilder.error().withHttpStatus(HttpStatus.BAD_REQUEST).withMessages(e.getMessages()).build();

        } else {
            return RestResponseBuilder.error().withHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR).withMessages(e.getMessages()).build();

        }

    }

}
