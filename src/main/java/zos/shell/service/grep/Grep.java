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
        if (pattern == null || pattern.isBlank()) {
            throw new IllegalArgumentException("pattern must not be null or blank");
        }
        this.concatenate = concatenate;
        this.pattern = pattern;
        this.withMember = withMember;
        compileMisMatchShiftsTable();
    }

    public List<String> search(final String currDataSet, final String target) {
        LOG.debug("*** search ***");

        List<String> lines = new ArrayList<>();
        ResponseStatus responseStatus = concatenate.cat(currDataSet, target);
        String content = responseStatus.getMessage();

        if (content == null || content.isBlank()) {
            return lines;
        }

        String title = target + ":";
        int searchFrom = 0;

        /*
         * Example of how the search loop works.
         *
         * Message:
         *   "line1\nhello world\nline3\nhello again"
         *
         * Pattern:
         *   "hello"
         *
         * Iteration 1
         *   searchFrom = 0
         *   findPosition() finds "hello" at index 6
         *
         *   Determine line boundaries:
         *     lineStart = last newline before index -> position 6 -> start of "hello world"
         *     lineEnd   = next newline after index   -> end of "hello world"
         *
         *   Extract line:
         *     "hello world"
         *
         *   Add to results.
         *
         *   Advance searchFrom to the start of the next line:
         *     searchFrom = lineEnd + 1
         *
         * Iteration 2
         *   searchFrom now points to "line3\nhello again"
         *   findPosition() finds "hello" again
         *
         *   Determine line boundaries:
         *     lineStart -> start of "hello again"
         *     lineEnd   -> end of message
         *
         *   Extract line:
         *     "hello again"
         *
         *   Add to results.
         *
         * Iteration 3
         *   searchFrom reaches message.length()
         *   Loop exits.
         *
         * Result:
         *   ["hello world", "hello again"]
         *
         * This approach ensures:
         *   - matches at index 0 are handled correctly
         *   - each matching line is returned once
         *   - the same line is not scanned repeatedly
         */
        while (searchFrom < content.length()) {
            // Continue searching until we reach the end of the content

            // Search for the pattern starting from the current search position.
            // findPosition() returns the index relative to the substring we pass in.
            int relativeIndex = findPosition(content.substring(searchFrom));

            // If no match was found (-1), we are done searching.
            if (relativeIndex < 0) {
                break;
            }

            // Convert the relative match index into the absolute index in the full content.
            int index = searchFrom + relativeIndex;

            // Find the start of the line containing the match.
            // lastIndexOf searches backwards from the match position for a newline.
            int lineStart = content.lastIndexOf('\n', index);

            // If no newline was found, the match is on the first line.
            // Otherwise, move one character forward to skip the newline.
            lineStart = (lineStart == -1) ? 0 : lineStart + 1;

            // Find the end of the line containing the match.
            // indexOf searches forward from the match position to the next newline.
            int lineEnd = content.indexOf('\n', index);

            // If no newline is found, the match occurs on the last line of the content.
            lineEnd = (lineEnd == -1) ? content.length() : lineEnd;

            // Extract the entire line that contains the match.
            String line = content.substring(lineStart, lineEnd);

            // If the line is not empty, add it to the results.
            // If withMember is true, prepend the dataset/member title.
            if (!line.isBlank()) {
                lines.add(withMember ? title + line : line);
            }

            // Move the search start position to the next line so we do not
            // match the same line again even if the pattern appears multiple times.
            // This mimics typical grep behavior (one output per matching line).
            searchFrom = (lineEnd == content.length()) ? content.length() : lineEnd + 1;
        }

        return lines;
    }

    private int findPosition(final String text) {
        LOG.debug("*** findPosition ***");

        int lengthOfPattern = pattern.length();
        int lengthOfText = text.length();
        int numOfSkips;

        for (int i = 0; i <= lengthOfText - lengthOfPattern; i += numOfSkips) {
            numOfSkips = 0;

            for (int j = lengthOfPattern - 1; j >= 0; j--) {
                if (pattern.charAt(j) != text.charAt(i + j)) {
                    Integer shift = misMatchShiftsTable.get(text.charAt(i + j));
                    numOfSkips = (shift != null) ? shift : lengthOfPattern;
                    break;
                }
            }

            if (numOfSkips == 0) {
                return i;
            }
        }

        return -1;
    }

    private void compileMisMatchShiftsTable() {
        LOG.debug("*** compileMisMatchShiftsTable ***");

        int lengthOfPattern = pattern.length();
        for (int i = 0; i < lengthOfPattern; i++) {
            misMatchShiftsTable.put(pattern.charAt(i), Math.max(1, lengthOfPattern - i - 1));
        }
    }

}
