package zos.shell.data;

import java.util.List;

public class SearchDictionary {

    private static TriePreFix dictionary;
    private static final String[] commands = new String[]{"browsejob", "cancel", "cat", "cd", "change", "clear",
            "clearlog", "color", "connections", "count", "copy", "download", "downloadjob", "end", "files",
            "help", "history", "ls", "mvs", "purgejob", "ps", "pwd", "rm", "save", "search", "stop", "submit",
            "tailjob", "timeout", "touch", "uname", "ussh", "vi", "visited", "whoami"};

    public SearchDictionary() {
        dictionary = new TriePreFix(commands);
    }

    public static List<String> search(String prefix) {
        return dictionary.getWords(prefix);
    }

}
