package com.konkerlabs.platform.utilities.validations;

public class ValidationException extends Exception {

    private static final long serialVersionUID = 533580554403643750L;
    
    public ValidationException(String msg) {
        super(msg);
    }

    public ValidationException(String msg, Throwable rootCause) {
        super(msg, rootCause);
    }

}
 