package zos.shell.configuration.model;

public class Profile {

    private String hostname;
    private String zosmfport;
    private String sshport;
    private String username;
    private String password;
    private String downloadpath;
    private String consolename;
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
        return zosmfport;
    }

    public void setZosmfport(String zosmfport) {
        this.zosmfport = zosmfport;
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
        return downloadpath;
    }

    public void setDownloadpath(String downloadpath) {
        this.downloadpath = downloadpath;
    }

    public String getConsolename() {
        return consolename;
    }

    public void setConsolename(String consolename) {
        this.consolename = consolename;
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
                ", zosmfport='" + zosmfport + '\'' +
                ", sshport='" + sshport + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", downloadpath='" + downloadpath + '\'' +
                ", consolename='" + consolename + '\'' +
                ", window=" + window +
                '}';
    }

}


