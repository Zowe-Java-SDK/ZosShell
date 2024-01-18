package zos.shell.service.autocomplete;

import java.util.ArrayList;
import java.util.List;

/*
 * This class implements a trie data structure.
 */
public class TrieNode {

    private static final int ALPHABET_SIZE = 26;
    private static final int BUFFER_SIZE = 1024;
    private TrieNode[] children;
    private TrieNode parent; // parent allows to backtrack nodes to form the command
    private char character; // while looping through parent get the char to form the command
    private boolean isCmd;
    private boolean leaf;

    public TrieNode() {
        this.setChildren(new TrieNode[ALPHABET_SIZE]);
        this.setCmd(false);
        // no character here as such leaf = true
        // this is used for checking while looping through the parent
        // to form the command, any leaf set as true is the root and is
        // the end of the looping point, there is no character in
        // the root node
        this.setLeaf(true);
    }

    public void addCommand(String command) {
        int limit = command.length();
        var node = this;
        int letter, index;

        command = command.toLowerCase();
        for (var i = 0; i < limit; i++) {
            letter = command.charAt(i);
            index = letter - 'a';

            if (!isNotLetter(letter)) {
                continue;
            }

            if (node.children[index] == null) {
                node.children[index] = new TrieNode();
                node.children[index].character = (char) letter;
            }
            node.children[index].setLeaf(false); // character is here as such leaf = false
            node.children[index].parent = node;
            node = node.children[index];
        }
        node.setCmd(true);
    }

    private boolean isNotLetter(int letter) {
        return Character.isAlphabetic(letter);
    }

    public List<String> FindAndRetrieveCommands() {
        List<String> cmds = new ArrayList<>();
        var node = this;

        if (node.isCmd()) {
            cmds.add(node.getCommand());
        }

        if (node.isNotLeaf()) {
            for (var i = 0; i < node.children.length; i++) {
                if (node.children[i] != null) {
                    cmds.addAll(children[i].FindAndRetrieveCommands());
                }
            }
        }

        return cmds;
    }

    private String getCommand() {
        var node = this;
        var buffer = new char[BUFFER_SIZE];
        var result = new StringBuilder();
        String value;

        var i = 0;
        while (node.isNotLeaf()) {
            buffer[i] = node.character;
            node = node.parent;
            i++;
        }

        result.append(buffer);
        value = result.reverse().toString().trim();

        return value;
    }

    public TrieNode getNode(int index) {
        var node = this;

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
     * return a list of those commands.
     */
    public List<String> getCommands() {
        var node = this;
        List<String> cmds = new ArrayList<>();

        for (var i = 0; i < ALPHABET_SIZE; i++) {
            if (node.children[i] != null) {
                cmds.addAll(node.getChildren()[i].FindAndRetrieveCommands());
            }
        }

        return cmds;
    }

    public TrieNode[] getChildren() {
        return children;
    }

    public void setChildren(TrieNode[] children) {
        this.children = children;
    }

    public boolean isCmd() {
        return isCmd;
    }

    public void setCmd(boolean isCmd) {
        this.isCmd = isCmd;
    }

    public boolean isNotLeaf() {
        return !leaf;
    }

    public void setLeaf(boolean leaf) {
        this.leaf = leaf;
    }

    public void addCommands(String[] commands) {
        for (final String s : commands) {
            addCommand(s);
        }
    }

}
