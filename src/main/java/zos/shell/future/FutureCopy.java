package zos.shell.future;

import zos.shell.Constants;
import zos.shell.commands.Copy;
import zos.shell.dto.ResponseStatus;
import com.google.common.base.Strings;
import zowe.client.sdk.zosfiles.ZosDsnCopy;

import java.util.concurrent.Callable;

public class FutureCopy extends Copy implements Callable<ResponseStatus> {

    private final String fromDataSetName;
    private final String toDataSetName;
    private final String member;

    public FutureCopy(ZosDsnCopy zosDsnCopy, String fromDataSetName, String toDataSetName, String member) {
        super(zosDsnCopy);
        this.fromDataSetName = fromDataSetName;
        this.toDataSetName = toDataSetName;
        this.member = member;
    }

    @Override
    public ResponseStatus call() {
        final var params = new String[]{"cp", member, toDataSetName};
        var message = Strings.padStart(member, Constants.STRING_PAD_LENGTH, ' ') + Constants.ARROW;
        final var response = this.copy(fromDataSetName, params);
        var responseMsg = response.getMessage();
        if (responseMsg.contains("Http error")) {
            var index = responseMsg.indexOf(".");
            responseMsg = responseMsg.substring(0, index + 1);
            responseMsg += " Perform individual copy for more info.";
        }
        message += responseMsg;
        response.setMessage(message);
        return response;
    }

}
