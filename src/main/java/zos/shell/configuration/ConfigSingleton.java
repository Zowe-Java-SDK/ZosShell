package zos.shell.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import zos.shell.configuration.model.Profile;
import zos.shell.configuration.record.ConfigSettings;
import zowe.client.sdk.core.SshConnection;
import zowe.client.sdk.core.ZosConnection;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        final var path = Paths.get("/ZosShell/config.json").toFile();
        this.profiles = Arrays.asList(mapper.readValue(path, Profile[].class));
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

