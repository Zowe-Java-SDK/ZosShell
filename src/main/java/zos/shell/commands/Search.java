package zos.shell.commands;

import org.beryx.textio.TextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.dto.Output;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class Search {

    private static final Logger LOG = LoggerFactory.getLogger(Search.class);

    private final TextTerminal<?> terminal;

    public Search(TextTerminal<?> terminal) {
        LOG.debug("*** Search ***");
        this.terminal = terminal;
    }

    public void search(Output output, String text) {
        LOG.debug("*** search ***");
        final var log = Optional.ofNullable(output);
        log.ifPresentOrElse((value) -> {
            terminal.println("searching " + value.getName().toUpperCase() + "...");
            final var results = Arrays.stream(value.getOutput().toString().split("\n"))
                    .filter(line -> line.toUpperCase().contains(text.toUpperCase()))
                    .collect(Collectors.toList());
            if (!results.isBlank()) {
                results.forEach(terminal::println);
            } else {
                terminal.println("no results found...");
            }
        }, () -> terminal.println("nothing to search for..."));
    }

}
