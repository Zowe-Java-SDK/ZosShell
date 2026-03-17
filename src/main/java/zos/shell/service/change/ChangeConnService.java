package zos.shell.service.change;

import org.beryx.textio.TextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.service.terminal.TerminalOutputService;
import zos.shell.singleton.configuration.ConfigSingleton;
import zos.shell.singleton.configuration.record.ConfigSettings;
import zos.shell.utility.PromptUtil;
import zowe.client.sdk.core.SshConnection;
import zowe.client.sdk.core.ZosConnection;
import zowe.client.sdk.core.ZosConnectionFactory;

import java.util.Objects;

public class ChangeConnService {

    private static final Logger LOG = LoggerFactory.getLogger(ChangeConnService.class);

    private final TextTerminal<?> terminal;
    private final TerminalOutputService terminalOutputService;
    private final ConfigSingleton configSingleton = ConfigSingleton.getInstance();

    public ChangeConnService(final TextTerminal<?> terminal) {
        LOG.debug("*** ChangeConnService ***");
        this.terminal = terminal;
        this.terminalOutputService = new TerminalOutputService(terminal);
    }

    public ZosConnection changeZosConnection(final ZosConnection zosConnection, final int index) {
        LOG.debug("*** changeZosConnection ***");
        int connectionCount = this.configSingleton.getZosConnections().size();
        if (index < 0 || index > connectionCount) {
            this.terminalOutputService.println(Constants.NO_CONNECTION);
            return zosConnection;
        }

        var profile = this.configSingleton.getProfileByIndex(index);
        this.configSingleton.setConfigSettings(
                new ConfigSettings(
                        profile.getHostname(),
                        profile.getDownloadpath(),
                        profile.getConsolename(),
                        profile.getAccountnumber(),
                        profile.getBrowselimit(),
                        profile.getPrompt(),
                        profile.getWindow()
                ));

        ZosConnection selectedZosConnection = Objects.requireNonNull(
                this.configSingleton.getZosConnectionByIndex(index),
                "Connection configuration missing"
        );
        var host = selectedZosConnection.getHost();
        var username = selectedZosConnection.getUser();
        var password = selectedZosConnection.getPassword();
        var zosmfPort = selectedZosConnection.getZosmfPort();

        if (host.isBlank() || zosmfPort == 0) {
            this.terminalOutputService.println("Error: Hostname or z/OSMF port value(s) missing");
            this.terminalOutputService.println("Check configuration file and try again...");
            return zosConnection;
        }

        boolean hasCredentials = !(username.equals(Constants.DEFAULT_EMPTY_USER_PASSWORD_VALUE)
                || password.equals(Constants.DEFAULT_EMPTY_USER_PASSWORD_VALUE));

        if (hasCredentials) {
            this.configSingleton.updateWindowSettings(this.terminal);
            return selectedZosConnection;
        }

        this.terminalOutputService.println("Enter username and password for host " + host);
        username = PromptUtil.getPromptInfo("username:", false);

        String newPassword;
        String confirmPassword;
        do {
            newPassword = PromptUtil.getPromptInfo("password:", true);
            confirmPassword = PromptUtil.getPromptInfo("confirm password:", true);
        } while (!newPassword.equals(confirmPassword));

        this.configSingleton.updateWindowSettings(this.terminal);
        ZosConnection newZosConnection = ZosConnectionFactory.createBasicConnection(
                host,
                zosmfPort,
                username,
                newPassword
        );
        this.configSingleton.setZosConnectionByIndex(newZosConnection, index);
        return newZosConnection;
    }

    public SshConnection changeSshConnection(final SshConnection sshConnection, final int index) {
        LOG.debug("*** changeSshConnection ***");
        var connectionCount = this.configSingleton.getZosConnections().size();
        if (index < 0 || index > connectionCount) {
            return sshConnection;
        }

        ZosConnection selectedZosConnection = Objects.requireNonNull(
                this.configSingleton.getZosConnectionByIndex(index),
                "ZosConnection configuration missing"
        );
        var zosUsername = selectedZosConnection.getUser();
        var zosPassword = selectedZosConnection.getPassword();

        SshConnection selectedSshConnection = Objects.requireNonNull(
                this.configSingleton.getSshConnectionByIndex(index),
                "SshConnection configuration missing"
        );
        var sshHost = selectedSshConnection.getHost();
        var sshPort = selectedSshConnection.getPort();

        SshConnection newSshConnection = new SshConnection(sshHost, sshPort, zosUsername, zosPassword);
        this.configSingleton.setSshConnectionByIndex(newSshConnection, index);
        return newSshConnection;
    }

    public void displayConnections() {
        LOG.debug("*** displayConnections ***");

        if (this.configSingleton.getZosConnections().isEmpty()) {
            this.terminalOutputService.println(Constants.NO_CONNECTION_INFO);
            return;
        }

        for (int i = 0; i < this.configSingleton.getZosConnections().size(); i++) {
            ZosConnection connection = Objects.requireNonNull(
                    this.configSingleton.getZosConnectionByIndex(i),
                    "Connection configuration missing"
            );
            String host = connection.getHost().isBlank() ? "n/a" : connection.getHost();
            this.terminalOutputService.println((i + 1) + " hostname: " + host + ", zosmfport: " + connection.getZosmfPort());
        }
    }

}
