package zos.shell.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.SystemUtils;
import zos.shell.configuration.model.Profile;
import zos.shell.configuration.record.ConfigSettings;
import zos.shell.constants.Constants;
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
            file = Paths.get(!path.isBlank() ? path : "C:\\ZosShell\\config.json").toFile();
        } else if (SystemUtils.IS_OS_MAC_OSX) {
            file = Paths.get(!path.isBlank() ? path : "/ZosShell/config.json").toFile();
        } else {
            throw new RuntimeException(Constants.OS_ERROR);
        }
        this.profiles = Arrays.asList(mapper.readValue(file, Profile[].class));
        this.createZosConnections();
        this.createSshConnections();
        this.createCurrConfigSettings();
    }

    private void createCurrConfigSettings() {
        final var profile = this.getProfileByIndex(0);
        configSettings = new ConfigSettings(profile.getDownloadpath(), profile.getConsolename(), profile.getWindow());
    }

    public void createZosConnections() {
        profiles.forEach(profile -> zosConnections.add(new ZosConnection(profile.getHostname(),
                profile.getZosmfport(), profile.getUsername(), profile.getPassword())));
    }

    public void createSshConnections() {
        profiles.forEach(profile -> shhConnections.add(new SshConnection(profile.getHostname(),
                Integer.parseInt(profile.getSshport()), profile.getUsername(), profile.getPassword())));
    }

    public List<ZosConnection> getZosConnections() {
        return zosConnections;
    }

    public Profile getProfileByIndex(final int index) {
        return this.profiles.get(index);
    }

    public SshConnection getSshConnectionByIndex(final int index) {
        return shhConnections.get(index);
    }

    public ZosConnection getZosConnectionByIndex(final int index) {
        return zosConnections.get(index);
    }

    public ConfigSettings getConfigSettings() {
        return configSettings;
    }

    public void setConfigSettings(final ConfigSettings configSettings) {
        this.configSettings = configSettings;
    }

}

