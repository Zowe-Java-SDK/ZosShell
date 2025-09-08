package zos.shell.service.tso;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.response.ResponseStatus;
import zos.shell.utility.ResponseUtil;
import zowe.client.sdk.rest.exception.ZosmfRequestException;
import zowe.client.sdk.zostso.methods.TsoCmd;

import java.util.List;
import java.util.regex.Pattern;

public class Tso {

    private static final Logger LOG = LoggerFactory.getLogger(Tso.class);

    private final TsoCmd issueTso;

    public Tso(TsoCmd issueTso) {
        LOG.debug("*** Tso ***");
        this.issueTso = issueTso;
    }

    public ResponseStatus issueCommand(String command) {
        LOG.debug("*** issueCommand ***");

        var p = Pattern.compile("\"([^\"]*)\"");
        var m = p.matcher(command);
        while (m.find()) {
            command = m.group(1);
        }

        List<String> response;
        try {
            response = execute(command);
        } catch (ZosmfRequestException e) {
            final String errMsg = ResponseUtil.getResponsePhrase(e.getResponse());
            return new ResponseStatus((errMsg != null ? errMsg : e.getMessage()), false);
        }

        return new ResponseStatus(String.join("\n", response), true);
    }

    private List<String> execute(final String command) throws ZosmfRequestException {
        LOG.debug("*** issue ***");
        return issueTso.issueCommand(command);
    }

}
