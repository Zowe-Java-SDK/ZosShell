package com.commands;

import com.Constants;
import org.apache.commons.lang3.SystemUtils;
import org.beryx.textio.TextTerminal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LocalFiles {

    public static void listFiles(TextTerminal<?> terminal) {
        var files = getFiles();
        if (files.isEmpty()) {
            terminal.println(Constants.NO_FILES);
            return;
        }
        terminal.println(Constants.PATH_FILE_DIRECTORY_WINDOWS + ":");
        files.forEach(terminal::println);
    }

    private static List<String> getFiles() {
        if (!SystemUtils.IS_OS_WINDOWS)
            return new ArrayList<>();
        var files = Optional.ofNullable(new File(Constants.PATH_FILE_DIRECTORY_WINDOWS).listFiles());
        return Stream.of(files.orElse(new File[]{}))
                .filter(file -> !file.isDirectory())
                .map(File::getName)
                .filter(name -> !name.equalsIgnoreCase("credentials.txt"))
                .sorted()
                .collect(Collectors.toList());
    }

}
