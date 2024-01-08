package zos.shell.service.dsn.copy;

import com.google.common.base.Strings;
import zos.shell.constants.Constants;
import zos.shell.response.ResponseStatus;
import zowe.client.sdk.zosfiles.dsn.methods.DsnCopy;

import java.util.concurrent.Callable;

public class FutureCopy extends Copy implements Callable<ResponseStatus> {

    private final String fromDataSetName;
    private final String toDataSetName;
    private final boolean isCopyAll;

    public FutureCopy(final DsnCopy DsnCopy, final String fromDataSetName,
                      final String toDataSetName, boolean isCopyAll) {
        super(DsnCopy);
        this.fromDataSetName = fromDataSetName;
        this.toDataSetName = toDataSetName;
        this.isCopyAll = isCopyAll;
    }

    @Override
    public ResponseStatus call() {
        return this.copy(fromDataSetName, toDataSetName, isCopyAll);
    }

}
