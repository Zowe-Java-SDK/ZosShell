package zos.shell.singleton.configuration.record;

import zos.shell.singleton.EnvVariableSingleton;
import zos.shell.singleton.configuration.model.Window;

public class ConfigSettings {

    private final String hostName;
    private final String downloadPath;
    private final String consoleName;
    private final String accountNumber;
    private final String browseLimit;
    private final String prompt;
    private final Window window;

    public ConfigSettings(final String hostName, final String downloadPath, final String consoleName,
                          final String accountNumber, final String browseLimit, final String prompt,
                          final Window window) {
        this.hostName = hostName;
        if (hostName != null && !hostName.isBlank()) {
            EnvVariableSingleton.getInstance().getVariables().put("HOSTNAME", hostName.trim());
        }
        this.downloadPath = downloadPath;
        if (downloadPath != null && !downloadPath.isBlank()) {
            EnvVariableSingleton.getInstance().getVariables().put("DOWNLOAD_PATH", downloadPath.trim());
        }
        this.consoleName = consoleName;
        if (consoleName != null && !consoleName.isBlank()) {
            EnvVariableSingleton.getInstance().getVariables().put("CONSOLE_NAME", consoleName.trim());
        }
        this.accountNumber = accountNumber;
        if (accountNumber != null && !accountNumber.isBlank()) {
            EnvVariableSingleton.getInstance().getVariables().put("ACCOUNT_NUMBER", accountNumber.trim());
        }
        this.browseLimit = browseLimit;
        if (browseLimit != null && !browseLimit.isBlank()) {
            EnvVariableSingleton.getInstance().getVariables().put("BROWSE_LIMIT", browseLimit.trim());
        }
        this.prompt = prompt;
        if (prompt != null && !prompt.isBlank()) {
            EnvVariableSingleton.getInstance().getVariables().put("PROMPT", prompt);
        }
        this.window = window;
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
                ", accountNumber='" + accountNumber + '\'' +
                ", browseLimit='" + browseLimit + '\'' +
                ", prompt='" + prompt + '\'' +
                ", window=" + window +
                '}';
    }

}
