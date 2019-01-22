package com.altona.dashboard.service;

public class ServiceResponse {

    private Integer code;

    private String value;
    private String message;

    static ServiceResponse success(int code, String value) {
        return new ServiceResponse(code, value, null);
    }

    static ServiceResponse failure(String message) {
        return new ServiceResponse(null, null, message);
    }

    private ServiceResponse(Integer code, String value, String message) {
        this.code = code;
        this.value = value;
        this.message = message;
    }

    public void consume(SuccessConsumer successConsumer, FailureConsumer failureConsumer) {
        if (code != null) {
            successConsumer.accept(code, value);
        } else {
            failureConsumer.accept(value);
        }
    }

    public interface SuccessConsumer {

        void accept(int code, String value);

    }

    public interface FailureConsumer {

        void accept(String message);

    }

}
