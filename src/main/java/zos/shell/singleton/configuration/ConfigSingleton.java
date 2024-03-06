package zos.shell.singleton.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.SystemUtils;
import org.beryx.textio.TextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.service.change.ChangeWinService;
import zos.shell.singleton.TerminalSingleton;
import zos.shell.singleton.configuration.model.Profile;
import zos.shell.singleton.configuration.record.ConfigSettings;
import zowe.client.sdk.core.SshConnection;
import zowe.client.sdk.core.ZosConnection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ConfigSingleton {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigSingleton.class);

    private List<Profile> profiles;
    private final ObjectMapper mapper = new ObjectMapper();
    private final List<ZosConnection> zosConnections = new ArrayList<>();
    private final List<SshConnection> shhConnections = new ArrayList<>();
    private ConfigSettings configSettings;
    private ChangeWinService windowCmd;

    private static class Holder {
        private static final ConfigSingleton instance = new ConfigSingleton();
    }

    private ConfigSingleton() {
        LOG.debug("*** ConfigSingleton ***");
    }

    public static ConfigSingleton getInstance() {
        LOG.debug("*** getInstance ***");
        return ConfigSingleton.Holder.instance;
    }

    public void readConfig() throws IOException {
        LOG.debug("*** readConfig ***");
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Map<String, String> env = System.getenv();
        String path = "";
        for (String envName : env.keySet()) {
            if ("ZOSSHELL_CONFIG_PATH".equalsIgnoreCase(envName)) {
                path = env.get("ZOSSHELL_CONFIG_PATH");
            }
        }
        File file;
        if (SystemUtils.IS_OS_WINDOWS) {
            file = Paths.get(!path.isBlank() ? path : Constants.DEFAULT_CONFIG_WINDOWS).toFile();
        } else if (SystemUtils.IS_OS_MAC_OSX) {
            file = Paths.get(!path.isBlank() ? path : Constants.DEFAULT_CONFIG_MAC).toFile();
        } else {
            throw new RuntimeException(Constants.OS_ERROR);
        }
        try {
            this.profiles = Arrays.asList(mapper.readValue(file, Profile[].class));
        } catch (IOException e) {
            if (SystemUtils.IS_OS_WINDOWS) {
                file = Paths.get(Constants.DEFAULT_CONFIG_WINDOWS).toFile();
            } else {
                file = Paths.get(Constants.DEFAULT_CONFIG_MAC).toFile();
            }
            this.profiles = Arrays.asList(mapper.readValue(file, Profile[].class));
        }
        this.createZosConnections();
        this.createSshConnections();
        this.initialConfigSettings();
    }

    private void initialConfigSettings() {
        LOG.debug("*** initialConfigSettings ***");
        if (profiles.isEmpty()) {
            configSettings = null;
            return;
        }
        var profile = this.getProfileByIndex(0);
        configSettings = new ConfigSettings(
                profile.getHostname(),
                profile.getDownloadpath(),
                profile.getConsolename(),
                profile.getPrompt(),
                profile.getWindow());
    }

    private void createZosConnections() {
        LOG.debug("*** createZosConnections ***");
        profiles.forEach(profile -> zosConnections.add(new ZosConnection(profile.getHostname(),
                profile.getZosmfport() != null ? profile.getZosmfport() : "0",
                profile.getUsername() != null ? profile.getUsername() : "",
                profile.getPassword() != null ? profile.getPassword() : "")));
    }

    private void createSshConnections() {
        LOG.debug("*** createSshConnections ***");
        profiles.forEach(profile -> {
            int sshport = 0;
            try {
                sshport = Integer.parseInt(profile.getSshport());
            } catch (NumberFormatException ignored) {
            }
            shhConnections.add(new SshConnection(profile.getHostname(),
                    sshport, profile.getUsername() != null ? profile.getUsername() : "",
                    profile.getPassword() != null ? profile.getPassword() : ""));
        });
    }

    public void updateWindowSittings(final TextTerminal<?> terminal) {
        LOG.debug("*** updateWindowSittings ***");
        var str = new StringBuilder();
        if (windowCmd == null) {
            windowCmd = new ChangeWinService(TerminalSingleton.getInstance().getTerminal());
        }
        if (this.configSettings == null) {
            return;
        }
        var window = configSettings.getWindow();
        String result;
        result = windowCmd.setTextColor(window != null && window.getTextcolor() != null ?
                configSettings.getWindow().getTextcolor() : Constants.DEFAULT_TEXT_COLOR);
        str.append(result != null ? result + "\n" : "");
        result = windowCmd.setBackGroundColor(window != null && window.getBackgroundcolor() != null ?
                configSettings.getWindow().getBackgroundcolor() : Constants.DEFAULT_BACKGROUND_COLOR);
        str.append(result != null ? result + "\n" : "");
        result = windowCmd.setBold(window != null && "true".equalsIgnoreCase(configSettings.getWindow().getFontbold()));
        str.append(result != null ? result + "\n" : "");
        result = windowCmd.setFontSize(window != null && window.getFontsize() != null ?
                configSettings.getWindow().getFontsize() : String.valueOf(Constants.DEFAULT_FONT_SIZE));
        str.append(result != null ? result : "");
        terminal.println(str.toString());
    }

    public List<ZosConnection> getZosConnections() {
        LOG.debug("*** getZosConnections ***");
        return zosConnections;
    }

    public Profile getProfileByIndex(int index) {
        LOG.debug("*** getProfileByIndex ***");
        return this.profiles.get(index);
    }

    public SshConnection getSshConnectionByIndex(int index) {
        LOG.debug("*** getSshConnectionByIndex ***");
        if (shhConnections.isEmpty()) {
            return null;
        }
        return shhConnections.get(index);
    }

    public void setSshConnectionByIndex(final SshConnection sshConnection, final int index) {
        this.shhConnections.set(index, sshConnection);
    }

    public ZosConnection getZosConnectionByIndex(int index) {
        LOG.debug("*** getZosConnectionByIndex ***");
        if (zosConnections.isEmpty()) {
            return null;
        }
        return zosConnections.get(index);
    }

    public void setZosConnectionByIndex(final ZosConnection zosConnection, final int index) {
        this.zosConnections.set(index, zosConnection);
    }

    public ConfigSettings getConfigSettings() {
        LOG.debug("*** getConfigSettings ***");
        return configSettings;
    }

    public void setConfigSettings(final ConfigSettings configSettings) {
        LOG.debug("*** setConfigSettings ***");
        this.configSettings = configSettings;
    }

}
