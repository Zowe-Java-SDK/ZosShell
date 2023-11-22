package zos.shell.future;

import zos.shell.commands.MvsCommand;
import zos.shell.dto.ResponseStatus;
import zowe.client.sdk.core.ZosConnection;

import java.util.concurrent.Callable;

public class FutureMvs extends MvsCommand implements Callable<ResponseStatus> {

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
