package zos.shell.future;

import zos.shell.service.console.MvsCmd;
import zos.shell.response.ResponseStatus;
import zowe.client.sdk.core.ZosConnection;

import java.util.concurrent.Callable;

public class FutureMvs extends MvsCmd implements Callable<ResponseStatus> {

    private final String command;

    public FutureMvs(ZosConnection connection, String command) {
        super(connection);
        this.command = command;
    }

    @Override
    public ResponseStatus call() {
        return this.executeCommand(command);
    }

}
