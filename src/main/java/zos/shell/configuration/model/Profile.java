package zos.shell.configuration.model;

public class Profile {

    private String hostname;
    private String zosmfPort;
    private String sshport;
    private String username;
    private String password;
    private String downloadPath;
    private String consoleName;
    private Window window;

    public Profile() {
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getZosmfport() {
        return zosmfPort;
    }

    public void setZosmfport(String zosmfPort) {
        this.zosmfPort = zosmfPort;
    }

    public String getSshport() {
        return sshport;
    }

    public void setSshport(String sshport) {
        this.sshport = sshport;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDownloadpath() {
        return downloadPath;
    }

    public void setDownloadpath(String downloadPath) {
        this.downloadPath = downloadPath;
    }

    public String getConsolename() {
        return consoleName;
    }

    public void setConsolename(String consolename) {
        this.consoleName = consolename;
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
                "hostname='" + hostname + '\'' +
                ", zosmfPort='" + zosmfPort + '\'' +
                ", sshport='" + sshport + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", downloadPath='" + downloadPath + '\'' +
                ", consoleName='" + consoleName + '\'' +
                ", window=" + window +
                '}';
    }

}


