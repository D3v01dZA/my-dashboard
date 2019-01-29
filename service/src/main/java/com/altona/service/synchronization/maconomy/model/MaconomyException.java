package com.altona.service.synchronization.maconomy.model;

public class MaconomyException extends RuntimeException {

    public MaconomyException(String message) {
        super(message);
    }

    public MaconomyException(String message, Throwable cause) {
        super(message, cause);
    }

}
