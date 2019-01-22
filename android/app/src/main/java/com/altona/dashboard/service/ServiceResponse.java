package com.altona.dashboard.service;

public class ServiceResponse {

    private int code;
    private String value;

    ServiceResponse(Integer code, String value) {
        this.code = code;
        this.value = value;
    }

    public int getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }
}
