package zos.shell.controller.container;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zowe.client.sdk.core.SshConnection;
import zowe.client.sdk.core.ZosConnection;

import java.util.Optional;

public class DependencyCacheContainer {

    private static final Logger LOG = LoggerFactory.getLogger(DependencyCacheContainer.class);

    private ZosConnection zosConnection;
    private SshConnection sshConnection;
    private String data;
    private boolean toggle;
    private long timeout;

    public DependencyCacheContainer(final ZosConnection zosConnection, final long timeout) {
        LOG.debug("*** DependencyContainer zosConnection timeout ***");
        this.zosConnection = zosConnection;
        this.timeout = timeout;
    }

    public DependencyCacheContainer(final ZosConnection zosConnection, boolean toggle, final long timeout) {
        LOG.debug("*** DependencyContainer zosConnection toggle timeout ***");
        this.zosConnection = zosConnection;
        this.toggle = toggle;
        this.timeout = timeout;
    }

    public DependencyCacheContainer(final ZosConnection zosConnection) {
        LOG.debug("*** DependencyContainer zosConnection ***");
        this.zosConnection = zosConnection;
    }

    public DependencyCacheContainer(final SshConnection sshConnection) {
        LOG.debug("*** DependencyContainer  sshConnection ***");
        this.sshConnection = sshConnection;
    }

    public DependencyCacheContainer(final ZosConnection zosConnection, final String data, final long timeout) {
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
        return this.zosConnection.getHost().equalsIgnoreCase(zosConnection.getHost()) &&
                this.zosConnection.getPassword().equalsIgnoreCase(zosConnection.getPassword()) &&
                this.zosConnection.getUser().equalsIgnoreCase(zosConnection.getUser()) &&
                this.zosConnection.getZosmfPort().equals(zosConnection.getZosmfPort());
    }

    public boolean isSshConnectionSame(final SshConnection sshConnection) {
        LOG.debug("*** isSshConnectionSame ***");
        return this.sshConnection.getHost().equalsIgnoreCase(sshConnection.getHost()) &&
                this.sshConnection.getPassword().equalsIgnoreCase(sshConnection.getPassword()) &&
                this.sshConnection.getUser().equalsIgnoreCase(sshConnection.getUser()) &&
                this.sshConnection.getPort() == sshConnection.getPort();
    }

    public boolean isDataSame(final String data) {
        LOG.debug("*** isDataSame ***");
        return this.data.equalsIgnoreCase(data);
    }

    public boolean isToggleSame(boolean toggle) {
        LOG.debug("*** isToggleSame ***");
        return this.toggle == toggle;
    }

    public boolean isTimeoutSame(final long timeout) {
        LOG.debug("*** isTimeoutSame ***");
        return this.timeout == timeout;
    }

}
