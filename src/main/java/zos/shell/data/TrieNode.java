package zos.shell.data;

import java.util.ArrayList;
import java.util.List;

/*
 * This class implements a trie data structure.
 */
public class TrieNode {

    private static final int ALPHABET_SIZE = 26;
    private static final int BUFFER_SIZE = 1024;
    private TrieNode[] children;
    private TrieNode parent; // parent allows to backtrack nodes to form the word
    private char character; // while looping through parent get the char to form the word
    private boolean isWord;
    private boolean leaf;

    public TrieNode() {
        this.setChildren(new TrieNode[ALPHABET_SIZE]);
        this.setWord(false);
        // no character here as such leaf = true
        // this is used for checking while looping through the parent
        // to form the word, any leaf set as true is the root and is
        // the end of the looping point, there is no character in
        // the root node
        this.setLeaf(true);
    }

    public void addWord(String word) {
        final var limit = word.length();
        TrieNode node = this;
        int letter, index;

        word = word.toLowerCase();
        for (int i = 0; i < limit; i++) {
            letter = word.charAt(i);
            index = letter - 'a';

            if (!isNotLetter(letter))
                continue;

            if (node.children[index] == null) {
                node.children[index] = new TrieNode();
                node.children[index].character = (char) letter;
            }
            node.children[index].setLeaf(false); // character is here as such leaf = false
            node.children[index].parent = node;
            node = node.children[index];
        }
        node.setWord(true);
    }

    private boolean isNotLetter(int letter) {
        return Character.isAlphabetic(letter);
    }

    public List<String> FindAndRetrieveWords() {
        var words = new ArrayList<String>();
        TrieNode node = this;

        if (node.isWord())
            words.add(node.getWord());

        if (!node.isLeaf()) {
            for (int i = 0; i < node.children.length; i++) {
                if (node.children[i] != null) {
                    words.addAll(children[i].FindAndRetrieveWords());
                }
            }
        }

        return words;
    }

    private String getWord() {
        TrieNode node = this;
        var buffer = new char[BUFFER_SIZE];
        var result = new StringBuilder();
        String value;

        int i = 0;
        while (!node.isLeaf()) {
            buffer[i] = node.character;
            node = node.parent;
            i++;
        }

        result.append(buffer);
        value = result.reverse().toString().trim();

        return value;
    }

    public boolean isWord(String s) {
        TrieNode node = this;
        final var limit = s.length();

        for (int i = 0; i < limit; i++) {
            int letter = s.charAt(i);
            int index = letter - 'a';

            if (node.children[index] == null)
                return false;
            node = node.children[index];
        }

        return node.isWord();
    }

    public boolean isEmpty(TrieNode node) {
        if (node.children == null) {
            return true;
        }
        return false;
    }

    public TrieNode getNode(int index) {
        TrieNode node = this;

        if (node.children[index] != null) {
            return node.children[index];
        } else {
            return null;
        }
    }

    /*
     * When this method is called, the "this" Node is pointing to the last character
     * of the prefix string. From this Node, look at all its children nodes (array)
     * for branches of other patterns that complete the word with the prefix and
     * return a list of those words.
     */
    public List<String> getWords() {
        TrieNode node = this;
        var words = new ArrayList<String>();

        for (int i = 0; i < ALPHABET_SIZE; i++) {
            if (node.children[i] != null) {
                words.addAll(node.getChildren()[i].FindAndRetrieveWords());
            }
        }

        return words;
    }

    public TrieNode[] getChildren() {
        return children;
    }

    public void setChildren(TrieNode[] children) {
        this.children = children;
    }

    public TrieNode getParent() {
        return parent;
    }

    public void setParent(TrieNode parent) {
        this.parent = parent;
    }

    public char getCharacter() {
        return character;
    }

    public void setCharacter(char character) {
        this.character = character;
    }

    public boolean isWord() {
        return isWord;
    }

    public void setWord(boolean isWord) {
        this.isWord = isWord;
    }

    public boolean isLeaf() {
        return leaf;
    }

    public void setLeaf(boolean leaf) {
        this.leaf = leaf;
    }

    public void addWords(String[] words) {
        for (String s : words) {
            addWord(s);
        }
    }

}
