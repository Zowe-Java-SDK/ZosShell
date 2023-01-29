package zos.shell.data;

import java.util.List;

public class SearchDictionary {

    private final TriePreFix dictionary;

    private final String[] commands = new String[]{"browsejob", "cancel", "cat", "cd", "change", "clear",
            "clearlog", "color", "connections", "count", "copy", "download", "downloadjob", "end", "files",
            "help", "history", "ls", "mvs", "purgejob", "ps", "pwd", "rm", "save", "search", "stop", "submit",
            "tailjob", "timeout", "touch", "uname", "ussh", "vi", "visited", "whoami"};

    public SearchDictionary() {
        dictionary = new TriePreFix(commands);
    }

    public List<String> search(String prefix) {
        return dictionary.getCommands(prefix);
    }

}
