package zos.shell.data;

import java.util.List;

public final class SearchDictionary {

    private static final String[] commands = new String[]{"browsejob", "cancel", "cat", "cd", "change", "clear",
            "color", "connections", "count", "copy", "copys", "download", "downloadjob", "end", "env",
            "files", "grep", "help", "history", "hostname", "ls", "mkdir", "mvs", "purgejob", "ps", "pwd",
            "rm", "save", "search", "set", "stop", "submit", "tailjob", "timeout", "touch", "tso", "uname",
            "ussh", "vi", "visited", "whoami"};

    private static final TriePreFix dictionary = new TriePreFix(commands);

    public static List<String> search(String prefix) {
        return dictionary.getCommands(prefix);
    }

}
