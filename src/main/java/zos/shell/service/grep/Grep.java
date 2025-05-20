package zos.shell.service.grep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.response.ResponseStatus;
import zos.shell.service.dsn.concat.ConcatService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Grep {

    private static final Logger LOG = LoggerFactory.getLogger(Grep.class);

    private final ConcatService concatenate;
    private final String pattern;
    private final boolean withMember;
    private final Map<Character, Integer> misMatchShiftsTable = new HashMap<>();

    public Grep(final ConcatService concatenate, final String pattern, final boolean withMember) {
        LOG.debug("*** Grep ***");
        this.concatenate = concatenate;
        this.pattern = pattern;
        this.withMember = withMember;
        this.compileMisMatchShiftsTable();
    }

    public List<String> search(final String currDataSet, final String target) {
        LOG.debug("*** search ***");
        List<String> lines = new ArrayList<>();
        ResponseStatus responseStatus = concatenate.cat(currDataSet, target);
        var content = new StringBuilder(responseStatus.getMessage());
        var title = target + ":";

        var index = findPosition(content.toString());
        while (index != 0) {
            var foundStr = content.substring(index);
            var entireLine = new StringBuilder();

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
                lines.add(withMember ? entireLine.insert(0, title).toString() : entireLine.toString());
            }
            var newIndex = index + pattern.length();
            if (newIndex > content.length()) {
                break;
            }
            content = new StringBuilder(content.substring(index + pattern.length()));
            index = findPosition(content.toString());
        }

        // handle an edge case if found on the first line only
        if (index == 0 && !content.toString().isBlank()) {
            lines.add(withMember ? content.insert(0, title).toString() : content.toString());
        }

        return lines;
    }

    private int findPosition(final String text) {
        LOG.debug("*** findPosition ***");
        int lengthOfPattern = pattern.length();
        int lengthOfText = text.length();
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

        return lengthOfText; // meaning has not found a match
    }

    private void compileMisMatchShiftsTable() {
        LOG.debug("*** compileMisMatchShiftsTable ***");
        int lengthOfPattern = pattern.length();
        for (var i = 0; i < lengthOfPattern; i++) {
            misMatchShiftsTable.put(pattern.charAt(i), Math.max(1, lengthOfPattern - i - 1));
        }
    }

}
