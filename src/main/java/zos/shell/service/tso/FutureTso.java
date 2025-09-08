package zos.shell.service.tso;

import zos.shell.response.ResponseStatus;
import zowe.client.sdk.zostso.methods.TsoCmd;

import java.util.concurrent.Callable;

public class FutureTso extends Tso implements Callable<ResponseStatus> {

    private final String command;

    public FutureTso(final TsoCmd issueTso, final String command) {
        super(issueTso);
        this.command = command;
    }

    @Override
    public ResponseStatus call() {
        return this.issueCommand(command);
    }

}
