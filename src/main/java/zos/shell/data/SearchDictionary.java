package zos.shell.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class SearchDictionary {

    private static final Logger LOG = LoggerFactory.getLogger(SearchDictionary.class);

    private static final String[] commands = new String[]{"browsejob", "cancel", "cat", "cd", "change", "clear",
            "clearlog", "color", "connections", "count", "copy", "copys", "download", "downloadjob", "end", "env",
            "files", "grep", "help", "history", "ls", "mvs", "purgejob", "ps", "pwd", "rm", "save", "search", "set",
            "stop", "submit", "tailjob", "timeout", "touch", "uname", "ussh", "vi", "visited", "whoami"};

    private static final TriePreFix dictionary = new TriePreFix(commands);

    public static List<String> search(String prefix) {
        LOG.debug("*** search ***");
        return dictionary.getCommands(prefix);
    }

}
