package zos.shell.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zowe.client.sdk.zosfiles.dsn.response.Member;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DsnUtil {

    private DsnUtil() {
        throw new IllegalStateException("Utility class");
    }

    private static final Logger LOG = LoggerFactory.getLogger(DsnUtil.class);

    private static final String PATTERN_STRING_MORE_THAN_ONE_CHAR = "[A-Z#@\\$][A-Z\\d#@\\$\\-]{1,7}";
    private static final String PATTERN_STRING_FOR_ONE_CHAR = "[A-Z#@\\$]{1}";

    public static boolean isDataset(String name) {
        LOG.debug("*** isDataset ***");
        name = name.toUpperCase(Locale.ROOT);

        // Check that the dataset contains more than one segment
        // This could be valid for additionalTests
        var segments = name.split("\\.");
        if (segments.length < 2) {
            return false;
        }

        // The length cannot be longer than 44
        if (name.length() > 44) {
            return false;
        }

        // The name cannot contain two successive periods
        if (name.contains("..")) {
            return false;
        }

        // Cannot end in a period
        if (name.endsWith(".")) {
            return false;
        }

        for (var segment : segments) {
            if (!isSegment(segment)) {
                return false;
            }
        }

        return true;
    }

    public static boolean isMember(final String name) {
        LOG.debug("*** isMember ***");
        return isSegment(name.toUpperCase(Locale.ROOT));
    }

    private static boolean isSegment(final String segment) {
        LOG.debug("*** isSegment ***");
        // Each segment cannot be more than 8 characters
        // Each segment's first letter is a letter or #, @, $.
        // The remaining seven characters in a segment can be letters, numbers, and #, @, $, -
        var size = segment.length();
        if (size > 8) {
            return false;
        }

        if (size == 1) {
            var p = Pattern.compile(PATTERN_STRING_FOR_ONE_CHAR);
            var m = p.matcher(segment);
            return m.matches();
        }

        var p = Pattern.compile(PATTERN_STRING_MORE_THAN_ONE_CHAR);
        var m = p.matcher(segment);
        return m.matches();
    }

    public static List<Member> getMembersByFilter(final String filter, final List<Member> members) {
        LOG.debug("*** getMembersByFilter ***");
        Predicate<Member> isMemberPresent = m -> m.getMember().isPresent();
        Predicate<Member> isMemberFound = m -> m.getMember().orElse("").equalsIgnoreCase(filter.toUpperCase());
        return members.stream().filter(isMemberPresent.and(isMemberFound)).collect(Collectors.toList());
    }

    public static List<Member> getMembersByStartsWithFilter(final String filter, final List<Member> members) {
        LOG.debug("*** getMembersByStartsWithFilter ***");
        Predicate<Member> isMemberPresent = m -> m.getMember().isPresent();
        Predicate<Member> isMemberFound = m -> m.getMember().orElse("").startsWith(filter.toUpperCase());
        return members.stream().filter(isMemberPresent.and(isMemberFound)).collect(Collectors.toList());
    }

}
