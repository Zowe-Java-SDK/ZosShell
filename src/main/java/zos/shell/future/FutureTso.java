package zos.shell.future;

import zos.shell.commands.TsoCommand;
import zos.shell.dto.ResponseStatus;
import zowe.client.sdk.core.ZOSConnection;

import java.util.concurrent.Callable;

public class FutureTso extends TsoCommand implements Callable<ResponseStatus> {

    private final String command;

    public FutureTso(ZOSConnection connection, String accountNumber, String command) {
        super(connection, accountNumber);
        this.command = command;
    }

    @Override
    public ResponseStatus call() {
        return this.executeCommand(command);
    }

}
