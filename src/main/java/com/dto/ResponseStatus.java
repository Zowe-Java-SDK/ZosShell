package com.dto;

public class ResponseStatus {

    private final String message;
    private final boolean status;

    public ResponseStatus(String message, boolean status) {
        this.message = message;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public boolean isStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "ResponseStatus{" +
                "message='" + message + '\'' +
                ", status=" + status +
                '}';
    }

}
