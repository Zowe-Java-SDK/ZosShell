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
import zowe.client.sdk.core.ZosConnectionFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class ConfigSingleton {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigSingleton.class);
    private static final int WINDOW_UPDATE_DELAY_MS = 500;
    private static final String DEFAULT_EMPTY_STRING = "";
    private static final int PANE_WIDTH_DEFAULT_VALUE = 640;
    private static final int PANE_HEIGHT_DEFAULT_VALUE = 480;
    private List<Profile> profiles;
    private final ObjectMapper mapper = new ObjectMapper();
    private final LinkedHashSet<ZosConnection> zosConnections = new LinkedHashSet<>();
    private final LinkedHashSet<SshConnection> sshConnections = new LinkedHashSet<>();
    private ConfigSettings configSettings;
    private ChangeWinService changeWinService;

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
                profile.getAccountnumber(),
                profile.getBrowselimit(),
                profile.getPrompt(),
                profile.getWindow());
    }

    private void createZosConnections() {
        LOG.debug("*** createZosConnections ***");
        profiles.forEach(profile -> zosConnections.add(
                ZosConnectionFactory.createBasicConnection(profile.getHostname(),
                        Integer.parseInt(profile.getZosmfport()),
                        profile.getUsername() != null && !profile.getUsername().isBlank() ?
                                profile.getUsername() : Constants.DEFAULT_EMPTY_USER_PASSWORD_VALUE,
                        profile.getPassword() != null && !profile.getPassword().isBlank() ?
                                profile.getPassword() : Constants.DEFAULT_EMPTY_USER_PASSWORD_VALUE)));
    }

    private void createSshConnections() {
        LOG.debug("*** createSshConnections ***");
        profiles.forEach(profile -> {
            int sshport = 1;
            try {
                sshport = Integer.parseInt(profile.getSshport());
            } catch (NumberFormatException ignored) {
            }
            sshConnections.add(new SshConnection(profile.getHostname(),
                    sshport, profile.getUsername() != null ? profile.getUsername() : DEFAULT_EMPTY_STRING,
                    profile.getPassword() != null ? profile.getPassword() : DEFAULT_EMPTY_STRING));
        });
    }

    public void updateWindowSettings(final TextTerminal<?> terminal) {
        LOG.debug("*** updateWindowSettings ***");
        var str = new StringBuilder();
        if (changeWinService == null) {
            changeWinService = new ChangeWinService(TerminalSingleton.getInstance().getTerminal());
        }
        if (this.configSettings == null) {
            return;
        }
        var window = configSettings.getWindow();
        String result;
        result = changeWinService.setTextColor(window != null && window.getTextcolor() != null ?
                configSettings.getWindow().getTextcolor() : Constants.DEFAULT_TEXT_COLOR);
        str.append(result != null ? result + "\n" : DEFAULT_EMPTY_STRING);
        result = changeWinService.setBackGroundColor(window != null && window.getBackgroundcolor() != null ?
                configSettings.getWindow().getBackgroundcolor() : Constants.DEFAULT_BACKGROUND_COLOR);
        str.append(result != null ? result + "\n" : DEFAULT_EMPTY_STRING);
        result = changeWinService.setBold(window != null &&
                "true".equalsIgnoreCase(configSettings.getWindow().getFontbold()));
        str.append(result != null ? result + "\n" : DEFAULT_EMPTY_STRING);
        result = changeWinService.setFontSize(window != null && window.getFontsize() != null ?
                configSettings.getWindow().getFontsize() : String.valueOf(Constants.DEFAULT_FONT_SIZE));
        str.append(result != null ? result : DEFAULT_EMPTY_STRING);

        if (window != null) {
            var paneWidth = window.getPaneWidth();
            var paneHeight = window.getPaneHeight();
            if (paneWidth != null || paneHeight != null) {
                delayWindowUpdate();
            }
            if (paneWidth != null) {
                try {
                    if (Integer.parseInt(paneWidth) < PANE_WIDTH_DEFAULT_VALUE) {
                        paneWidth = String.valueOf(PANE_WIDTH_DEFAULT_VALUE);
                    }
                    result = changeWinService.setPaneWidth(paneWidth);
                    str.append(result != null ? "\n" + result : DEFAULT_EMPTY_STRING);
                } catch (NumberFormatException ignored) {
                }
            }
            if (paneHeight != null) {
                try {
                    if (Integer.parseInt(paneHeight) < PANE_HEIGHT_DEFAULT_VALUE) {
                        paneHeight = String.valueOf(PANE_HEIGHT_DEFAULT_VALUE);
                    }
                    result = changeWinService.setPaneHeight(paneHeight);
                    str.append(result != null ? "\n" + result : DEFAULT_EMPTY_STRING);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        terminal.println(str.toString());
    }

    private void delayWindowUpdate() {
        try {
            Thread.sleep(WINDOW_UPDATE_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.warn("Window update delay interrupted", e);
        }
    }

    public List<ZosConnection> getZosConnections() {
        LOG.debug("*** getZosConnections ***");
        return new ArrayList<>(this.zosConnections);
    }

    public Profile getProfileByIndex(int index) {
        LOG.debug("*** getProfileByIndex ***");
        return this.profiles.get(index);
    }

    public SshConnection getSshConnectionByIndex(int index) {
        LOG.debug("*** getSshConnectionByIndex ***");
        if (sshConnections.isEmpty()) {
            return null;
        }
        return new ArrayList<>(this.sshConnections).get(index);
    }

    public void setSshConnectionByIndex(final SshConnection sshConnection, final int index) {
        LOG.debug("*** setSshConnectionByIndex ***");
        List<SshConnection> lst = new ArrayList<>(this.sshConnections);
        lst.set(index, sshConnection);
        this.sshConnections.clear();
        this.sshConnections.addAll(lst);
    }

    public ZosConnection getZosConnectionByIndex(int index) {
        LOG.debug("*** getZosConnectionByIndex ***");
        if (zosConnections.isEmpty()) {
            return null;
        }
        return new ArrayList<>(this.zosConnections).get(index);
    }

    public void setZosConnectionByIndex(final ZosConnection zosConnection, final int index) {
        LOG.debug("*** setZosConnectionByIndex ***");
        List<ZosConnection> lst = new ArrayList<>(this.zosConnections);
        lst.set(index, zosConnection);
        this.zosConnections.clear();
        this.zosConnections.addAll(lst);
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