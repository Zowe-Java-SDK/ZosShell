package com.utility;

import com.Constants;
import com.dto.DataSetMember;
import core.ZOSConnection;
import org.beryx.textio.TextTerminal;

import java.util.Locale;
import java.util.regex.Pattern;

public class Util {

    @SuppressWarnings("Annotator")
    private static final String PATTERN_STRING = "[A-Z#@\\$]{1}[A-Z0-9#@\\$\\-]{1,7}";

    public static boolean isDataSet(String dataSetName) {
        dataSetName = dataSetName.toUpperCase(Locale.ROOT);

        // Check that the dataset contains more than one segment
        // This could be valid for additionalTests
        var segments = dataSetName.split("\\.");
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

        for (var segment : segments) {
            if (!isSegment(segment)) return false;
        }

        return true;
    }

    public static boolean isMember(String memberName) {
        memberName = memberName.toUpperCase(Locale.ROOT);
        return isSegment(memberName);
    }

    public static void printError(TextTerminal<?> terminal, String message) {
        if (message.contains("Not Found")) {
            var index = message.indexOf("Not Found");
            var msg = message.substring(index);
            terminal.println(msg);
        } else if (message.contains(Constants.CONNECTION_REFUSED)) {
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

    public static String getPrompt(ZOSConnection connection) {
        var prompt = ">";
        if (connection == null)
            return prompt;
        var periodIndex = connection.getHost().indexOf(".");
        if (periodIndex != -1)
            return connection.getHost().substring(0, connection.getHost().indexOf(".")) + prompt;
        else
            return connection.getHost() + prompt;
    }

    public static boolean isHttpError(int statusCode) {
        return !((statusCode >= 200 && statusCode <= 299) || (statusCode >= 100 && statusCode <= 199));
    }

    public static DataSetMember getMemberFromDataSet(String param) {
        String member;
        String dataset;

        var index = param.indexOf("(");
        dataset = param.substring(0, index);
        if (!Util.isDataSet(dataset)) {
            return null;
        }

        member = param.substring(index + 1, param.length() - 1);
        return new DataSetMember(dataset, member);
    }

    private static boolean isSegment(String segment) {
        // Each segment cannot be more than 8 characters
        // Each segment's first letter is a letter or #, @, $.
        // The remaining seven characters in a segment can be letters, numbers, and #, @, $, -
        if (segment.length() > 8) {
            return false;
        }
        var p = Pattern.compile(PATTERN_STRING);
        var m = p.matcher(segment);
        return m.matches();
    }

    public static String getMsgAfterArrow(String msg) {
        var index = msg.indexOf(Constants.ARROW) + Constants.ARROW.length();
        return msg.substring(index);
    }

}
