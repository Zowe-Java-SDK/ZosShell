package zos.shell.singleton.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.SystemUtils;
import org.beryx.textio.TextTerminal;
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
    }

    public static ConfigSingleton getInstance() {
        return ConfigSingleton.Holder.instance;
    }

    public void readConfig() throws IOException {
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
        if (profiles.isEmpty()) {
            configSettings = null;
            return;
        }
        var profile = this.getProfileByIndex(0);
        configSettings = new ConfigSettings(profile.getDownloadpath(), profile.getConsolename(), profile.getWindow());
    }

    private void createZosConnections() {
        profiles.forEach(profile -> zosConnections.add(new ZosConnection(profile.getHostname(),
                profile.getZosmfport(), profile.getUsername(), profile.getPassword())));
    }

    private void createSshConnections() {
        profiles.forEach(profile -> shhConnections.add(new SshConnection(profile.getHostname(),
                Integer.parseInt(profile.getSshport()), profile.getUsername(), profile.getPassword())));
    }

    public void updateWindowSittings(final TextTerminal<?> terminal) {
        var str = new StringBuilder();
        if (windowCmd == null) {
            windowCmd = new ChangeWinService(TerminalSingleton.getInstance().getTerminal());
        }
        var configSettings = ConfigSingleton.getInstance().getConfigSettings();
        if (configSettings == null) {
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
        return zosConnections;
    }

    public Profile getProfileByIndex(int index) {
        return this.profiles.get(index);
    }

    public SshConnection getSshConnectionByIndex(int index) {
        if (shhConnections.isEmpty()) {
            return null;
        }
        return shhConnections.get(index);
    }

    public ZosConnection getZosConnectionByIndex(int index) {
        if (zosConnections.isEmpty()) {
            return null;
        }
        return zosConnections.get(index);
    }

    public ConfigSettings getConfigSettings() {
        return configSettings;
    }

    public void setConfigSettings(final ConfigSettings configSettings) {
        this.configSettings = configSettings;
    }

}