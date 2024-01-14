package zos.shell.service.change;

import org.beryx.textio.TextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.configuration.ConfigSingleton;
import zos.shell.configuration.record.ConfigSettings;
import zos.shell.constants.Constants;
import zowe.client.sdk.core.SshConnection;
import zowe.client.sdk.core.ZosConnection;

import java.util.concurrent.atomic.AtomicInteger;

public class ChangeConnectionService {

    private static final Logger LOG = LoggerFactory.getLogger(ChangeConnectionService.class);

    private final TextTerminal<?> terminal;
    private final ConfigSingleton configSingleton = ConfigSingleton.getInstance();

    public ChangeConnectionService(final TextTerminal<?> terminal) {
        LOG.debug("*** ChangeConnectionService ***");
        this.terminal = terminal;
    }

    public ZosConnection changeZosConnection(final ZosConnection zosConnection, final String[] commands) {
        LOG.debug("*** changeZosConnection ***");
        var index = Integer.parseInt(commands[1]);
        if (index-- > configSingleton.getZosConnections().size()) {
            terminal.println(Constants.NO_CONNECTION);
            return zosConnection;
        }
        final var profile = configSingleton.getProfileByIndex(index);
        ConfigSingleton.getInstance().setConfigSettings(new ConfigSettings(profile.getDownloadpath(),
                profile.getConsolename(), profile.getWindow()));
        ConfigSingleton.getInstance().updateWindowSittings(terminal);
        return configSingleton.getZosConnectionByIndex(index);
    }

    public SshConnection changeSshConnection(final SshConnection sshConnection, final String[] commands) {
        LOG.debug("*** changeSshConnection ***");
        var index = Integer.parseInt(commands[1]);
        if (index-- > configSingleton.getZosConnections().size()) {
            return sshConnection;
        }
        return configSingleton.getSshConnectionByIndex(index);
    }

    public void displayConnections() {
        LOG.debug("*** displayConnections ***");
        final var i = new AtomicInteger(1);
        configSingleton.getZosConnections().forEach(c -> terminal.println(i.getAndIncrement() + " " + "hostname: " +
                c.getHost() + ", port: " + c.getZosmfPort() + ", user: " + c.getUser()));
        if (configSingleton.getZosConnections().isEmpty()) {
            terminal.println(Constants.NO_CONNECTION_INFO);
        }
    }

}