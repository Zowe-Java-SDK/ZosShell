package com.credential;

import core.ZOSConnection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class Credentials {

    private static final String PATH_FILE = "C:\\creds.txt";

    public static void readCredentials(List<ZOSConnection> connections) {
        File file = new File(PATH_FILE);
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));

            String str;
            while ((str = br.readLine()) != null) {
                String line[] = str.split(",");
                if (line.length < 4)
                    continue;
                ZOSConnection connection = new ZOSConnection(line[0], line[1], line[2], line[3]);
                connections.add(connection);
            }
        } catch (IOException e) {
        }
    }

}
