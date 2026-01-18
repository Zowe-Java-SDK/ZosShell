package zos.shell.service.usermod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.singleton.ConnSingleton;
import zos.shell.singleton.configuration.ConfigSingleton;
import zos.shell.utility.PromptUtil;
import zowe.client.sdk.core.SshConnection;
import zowe.client.sdk.core.ZosConnection;
import zowe.client.sdk.core.ZosConnectionFactory;

public class UsermodService {

    private static final Logger LOG = LoggerFactory.getLogger(UsermodService.class);

    private final String host;
    private final int zosmfPort;
    private final String username;
    private final String password;
    private final int sshPort;

    private final int index;

    public UsermodService(final ZosConnection connection, final int index) {
        LOG.debug("*** UsermodService ***");
        this.host = connection.getHost();
        this.zosmfPort = connection.getZosmfPort();
        this.username = connection.getUser();
        this.password = connection.getPassword();
        this.sshPort = ConfigSingleton.getInstance().getSshConnectionByIndex(index).getPort();
        this.index = index;
    }

    public void changePassword() {
        LOG.debug("*** changePassword ***");
        String confirmPassword = null;
        var password = "";
        while (confirmPassword == null || !confirmPassword.equals(password)) {
            password = PromptUtil.getPromptInfo("password:", true);
            confirmPassword = PromptUtil.getPromptInfo("confirm password:", true);
        }
        var zosConnection = ZosConnectionFactory.createBasicConnection(this.host, this.zosmfPort, this.username, password);
        var sshConnection = new SshConnection(this.host, this.sshPort, this.username, password);
        ConfigSingleton.getInstance().setZosConnectionByIndex(zosConnection, index);
        ConnSingleton.getInstance().setCurrZosConnection(zosConnection, index);
        ConfigSingleton.getInstance().setSshConnectionByIndex(sshConnection, index);
        ConnSingleton.getInstance().setCurrSshConnection(sshConnection);
    }

    public void changeUsername() {
        LOG.debug("*** changeUsername ***");
        var username = PromptUtil.getPromptInfo("username:", false);
        var zosConnection = ZosConnectionFactory.createBasicConnection(this.host, this.zosmfPort, username, this.password);
        var sshConnection = new SshConnection(this.host, this.sshPort, username, this.password);
        ConfigSingleton.getInstance().setZosConnectionByIndex(zosConnection, index);
        ConnSingleton.getInstance().setCurrZosConnection(zosConnection, index);
        ConfigSingleton.getInstance().setSshConnectionByIndex(sshConnection, index);
        ConnSingleton.getInstance().setCurrSshConnection(sshConnection);
    }

}
