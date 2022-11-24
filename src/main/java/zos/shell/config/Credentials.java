package zos.shell.config;

import org.apache.commons.lang3.SystemUtils;
import zos.shell.Constants;
import zos.shell.utility.Util;
import zowe.client.sdk.core.SSHConnection;
import zowe.client.sdk.core.ZOSConnection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Credentials {

    public static void readCredentials(List<ZOSConnection> connections, Map<String, SSHConnection> sshConnections) {
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
                int sshPort = 0;
                var zosConnection = new ZOSConnection(items[0], items[1], items[2], items[3]);
                var sshConnection = new SSHConnection(items[0], sshPort, items[2], items[3]);
                // items[4] can represent a mvsconsolename or sshport value, sshport will be a number
                if (items.length == 5 && Util.isStrNum(items[4])) {
                    sshPort = Integer.parseInt(items[4]);
                    sshConnection = new SSHConnection(items[0], sshPort, items[2], items[3]);
                } else if (items.length == 6 && Util.isStrNum(items[5])) { // items[5] is only sshport value
                    sshPort = Integer.parseInt(items[5]);
                    sshConnection = new SSHConnection(items[0], sshPort, items[2], items[3]);
                }
                connections.add(zosConnection);
                sshConnections.put(zosConnection.getHost(), sshConnection);
            }
        } catch (IOException | NullPointerException ignored) {
        }
    }

}
