package zos.shell.response;

public class ResponseStatus {

    private final String message;
    private final boolean status;
    private String optionalData;

    public ResponseStatus(final String message, final boolean status) {
        this.message = message;
        this.status = status;
    }

    public ResponseStatus(final String message, final boolean status, final String optionalData) {
        this.message = message;
        this.status = status;
        this.optionalData = optionalData;
    }

    public String getMessage() {
        return message;
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
