package com.config;

import com.Constants;
import zowe.client.sdk.core.ZOSConnection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class Credentials {

    public static void readCredentials(List<ZOSConnection> connections) {
        var file = new File(Constants.SECURITY_CONFIG_PATH_FILE);
        try (var br = new BufferedReader(new FileReader(file))) {
            String str;
            while ((str = br.readLine()) != null) {
                var items = str.split(",");
                if (items.length < 4) {
                    continue;
                }
                var connection = new ZOSConnection(items[0], items[1], items[2], items[3]);
                connections.add(connection);
            }
        } catch (IOException ignored) {
        }
    }

}
