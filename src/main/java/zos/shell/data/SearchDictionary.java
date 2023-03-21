package zos.shell.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.commands.BrowseJob;

import java.util.List;

public final class SearchDictionary {

    private static Logger LOG = LoggerFactory.getLogger(SearchDictionary.class);

    private static final String[] commands = new String[]{"browsejob", "cancel", "cat", "cd", "change", "clear",
            "clearlog", "color", "connections", "count", "copy", "copys", "download", "downloadjob", "end", "files",
            "help", "history", "ls", "mvs", "purgejob", "ps", "pwd", "rm", "save", "search", "stop", "submit",
            "tailjob", "timeout", "touch", "uname", "ussh", "vi", "visited", "whoami"};

    private static final TriePreFix dictionary = new TriePreFix(commands);

    public static List<String> search(String prefix) {
        LOG.debug("*** search ***");
        return dictionary.getCommands(prefix);
    }

}
