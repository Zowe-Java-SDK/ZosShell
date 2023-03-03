package zos.shell.dto;

public class ResponseStatus {

    private String message;
    private final boolean status;
    private String optionalData;

    public ResponseStatus(String message, boolean status) {
        this.message = message;
        this.status = status;
    }

    public ResponseStatus(String message, boolean status, String optionalData) {
        this.message = message;
        this.status = status;
        this.optionalData = optionalData;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isStatus() {
        return status;
    }

    public String getOptionalData() {
        return optionalData;
    }

    @Override
    public String toString() {
        return "ResponseStatus{" +
                "message='" + message + '\'' +
                ", status=" + status +
                ", optionalData='" + optionalData + '\'' +
                '}';
    }

}
