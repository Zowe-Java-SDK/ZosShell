package zos.shell.service.autocomplete;

import java.util.ArrayList;
import java.util.List;

public class TriePreFix {

    private final TrieNode root;

    public TriePreFix(String[] commands) {
        root = new TrieNode();
        root.addCommands(commands);
    }

    public List<String> getCommands(String prefix) {
        var lastNode = root;
        final var limit = prefix.length();

        // retrieve the last Node of the trie for
        // the last character of the prefix
        for (var i = 0; i < limit; i++) {
            lastNode = lastNode.getNode(prefix.charAt(i) - 'a');
            // if none, return empty array
            if (lastNode == null) {
                return new ArrayList<>();
            }
        }

        return lastNode.getCommands();
    }

}
