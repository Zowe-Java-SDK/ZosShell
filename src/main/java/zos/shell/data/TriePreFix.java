package zos.shell.data;

import java.util.ArrayList;
import java.util.List;

public class TriePreFix {

    private final TrieNode root;

    public TriePreFix() {
        root = new TrieNode();
    }

    public TriePreFix(String[] words) {
        root = new TrieNode();
        root.addWords(words);
    }

    public void addWord(String word) {
        root.addWord(word);
    }

    public List<String> getWords(String prefix) {
        TrieNode lastNode = root;
        final var limit = prefix.length();

        // retrieve the last Node of the trie for
        // the last character of the prefix
        for (int i = 0; i < limit; i++) {
            lastNode = lastNode.getNode(prefix.charAt(i) - 'a');
            // if none, return empty array
            if (lastNode == null)
                return new ArrayList<>();
        }

        return lastNode.getWords();
    }

}
