package com.commands;

import com.Constants;
import org.beryx.textio.TextTerminal;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LocalFiles {

    public static void listFiles(TextTerminal<?> terminal) {
        List<String> files = getFiles(Constants.PATH_FILE_DIRECTORY);
        if (files.isEmpty()) {
            terminal.println(Constants.NO_FILES);
        }
        files.forEach(terminal::println);
    }

    private static List<String> getFiles(String dir) {
        return Stream.of(new File(dir).listFiles())
                .filter(file -> !file.isDirectory())
                .filter(file -> !file.getName().equalsIgnoreCase("credentials.txt"))
                .map(File::getName)
                .sorted()
                .collect(Collectors.toList());
    }

}
