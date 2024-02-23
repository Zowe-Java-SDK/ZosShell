package zos.shell.service.autocomplete;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.service.change.ChangeConnService;

import java.util.List;

public final class SearchCommandService {

    private static final Logger LOG = LoggerFactory.getLogger(ChangeConnService.class);

    private static final String[] commands = new String[]{"browsejob", "cancel", "cat", "cd", "change", "clear",
            "color", "connections", "count", "copy", "download", "downloadjob", "end", "env", "files", "grep",
            "help", "history", "hostname", "ls", "mkdir", "mvs", "purge", "ps", "pwd", "rename", "rm", "rn", "save",
            "search", "set", "stop", "submit", "tail", "timeout", "touch", "tso", "uname", "ussh", "vi", "visited",
            "whoami"};

    public SearchCommandService() {
        LOG.debug("*** SearchCommandService ***");
    }

    private final TriePreFix dictionary = new TriePreFix(commands);

    public List<String> search(String prefix) {
        LOG.debug("*** search command for autofill ***");
        return dictionary.getCommands(prefix);
    }

}
