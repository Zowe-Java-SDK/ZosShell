package com.utility;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

    public static boolean isDataSet(String dataSetName) {
        dataSetName = dataSetName.toUpperCase(Locale.ROOT);
        String invalidDatasetMsg = "Invalid data set name '" + dataSetName + "'.";

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
            Pattern p = Pattern.compile("[A-Z#@\\$]{1}[A-Z0-9#@\\$\\-]{1,7}");
            Matcher m = p.matcher(segment);
            if (!m.matches()) {
                return false;
            }
        }

        return true;
    }

}