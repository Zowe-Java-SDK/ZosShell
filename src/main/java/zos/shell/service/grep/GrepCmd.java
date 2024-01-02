package zos.shell.service.grep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.service.dsn.concatenate.ConcatCmd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GrepCmd {

    private static final Logger LOG = LoggerFactory.getLogger(GrepCmd.class);

    private final ConcatCmd concatenate;
    private final String pattern;
    private final boolean withMember;
    private final Map<Character, Integer> misMatchShiftsTable = new HashMap<>();

    public GrepCmd(ConcatCmd concatenate, String pattern) {
        LOG.debug("*** Grep #1 ***");
        this.concatenate = concatenate;
        this.pattern = pattern;
        this.withMember = false;
        this.compileMisMatchShiftsTable();
    }

    public GrepCmd(ConcatCmd concatenate, String pattern, boolean withMember) {
        LOG.debug("*** Grep #2 ***");
        this.concatenate = concatenate;
        this.pattern = pattern;
        this.withMember = withMember;
        this.compileMisMatchShiftsTable();
    }

    public List<String> search(String currDataSet, String target) {
        LOG.debug("*** search ***");
        final var lines = new ArrayList<String>();
        final var responseStatus = concatenate.cat(currDataSet, target);
        var content = new StringBuilder(responseStatus.getMessage());

        var index = findPosition(content.toString());
        while (index != 0) {
            final var foundStr = content.substring(index);
            final var entireLine = new StringBuilder();

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

            if (entireLine.length() > 0) {
                final var title = target + ":";
                lines.add(withMember ? entireLine.insert(0, title).toString() : entireLine.toString());
            }
            final var newIndex = index + pattern.length();
            if (newIndex > content.length()) {
                break;
            }
            content = new StringBuilder(content.substring(index + pattern.length()));
            index = findPosition(content.toString());
        }

        // handle edge case if found on first line only
        if (index == 0 && !content.toString().isBlank()) {
            lines.add(content.toString());
        }

        return lines;
    }

    private int findPosition(String text) {
        LOG.debug("*** findPosition ***");
        final var lengthOfPattern = pattern.length();
        final var lengthOfText = text.length();
        int numOfSkips;

        for (var i = 0; i <= lengthOfText - lengthOfPattern; i += numOfSkips) {
            numOfSkips = 0;
            for (var j = lengthOfPattern - 1; j >= 0; j--) {  // check starting from right to left
                if (pattern.charAt(j) != text.charAt(i + j)) {
                    if (misMatchShiftsTable.get(text.charAt(i + j)) != null) {
                        numOfSkips = misMatchShiftsTable.get(text.charAt(i + j));
                    } else {
                        numOfSkips = misMatchShiftsTable.size();
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
        LOG.debug("*** compileMisMatchShiftsTable ***");
        final var lengthOfPattern = pattern.length();
        for (var i = 0; i < lengthOfPattern; i++) {
            misMatchShiftsTable.put(pattern.charAt(i), Math.max(1, lengthOfPattern - i - 1));
        }
    }

}

