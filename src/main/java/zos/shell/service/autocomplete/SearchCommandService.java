package zos.shell.service.autocomplete;

import java.util.List;

public final class SearchCommandService {

    private static final String[] commands = new String[]{"browse", "cancel", "cat", "cd", "change", "clear",
            "color", "connections", "count", "copy", "download", "downloadjob", "end", "env", "files", "grep",
            "help", "history", "hostname", "ls", "mkdir", "mvs", "purge", "ps", "pwd", "rm", "save", "search",
            "set", "stop", "submit", "tail", "timeout", "touch", "tso", "uname", "ussh", "vi", "visited", "whoami"};

    private static final TriePreFix dictionary = new TriePreFix(commands);

    public static List<String> search(String prefix) {
        return dictionary.getCommands(prefix);
    }

}
