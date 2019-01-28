package com.altona.repository.integration.maconomy;

public class MaconomyException extends RuntimeException {

    public MaconomyException(String message) {
        super(message);
    }

    public MaconomyException(String message, Throwable cause) {
        super(message, cause);
    }

}
