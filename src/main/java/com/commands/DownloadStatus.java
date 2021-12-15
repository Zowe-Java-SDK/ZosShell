package com.commands;

public class DownloadStatus {

    private String message;
    private boolean status;

    public DownloadStatus(String message, boolean status) {
        this.message = message;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public boolean isStatus() {
        return status;
    }

}
