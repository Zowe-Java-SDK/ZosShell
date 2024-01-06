package zos.shell.utility;

import zowe.client.sdk.rest.Response;

public final class ResponseUtil {

    public static String getResponsePhrase(Response response) {
        if (response == null || response.getResponsePhrase().isEmpty()) {
            return null;
        }
        return response.getResponsePhrase().get().toString();
    }

}
