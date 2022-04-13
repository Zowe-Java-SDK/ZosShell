package com.config;

import com.Constants;
import org.apache.commons.lang3.SystemUtils;
import zowe.client.sdk.core.ZOSConnection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class Credentials {

    public static void readCredentials(List<ZOSConnection> connections) {
        File file = null;
        if (SystemUtils.IS_OS_WINDOWS) {
            file = new File(Constants.SECURITY_CONFIG_PATH_FILE_WINDOWS);
        } else if (SystemUtils.IS_OS_MAC_OSX) {
            file = new File(Constants.SECURITY_CONFIG_PATH_FILE_MAC);
        }
        try (var br = new BufferedReader(new FileReader(Objects.requireNonNull(file)))) {
            String str;
            while ((str = br.readLine()) != null) {
                var items = str.split(",");
                if (items.length < 4) {
                    continue;
                }
                var connection = new ZOSConnection(items[0], items[1], items[2], items[3]);
                connections.add(connection);
            }
        } catch (IOException | NullPointerException ignored) {
        }
    }

}
