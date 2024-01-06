package zos.shell.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zowe.client.sdk.rest.Response;

public final class ResponseUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ResponseUtil.class);

    public static String getResponsePhrase(final Response response) {
        LOG.debug("*** getResponsePhrase ***");
        if (response == null || response.getResponsePhrase().isEmpty()) {
            return null;
        }
        return response.getResponsePhrase().get().toString();
    }

    public static String getMsgAfterArrow(final String msg) {
        LOG.debug("*** getMsgAfterArrow ***");
        if (!msg.contains(Constants.ARROW)) {
            return msg;
        }
        final var index = msg.indexOf(Constants.ARROW) + Constants.ARROW.length();
        return msg.substring(index);
    }

}
