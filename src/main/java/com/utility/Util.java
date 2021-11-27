package com.utility;

import com.Constants;
import org.beryx.textio.TextTerminal;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

    private static final String PATTERN_STRING = "[A-Z#@\\$]{1}[A-Z0-9#@\\$\\-]{1,7}";

    public static boolean isDataSet(String dataSetName) {
        dataSetName = dataSetName.toUpperCase(Locale.ROOT);

        // Check that the dataset contains more than one segment
        // This could be valid for additionalTests
        String[] segments = dataSetName.split("\\.");
        if (segments.length < 2) {
            return false;
        }

        // The length cannot be longer than 44
        if (dataSetName.length() > 44) {
            return false;
        }

        // The name cannot contain two successive periods
        if (dataSetName.contains("..")) {
            return false;
        }

        // Cannot end in a period
        if (dataSetName.endsWith(".")) {
            return false;
        }

        // Each segment cannot be more than 8 characters
        // Each segment's first letter is a letter or #, @, $.
        // The remaining seven characters in a segment can be letters, numbers, and #, @, $, -
        for (String segment : segments) {
            if (segment.length() > 8) {
                return false;
            }
            Pattern p = Pattern.compile(PATTERN_STRING);
            Matcher m = p.matcher(segment);
            if (!m.matches()) {
                return false;
            }
        }

        return true;
    }

    public static boolean isMember(String memberName) {
        memberName = memberName.toUpperCase(Locale.ROOT);

        // A member name cannot be longer than eight characters.
        // The first member character must be either a letter or one of the following three special characters: #, @, $.
        // The remaining seven characters can be letters, numbers, or one of the following special characters: #, @, or $.
        // A PDS member name cannot contain a hyphen (-).
        if (memberName.length() > 8) {
            return false;
        }
        Pattern p = Pattern.compile(PATTERN_STRING);
        Matcher m = p.matcher(memberName);
        return m.matches();
    }

    public static void printError(TextTerminal<?> terminal, String message) {
        if (message.contains("Not Found")) {
            terminal.println(Constants.NOT_FOUND);
        } else if (message.contains("Connection refused")) {
            terminal.println(Constants.SEVERE_ERROR);
        } else if (message.contains("dataSetName not specified")) {
            terminal.println(Constants.DATASET_NOT_SPECIFIED);
        } else {
            terminal.println(message);
        }
    }

    public static boolean isStrNum(String strNum) {
        try {
            Integer.parseInt(strNum);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

}
