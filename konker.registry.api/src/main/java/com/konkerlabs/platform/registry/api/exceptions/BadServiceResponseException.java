package com.konkerlabs.platform.registry.api.exceptions;

import java.util.List;

public class BadServiceResponseException extends Exception {

    private static final long serialVersionUID = -854909746416282903L;

    public boolean hasValidationsError() {
        // TODO Auto-generated method stub
        return false;
    }

    public List<String> getMessages() {
        // TODO Auto-generated method stub
        return null;
    }

}
