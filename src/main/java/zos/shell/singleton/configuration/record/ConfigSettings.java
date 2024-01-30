package zos.shell.singleton.configuration.record;

import zos.shell.singleton.configuration.model.Window;

public class ConfigSettings {

    private final String downloadPath;
    private final String consoleName;
    private final Window window;

    public ConfigSettings(final String downloadPath, final String consoleName, final Window window) {
        this.downloadPath = downloadPath;
        this.consoleName = consoleName;
        this.window = window;
    }

    public String getDownloadPath() {
        return downloadPath;
    }

    public String getConsoleName() {
        return consoleName;
    }

    public Window getWindow() {
        return window;
    }

    @Override
    public String toString() {
        return "ConfigSettings{" +
                "downloadPath='" + downloadPath + '\'' +
                ", consoleName='" + consoleName + '\'' +
                ", window=" + window +
                '}';
    }

}
