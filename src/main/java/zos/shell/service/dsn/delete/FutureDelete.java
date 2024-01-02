package zos.shell.service.dsn.delete;

import com.google.common.base.Strings;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zowe.client.sdk.zosfiles.dsn.methods.DsnDelete;

import java.util.concurrent.Callable;

public class FutureDelete extends Delete implements Callable<ResponseStatus> {

    private final String dataset;
    private final String member;

    public FutureDelete(final DsnDelete dsnDelete, final String dataset, final String member) {
        super(dsnDelete);
        this.dataset = dataset;
        this.member = member;
    }

    public FutureDelete(final DsnDelete dsnDelete, final String dataset) {
        super(dsnDelete);
        this.dataset = dataset;
        this.member = "";
    }

    @Override

    public ResponseStatus call() {
        ResponseStatus responseStatus;
        String item;
        if (member.isBlank()) {
            responseStatus = this.delete(dataset);
            item = dataset;
        } else {
            responseStatus = this.delete(dataset, member);
            item = member;
        }

        final var arrowMsg = Strings.padStart(item, Constants.STRING_PAD_LENGTH, ' ') + Constants.ARROW;
        if (responseStatus.isStatus()) { // success state has No Content in return phrase
            responseStatus.setMessage(arrowMsg);
        } else { // error state
            responseStatus.setMessage(arrowMsg + responseStatus.getMessage());
        }
        return responseStatus;

    }

}
