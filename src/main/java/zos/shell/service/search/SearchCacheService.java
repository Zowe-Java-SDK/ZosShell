package zos.shell.service.search;

import org.beryx.textio.TextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class SearchCacheService {

    private static final Logger LOG = LoggerFactory.getLogger(SearchCacheService.class);

    private final TextTerminal<?> terminal;

    public SearchCacheService(TextTerminal<?> terminal) {
        LOG.debug("*** SearchCacheService ***");
        this.terminal = terminal;
    }

    public void search(SearchCache output, String text) {
        LOG.debug("*** search ***");
        final var log = Optional.ofNullable(output);
        log.ifPresentOrElse((value) -> {
            terminal.println("searching " + value.getName().toUpperCase() + "...");
            final var results = Arrays.stream(value.getOutput().toString().split("\n"))
                    .filter(line -> line.toUpperCase().contains(text.toUpperCase()))
                    .collect(Collectors.toList());
            if (!results.isEmpty()) {
                results.forEach(terminal::println);
            } else {
                terminal.println("no results found...");
            }
        }, () -> terminal.println("nothing to search for..."));
    }

}
