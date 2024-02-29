package zos.shell.service.change;

import org.beryx.textio.TextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.singleton.configuration.ConfigSingleton;
import zos.shell.singleton.configuration.record.ConfigSettings;
import zos.shell.utility.PromptUtil;
import zowe.client.sdk.core.SshConnection;
import zowe.client.sdk.core.ZosConnection;

import java.util.concurrent.atomic.AtomicInteger;

public class ChangeConnService {

    private static final Logger LOG = LoggerFactory.getLogger(ChangeConnService.class);

    private final TextTerminal<?> terminal;
    private final ConfigSingleton configSingleton = ConfigSingleton.getInstance();

    public ChangeConnService(final TextTerminal<?> terminal) {
        LOG.debug("*** ChangeConnService ***");
        this.terminal = terminal;
    }

    public ZosConnection changeZosConnection(final ZosConnection zosConnection, final String[] commands) {
        LOG.debug("*** changeZosConnection ***");
        var index = Integer.parseInt(commands[1]);
        if (index-- > configSingleton.getZosConnections().size()) {
            terminal.println(Constants.NO_CONNECTION);
            return zosConnection;
        }
        var profile = configSingleton.getProfileByIndex(index);
        ConfigSingleton.getInstance().setConfigSettings(new ConfigSettings(profile.getDownloadpath(),
                profile.getConsolename(), profile.getWindow()));

        var zosConnectionByIndex = configSingleton.getZosConnectionByIndex(index);
        var host = zosConnectionByIndex.getHost();
        var username = zosConnectionByIndex.getUser();
        var password = zosConnectionByIndex.getPassword();
        var zosmfport = zosConnectionByIndex.getZosmfPort();

        if (host.isBlank() || zosmfport.isBlank()) {
            terminal.println("Error: Hostname or z/OSMF port value(s) missing");
            terminal.println("Check configuration file and try again...");
            return zosConnection;
        }

        if (!username.isBlank() && !password.isBlank()) {
            ConfigSingleton.getInstance().updateWindowSittings(terminal);
            return zosConnectionByIndex;
        }

        terminal.println("Enter username and password for host " + host);
        username = PromptUtil.getPromptInfo("username:", false);
        password = PromptUtil.getPromptInfo("password:", true);
        ConfigSingleton.getInstance().updateWindowSittings(terminal);
        configSingleton.setZosConnectionByIndex(new ZosConnection(host, zosmfport, username, password), index);
        return configSingleton.getZosConnectionByIndex(index);
    }

    public SshConnection changeSshConnection(final SshConnection sshConnection, final String[] commands) {
        LOG.debug("*** changeSshConnection ***");
        var index = Integer.parseInt(commands[1]);
        if (index-- > configSingleton.getZosConnections().size()) {
            return sshConnection;
        }

        var zosConnectionByIndex = configSingleton.getZosConnectionByIndex(index);
        var zosUsername = zosConnectionByIndex.getUser();
        var zosPassword = zosConnectionByIndex.getPassword();

        var sshConnectionByIndex = configSingleton.getSshConnectionByIndex(index);
        var sshHost = sshConnectionByIndex.getHost();
        var sshPort = sshConnectionByIndex.getPort();

        configSingleton.setSshConnectionByIndex(new SshConnection(sshHost, sshPort, zosUsername, zosPassword), index);
        return configSingleton.getSshConnectionByIndex(index);
    }

    public void displayConnections() {
        LOG.debug("*** displayConnections ***");
        var i = new AtomicInteger(1);
        configSingleton.getZosConnections().forEach(
                c -> terminal.println(i.getAndIncrement() + " " + "hostname: " +
                        (c.getHost().isBlank() ? "n\\a" : c.getHost()) + ", zosmfport: " +
                        (c.getZosmfPort().isBlank() ? "n\\a" : c.getZosmfPort())));
        if (configSingleton.getZosConnections().isEmpty()) {
            terminal.println(Constants.NO_CONNECTION_INFO);
        }
    }

}