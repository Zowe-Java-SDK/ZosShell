package zos.shell.singleton;

import zowe.client.sdk.core.SshConnection;
import zowe.client.sdk.core.ZosConnection;

public class ConnSingleton {

    private ZosConnection currZosConnection;
    private SshConnection currSshConnection;

    private static class Holder {
        private static final ConnSingleton instance = new ConnSingleton();
    }

    private ConnSingleton() {
    }

    public static ConnSingleton getInstance() {
        return ConnSingleton.Holder.instance;
    }

    public ZosConnection getCurrZosConnection() {
        return currZosConnection;
    }

    public void setCurrZosConnection(final ZosConnection currZosConnection) {
        this.currZosConnection = currZosConnection;
    }

    public SshConnection getCurrSshConnection() {
        return currSshConnection;
    }

    public void setCurrSshConnection(final SshConnection currSshConnection) {
        this.currSshConnection = currSshConnection;
    }

}
