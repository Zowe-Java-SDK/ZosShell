package zos.shell.service.connection;

import org.beryx.textio.TextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.service.terminal.TerminalOutputService;
import zos.shell.singleton.ConnSingleton;
import zos.shell.singleton.TerminalSingleton;
import zos.shell.singleton.configuration.ConfigSingleton;
import zos.shell.utility.PromptUtil;
import zowe.client.sdk.core.SshConnection;
import zowe.client.sdk.core.ZosConnection;
import zowe.client.sdk.core.ZosConnectionFactory;

public class ConnectionStartupService {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectionStartupService.class);
    private int connectionIdentifier;
    private TerminalOutputService terminalOutputService;

    public void initialize(final String connectionIdentifier, final TextTerminal<?> terminal) {
        LOG.debug("*** initialize ***");
        try {
            this.connectionIdentifier = Integer.parseInt(connectionIdentifier);
            this.connectionIdentifier--;
        } catch (NumberFormatException e) {
            // Use default index of zero for invalid input
            this.connectionIdentifier = 0;
        }
        this.terminalOutputService = new TerminalOutputService(terminal);
        selectConnection();
        validateSelectedConnection();
        updateTerminalTitle();
    }

    private void selectConnection() {
        LOG.debug("*** selectConnection ***");

        ConnSingleton connSingleton = ConnSingleton.getInstance();
        ConfigSingleton configSingleton = ConfigSingleton.getInstance();
        int numOfConnections = configSingleton.getZosConnections().size();

        // See if connectionIdentifier set to a value within range.
        if (this.connectionIdentifier >= 0 && this.connectionIdentifier < numOfConnections) {
            ZosConnection zosConnection = configSingleton.getZosConnectionByIndex(connectionIdentifier);
            connSingleton.setCurrZosConnection(zosConnection, connectionIdentifier);
            SshConnection sshConnection = configSingleton.getSshConnectionByIndex(connectionIdentifier);
            connSingleton.setCurrSshConnection(sshConnection);
            return;
        }

        // connectionIdentifier not in range; Default to first index which is zero if connections exist
        if (connSingleton.getCurrZosConnection() == null && numOfConnections > 0) {
            this.connectionIdentifier = 0;
            ZosConnection zosConnection = configSingleton.getZosConnectionByIndex(this.connectionIdentifier);
            connSingleton.setCurrZosConnection(zosConnection, this.connectionIdentifier);

            SshConnection sshConnection = configSingleton.getSshConnectionByIndex(this.connectionIdentifier);
            connSingleton.setCurrSshConnection(sshConnection);
        }
    }

    private void validateSelectedConnection() {
        LOG.debug("*** validateSelectedConnection ***");

        ConnSingleton connSingleton = ConnSingleton.getInstance();
        ConfigSingleton configSingleton = ConfigSingleton.getInstance();

        ZosConnection currConnection = connSingleton.getCurrZosConnection();
        SshConnection currSshConnection = connSingleton.getCurrSshConnection();

        if (currConnection == null || currSshConnection == null) {
            throw new IllegalStateException("No valid connection definition found in configuration.");
        }

        String host = currConnection.getHost();
        int zosmfPort = currConnection.getZosmfPort();
        String username = currConnection.getUser();
        String password = currConnection.getPassword();

        if (host.isBlank() || zosmfPort == 0) {
            throw new IllegalStateException(
                    "Error: Hostname or z/OSMF port value(s) missing\nCheck configuration file and try again..."
            );
        }

        final boolean authMissing =
                Constants.DEFAULT_EMPTY_USER_PASSWORD_VALUE.equals(username)
                        || Constants.DEFAULT_EMPTY_USER_PASSWORD_VALUE.equals(password);

        if (authMissing) {
            if (Constants.DEFAULT_EMPTY_USER_PASSWORD_VALUE.equals(username)) {
                this.terminalOutputService.println("Enter username for " + host);
                username = PromptUtil.getPromptInfo("username:", false);
            }

            if (Constants.DEFAULT_EMPTY_USER_PASSWORD_VALUE.equals(password)) {
                this.terminalOutputService.println("Enter password for " + host + "/" + username);
                String confirmPassword = null;
                while (confirmPassword == null || !confirmPassword.equals(password)) {
                    password = PromptUtil.getPromptInfo("password:", true);
                    confirmPassword = PromptUtil.getPromptInfo("confirm password:", true);
                }
            }

            currConnection = ZosConnectionFactory.createBasicConnection(host, zosmfPort, username, password);
            configSingleton.setZosConnectionByIndex(currConnection, this.connectionIdentifier);
            connSingleton.setCurrZosConnection(currConnection, this.connectionIdentifier);

            currSshConnection = new SshConnection(host, currSshConnection.getPort(), username, password);
            configSingleton.setSshConnectionByIndex(currSshConnection, this.connectionIdentifier);
            connSingleton.setCurrSshConnection(currSshConnection);
        }

        final String msg = String.format(
                "Connection defined:%nhost:%s%nuser:%s%nzosmfport:%s%nsshport:%s",
                currConnection.getHost(),
                currConnection.getUser(),
                currConnection.getZosmfPort(),
                currSshConnection.getPort()
        );
        this.terminalOutputService.println(msg);
    }

    private void updateTerminalTitle() {
        LOG.debug("*** updateTerminalTitle ***");

        ConnSingleton connSingleton = ConnSingleton.getInstance();
        SshConnection currSshConnection = connSingleton.getCurrSshConnection();

        if (currSshConnection != null) {
            String hostName = currSshConnection.getHost();
            String title = Constants.APP_TITLE + " - " + hostName.toUpperCase();
            TerminalSingleton.getInstance().getMainTerminal().setPaneTitle(title);
        }
    }

}
