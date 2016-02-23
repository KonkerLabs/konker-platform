package com.konkerlabs.platform.registry.business.exceptions;

public class BusinessException extends Exception {

    private static final long serialVersionUID = -6355633603508946711L;

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
