package zos.shell.singleton.configuration.model;

@SuppressWarnings("unused")
public class Profile {

    private String name;
    private String hostname;
    private String zosmfPort;
    private String sshport;
    private String username;
    private String password;
    private String downloadPath;
    private String consoleName;
    private String accountNumber;
    private String browseLimit;
    private String prompt;
    private Window window;

    public Profile() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(final String hostname) {
        this.hostname = hostname;
    }

    public String getZosmfport() {
        return zosmfPort;
    }

    public void setZosmfport(final String zosmfPort) {
        this.zosmfPort = zosmfPort;
    }

    public String getSshport() {
        return sshport;
    }

    public void setSshport(final String sshport) {
        this.sshport = sshport;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getDownloadpath() {
        return downloadPath;
    }

    public void setDownloadpath(final String downloadPath) {
        this.downloadPath = downloadPath;
    }

    public String getConsolename() {
        return consoleName;
    }

    public void setConsolename(final String consolename) {
        this.consoleName = consolename;
    }

    public String getAccountnumber() {
        return accountNumber;
    }

    public void setAccountnumber(final String accountnumber) {
        this.accountNumber = accountnumber;
    }

    public String getBrowselimit() {
        return browseLimit;
    }

    public void setBrowselimit(String browselimit) {
        this.browseLimit = browselimit;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public Window getWindow() {
        return window;
    }

    public void setWindow(final Window window) {
        this.window = window;
    }

    @Override
    public String toString() {
        return "Profile{" +
                "name='" + name + '\'' +
                ", hostname='" + hostname + '\'' +
                ", zosmfPort='" + zosmfPort + '\'' +
                ", sshport='" + sshport + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", downloadPath='" + downloadPath + '\'' +
                ", consoleName='" + consoleName + '\'' +
                ", accountNumber='" + accountNumber + '\'' +
                ", browseLimit='" + browseLimit + '\'' +
                ", prompt='" + prompt + '\'' +
                ", window=" + window +
                '}';
    }

}


