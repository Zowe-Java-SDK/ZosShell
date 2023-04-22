package zos.shell.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Grep {

    private final Concatenate concatenate;
    private String pattern;
    private Map<Character, Integer> misMatchShiftsTable = new HashMap<>();

    public Grep(Concatenate concatenate, String pattern) {
        this.concatenate = concatenate;
        this.pattern = pattern;
        this.compileMisMatchShiftsTable();
    }

    public List<String> search(String dataSet, String member) {
        final var lines = new ArrayList<String>();
        final var responseStatus = concatenate.cat(dataSet, member);
        var content = new StringBuilder(responseStatus.getMessage());

        var index = findPosition(content.toString());
        while (index != 0) {
            String foundStr = content.substring(index);

            StringBuilder entireLine = new StringBuilder();
            for (var i = index - 1; i >= 0; i--) {
                if (content.charAt(i) == '\n') {
                    break;
                }
                entireLine.append(content.charAt(i));
            }
            entireLine.reverse();
            for (var i = 0; i < foundStr.length(); i++) {
                if (foundStr.charAt(i) == '\n') {
                    break;
                }
                entireLine.append(foundStr.charAt(i));
            }

            lines.add(entireLine.toString());
            var newIndex = index + pattern.length();
            if (newIndex > content.length()) {
                break;
            }
            content = new StringBuilder(content.substring(index + pattern.length()));
            index = findPosition(content.toString());
        }

        return lines;
    }

    private int findPosition(String text) {
        var lengthOfPattern = pattern.length();
        var lengthOfText = text.length();
        int numOfSkips;

        for (var i = 0; i <= lengthOfText - lengthOfPattern; i += numOfSkips) {
            numOfSkips = 0;
            for (var j = lengthOfPattern - 1; j >= 0; j--) {  // check starting from right to left
                if (pattern.charAt(j) != text.charAt(i + j)) {
                    if (misMatchShiftsTable.get(text.charAt(i + j)) != null) {
                        numOfSkips = misMatchShiftsTable.get(text.charAt(i + j));
                    } else {
                        numOfSkips = lengthOfPattern;
                    }
                    break;
                }
            }

            if (numOfSkips == 0) {  // means we found the matching position
                return i;
            }
        }

        return lengthOfText; // meaning have not found a match
    }

    private void compileMisMatchShiftsTable() {
        var lengthOfPattern = pattern.length();
        for (var i = 0; i < lengthOfPattern; i++) {
            misMatchShiftsTable.put(pattern.charAt(i), Math.max(1, lengthOfPattern - i - 1));
        }
    }

}

