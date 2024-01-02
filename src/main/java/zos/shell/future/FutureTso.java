package zos.shell.future;

import zos.shell.service.tso.TsoCmd;
import zos.shell.response.ResponseStatus;
import zowe.client.sdk.core.ZosConnection;

import java.util.concurrent.Callable;

public class FutureTso extends TsoCmd implements Callable<ResponseStatus> {

    private final String command;

    public FutureTso(ZosConnection connection, String accountNumber, String command) {
        super(connection, accountNumber);
        this.command = command;
    }

    @Override
    public ResponseStatus call() {
        return this.executeCommand(command);
    }

}
