package zos.shell.service.tso;

import zos.shell.response.ResponseStatus;
import zowe.client.sdk.zostso.method.IssueTso;

import java.util.concurrent.Callable;

public class FutureTso extends Tso implements Callable<ResponseStatus> {

    private final String command;

    public FutureTso(final IssueTso issueTso, final String accountNumber, final String command) {
        super(issueTso, accountNumber);
        this.command = command;
    }

    @Override
    public ResponseStatus call() {
        return this.issueCommand(command);
    }

}
