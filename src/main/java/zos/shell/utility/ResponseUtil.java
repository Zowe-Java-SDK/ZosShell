package zos.shell.utility;

import kong.unirest.core.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zowe.client.sdk.rest.Response;
import zowe.client.sdk.rest.exception.ZosmfRequestException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public final class ResponseUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ResponseUtil.class);

    private ResponseUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static ResponseStatus getByteResponseStatus(ZosmfRequestException e) {
        LOG.debug("*** getByteResponseStatus ***");
        var byteMsg = (byte[]) e.getResponse().getResponsePhrase().orElseThrow(() ->
                new IllegalStateException("Response phrase is not present"));
        var errorStream = new ByteArrayInputStream(byteMsg);
        String errMsg;
        try {
            errMsg = FileUtil.getTextStreamData(errorStream);
            if (errMsg == null) {
                errMsg = "";
            }
        } catch (IOException ex) {
            errMsg = "error processing response";
        }
        return new ResponseStatus(errMsg.isBlank() ? e.getMessage() : errMsg, false);
    }

    public static String getResponsePhrase(final Response response) {
        LOG.debug("*** getResponsePhrase ***");
        if (response == null || response.getResponsePhrase().isEmpty()) {
            return null;
        }
        if (response.getResponsePhrase().get() instanceof JSONObject &&
                ("{}".equals(response.getResponsePhrase().get().toString()))) {
            return "http status error code: " +
                    (response.getStatusCode().isPresent() ? response.getStatusCode().getAsInt() : "") +
                    ", status text: " + response.getStatusText().orElse("") + ", response phrase: " +
                    response.getResponsePhrase().get();
        }

        return response.getResponsePhrase().get().toString();
    }

    public static String getMsgAfterArrow(final String msg) {
        LOG.debug("*** getMsgAfterArrow ***");
        if (!msg.contains(Constants.ARROW)) {
            return msg;
        }
        int index = msg.indexOf(Constants.ARROW) + Constants.ARROW.length();
        return msg.substring(index);
    }

}
