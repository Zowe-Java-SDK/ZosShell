package zos.shell.singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zowe.client.sdk.core.SshConnection;
import zowe.client.sdk.core.ZosConnection;

public final class ConnSingleton {

    private static final Logger LOG = LoggerFactory.getLogger(ConnSingleton.class);

    private ZosConnection currZosConnection;
    private SshConnection currSshConnection;
    private int connectionIndex;

    private static class Holder {
        private static final ConnSingleton instance = new ConnSingleton();
    }

    private ConnSingleton() {
        LOG.debug("*** ConnSingleton ***");
    }

    public static ConnSingleton getInstance() {
        LOG.debug("*** getInstance ***");
        return ConnSingleton.Holder.instance;
    }

    public ZosConnection getCurrZosConnection() {
        LOG.debug("*** getCurrZosConnection ***");
        return currZosConnection;
    }

    public void setCurrZosConnection(final ZosConnection currZosConnection, int index) {
        LOG.debug("*** setCurrZosConnection ***");
        this.currZosConnection = currZosConnection;
        this.connectionIndex = index;
    }

    public SshConnection getCurrSshConnection() {
        LOG.debug("*** getCurrSshConnection ***");
        return currSshConnection;
    }

    public void setCurrSshConnection(final SshConnection currSshConnection) {
        LOG.debug("*** setCurrSshConnection ***");
        this.currSshConnection = currSshConnection;
    }

    public int getCurrZosConnectionIndex() {
        LOG.debug("*** getCurrZosConnectionIndex ***");
        return connectionIndex;
    }

}
