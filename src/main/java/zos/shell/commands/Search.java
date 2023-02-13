package zos.shell.commands;

import org.beryx.textio.TextTerminal;
import zos.shell.dto.Output;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Search {

    private final TextTerminal<?> terminal;

    public Search(TextTerminal<?> terminal) {
        this.terminal = terminal;
    }

    @SuppressWarnings("DataFlowIssue")
    public void search(Output output, String text) {
        final var log = Optional.ofNullable(output);
        log.ifPresentOrElse((value) -> {
            final var name = value.getName();
            terminal.println("searching " + name.toUpperCase() + "...");
            List<String> results = new ArrayList<>();
            try {
                results = Arrays.stream(output.getOutput().toString().split("\n"))
                        .filter(line -> line.toUpperCase().contains(text.toUpperCase())).collect(Collectors.toList());
            } catch (Exception ignored) {
            }
            if (!results.isEmpty()) {
                results.forEach(terminal::println);
            } else {
                terminal.println("no results found...");
            }
        }, () -> terminal.println("nothing to search for..."));
    }

}
