package com.config;

import com.Constants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MvsConsoles {

    private Map<String, String> consoles = new HashMap<>();

    public MvsConsoles() {
        setup();
    }

    public void setup() {
        var file = new File(Constants.SECURITY_CONFIG_PATH_FILE);
        try (var br = new BufferedReader(new FileReader(file))) {
            String str;
            while ((str = br.readLine()) != null) {
                var items = str.split(",");
                if (items.length == 5) {
                    consoles.put(items[0], items[4]);
                }
            }
        } catch (IOException ignored) {
        }
    }

    public String getConsoleName(String connection) {
        return consoles.get(connection);
    }

}
