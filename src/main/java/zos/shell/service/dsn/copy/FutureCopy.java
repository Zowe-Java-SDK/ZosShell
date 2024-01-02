package zos.shell.service.dsn.copy;

import com.google.common.base.Strings;
import zos.shell.constants.Constants;
import zos.shell.service.dsn.copy.CopyCmd;
import zos.shell.response.ResponseStatus;
import zowe.client.sdk.zosfiles.dsn.methods.DsnCopy;

import java.util.concurrent.Callable;

public class FutureCopy extends Copy implements Callable<ResponseStatus> {

    private final String fromDataSetName;
    private final String toDataSetName;
    private final String member;

    public FutureCopy(DsnCopy DsnCopy, String fromDataSetName, String toDataSetName, String member) {
        super(DsnCopy);
        this.fromDataSetName = fromDataSetName;
        this.toDataSetName = toDataSetName;
        this.member = member;
    }

    @Override
    public ResponseStatus call() {
        final var params = new String[]{"cp", member, toDataSetName};
        final var arrowMsg = Strings.padStart(member, Constants.STRING_PAD_LENGTH, ' ') + Constants.ARROW;
        final var responseStatus = this.copy(fromDataSetName, params);
        responseStatus.setMessage(arrowMsg + responseStatus.getMessage());
        return responseStatus;
    }

}
