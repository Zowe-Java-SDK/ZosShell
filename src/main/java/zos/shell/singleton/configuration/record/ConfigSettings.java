package zos.shell.singleton.configuration.record;

import zos.shell.singleton.EnvVariableSingleton;
import zos.shell.singleton.configuration.model.Window;

public class ConfigSettings {

    private final String hostName;
    private final String downloadPath;
    private final String consoleName;
    private final String prompt;
    private final Window window;

    public ConfigSettings(final String hostName, final String downloadPath, final String consoleName,
                          final String prompt, final Window window) {
        this.hostName = hostName;
        if (hostName != null && !hostName.isBlank()) {
            EnvVariableSingleton.getInstance().getVariables().put("HOSTNAME", hostName);
        }
        this.downloadPath = downloadPath;
        if (downloadPath != null && !downloadPath.isBlank()) {
            EnvVariableSingleton.getInstance().getVariables().put("DOWNLOAD_PATH", downloadPath);
        }
        this.consoleName = consoleName;
        if (consoleName != null && !consoleName.isBlank()) {
            EnvVariableSingleton.getInstance().getVariables().put("CONSOLE_NAME", consoleName);
        }
        this.prompt = prompt;
        if (prompt != null && !prompt.isBlank()) {
            EnvVariableSingleton.getInstance().getVariables().put("PROMPT", prompt);
        }
        this.window = window;
    }

    public String getHostName() {
        return hostName;
    }

    public String getDownloadPath() {
        return downloadPath;
    }

    public String getConsoleName() {
        return consoleName;
    }

    public String getPrompt() {
        return prompt;
    }

    public Window getWindow() {
        return window;
    }

    @Override
    public String toString() {
        return "ConfigSettings{" +
                "hostName='" + hostName + '\'' +
                ", downloadPath='" + downloadPath + '\'' +
                ", consoleName='" + consoleName + '\'' +
                ", prompt='" + prompt + '\'' +
                ", window=" + window +
                '}';
    }

}
