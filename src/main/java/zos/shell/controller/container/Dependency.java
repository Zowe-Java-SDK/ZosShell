package zos.shell.controller.container;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zowe.client.sdk.core.SshConnection;
import zowe.client.sdk.core.ZosConnection;

public class Dependency {

    private static final Logger LOG = LoggerFactory.getLogger(Dependency.class);

    private ZosConnection zosConnection;
    private SshConnection sshConnection;
    private String data;
    private boolean toggle;
    private long timeout;

    public Dependency(final ZosConnection zosConnection, final long timeout) {
        LOG.debug("*** DependencyContainer zosConnection timeout ***");
        this.zosConnection = zosConnection;
        this.timeout = timeout;
    }

    public Dependency(final ZosConnection zosConnection, final boolean toggle, final long timeout) {
        LOG.debug("*** DependencyContainer zosConnection toggle timeout ***");
        this.zosConnection = zosConnection;
        this.toggle = toggle;
        this.timeout = timeout;
    }

    public Dependency(final ZosConnection zosConnection) {
        LOG.debug("*** DependencyContainer zosConnection ***");
        this.zosConnection = zosConnection;
    }

    public Dependency(final SshConnection sshConnection) {
        LOG.debug("*** DependencyContainer sshConnection ***");
        this.sshConnection = sshConnection;
    }

    public Dependency(final ZosConnection zosConnection, final String data, final long timeout) {
        LOG.debug("*** DependencyContainer zosConnection data timeout ***");
        this.zosConnection = zosConnection;
        if (data == null) {
            this.data = "";
        } else {
            this.data = data.trim();
        }
        this.timeout = timeout;
    }

    public boolean isZosConnectionSame(final ZosConnection zosConnection) {
        LOG.debug("*** isZosConnectionSame ***");
        return this.zosConnection.equals(zosConnection);
    }

    public boolean isSshConnectionSame(final SshConnection sshConnection) {
        LOG.debug("*** isSshConnectionSame ***");
        return this.sshConnection.equals(sshConnection);
    }

    public boolean isToggleSame(final boolean toggle) {
        LOG.debug("*** isToggleSame ***");
        return this.toggle == toggle;
    }

    private boolean isDataSame(final String data) {
        LOG.debug("*** isDataSame ***");
        return this.data.equals(data);
    }
    
    public boolean isTimeoutSame(final long timeout) {
        LOG.debug("*** isTimeoutSame ***");
        return this.timeout == timeout;
    }

    public boolean isValid(ZosConnection connection, boolean toggle, long timeout) {
        return !isZosConnectionSame(connection) || !isTimeoutSame(timeout) || isToggleSame(toggle);
    }

    public boolean isValid(ZosConnection connection, String data, long timeout) {
        return !isZosConnectionSame(connection) || !isTimeoutSame(timeout) || isDataSame(data);
    }

    public boolean isValid(ZosConnection connection, long timeout) {
        return isZosConnectionSame(connection) && isTimeoutSame(timeout);
    }

}
