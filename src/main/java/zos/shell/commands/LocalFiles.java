package zos.shell.commands;

import org.apache.commons.lang3.SystemUtils;
import org.beryx.textio.TextTerminal;
import zos.shell.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LocalFiles {

    public static void listFiles(TextTerminal<?> terminal, String dataSet) {
        final var files = getFiles(terminal, dataSet);
        if (files.isEmpty()) {
            terminal.println(Constants.NO_FILES);
            return;
        }
        files.forEach(terminal::println);
    }

    private static List<String> getFiles(TextTerminal<?> terminal, String dataSet) {
        if (!SystemUtils.IS_OS_WINDOWS && !SystemUtils.IS_OS_MAC_OSX) {
            return new ArrayList<>();
        }
        String path;
        if (dataSet == null || dataSet.isEmpty()) {
            if (SystemUtils.IS_OS_WINDOWS) {
                path = Constants.PATH_FILE_DIRECTORY_WINDOWS;
            } else {
                path = Constants.PATH_FILE_DIRECTORY_MAC;
            }
        } else {
            if (SystemUtils.IS_OS_WINDOWS) {
                path = Constants.PATH_FILE_DIRECTORY_WINDOWS + "\\" + dataSet;
            } else {
                path = Constants.PATH_FILE_DIRECTORY_MAC + "\\" + dataSet;
            }
        }
        terminal.println(path + ":");
        final Predicate<String> isNotCredentials = name -> !name.equalsIgnoreCase("credentials.txt");
        final Predicate<String> isNotColors = name -> !name.equalsIgnoreCase("colors.txt");
        final var files = Optional.ofNullable(new File(path).listFiles());
        return Stream.of(files.orElse(new File[]{}))
                .map(File::getName)
                .filter(isNotCredentials.and(isNotColors))
                .sorted()
                .collect(Collectors.toList());
    }

}