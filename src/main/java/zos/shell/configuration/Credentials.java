package zos.shell.configuration;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.constants.Constants;
import zos.shell.utility.StrUtil;
import zowe.client.sdk.core.SshConnection;
import zowe.client.sdk.core.ZosConnection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Credentials {

    private static final Logger LOG = LoggerFactory.getLogger(Credentials.class);

    public static void readCredentials(List<ZosConnection> connections, Map<String, SshConnection> SshConnections) {
        LOG.debug("*** readCredentials ***");
        File file = null;
        if (SystemUtils.IS_OS_WINDOWS) {
            file = new File(Constants.SECURITY_CONFIG_PATH_FILE_WINDOWS);
        } else if (SystemUtils.IS_OS_MAC_OSX) {
            file = new File(Constants.SECURITY_CONFIG_PATH_FILE_MAC);
        }
        try (final var br = new BufferedReader(new FileReader(Objects.requireNonNull(file)))) {
            String str;
            while ((str = br.readLine()) != null) {
                final var items = str.split(",");
                if (items.length < 4) {
                    continue;
                }
                var sshPort = 0;
                var ZosConnection = new ZosConnection(items[0], items[1], items[2], items[3]);
                var SshConnection = new SshConnection(items[0], sshPort, items[2], items[3]);
                // items[4] can represent a mvsconsolename or sshport value, sshport will be a number
                if (items.length == 5 && StrUtil.isStrNum(items[4])) {
                    sshPort = Integer.parseInt(items[4]);
                    SshConnection = new SshConnection(items[0], sshPort, items[2], items[3]);
                } else if (items.length == 6 && StrUtil.isStrNum(items[5])) { // items[5] is only sshport value
                    sshPort = Integer.parseInt(items[5]);
                    SshConnection = new SshConnection(items[0], sshPort, items[2], items[3]);
                }
                connections.add(ZosConnection);
                SshConnections.put(ZosConnection.getHost(), SshConnection);
            }
        } catch (IOException | NullPointerException ignored) {
        }
    }

}
