package zos.shell.utility;

import org.apache.commons.lang3.SystemUtils;
import org.beryx.textio.TextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.Constants;
import zos.shell.dto.DataSetMember;
import zos.shell.dto.Member;
import zowe.client.sdk.core.ZOSConnection;
import zowe.client.sdk.zosfiles.ZosDsnList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

public class Util {

    private static final Logger LOG = LoggerFactory.getLogger(Util.class);

    private static final String PATTERN_STRING_MORE_THAN_ONE_CHAR = "[A-Z#@\\$][A-Z\\d#@\\$\\-]{1,7}";
    private static final String PATTERN_STRING_FOR_ONE_CHAR = "[A-Z#@\\$]{1}";

    public static boolean isDataSet(String dataSetName) {
        LOG.debug("*** isDataSet ***");
        dataSetName = dataSetName.toUpperCase(Locale.ROOT);

        // Check that the dataset contains more than one segment
        // This could be valid for additionalTests
        final var segments = dataSetName.split("\\.");
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

        for (final var segment : segments) {
            if (!isSegment(segment)) {
                return false;
            }
        }

        return true;
    }

    public static boolean isMember(String memberName) {
        LOG.debug("*** isMember ***");
        return isSegment(memberName.toUpperCase(Locale.ROOT));
    }

    public static void printError(TextTerminal<?> terminal, String message) {
        LOG.debug("*** printError ***");
        if (message.contains("Not Found")) {
            final var index = message.indexOf("Not Found");
            final var msg = message.substring(index);
            terminal.println(msg);
        } else if (message.contains(Constants.CONNECTION_REFUSED)) {
            terminal.println(Constants.SEVERE_ERROR);
        } else if (message.contains("dataSetName not specified")) {
            terminal.println(Constants.DATASET_NOT_SPECIFIED);
        } else {
            terminal.println(message);
        }
    }

    public static String getErrorMsg(String message) {
        LOG.debug("*** getErrorMsg ***");
        if (message.contains("Not Found")) {
            final var index = message.indexOf("Not Found");
            return message.substring(index);
        } else if (message.contains(Constants.CONNECTION_REFUSED)) {
            return Constants.SEVERE_ERROR;
        } else if (message.contains("dataSetName not specified")) {
            return Constants.DATASET_NOT_SPECIFIED;
        } else {
            return message;
        }
    }

    public static boolean isStrNum(String strNum) {
        LOG.debug("*** isStrNum ***");
        try {
            Integer.parseInt(strNum);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    public static boolean isHttpError(int statusCode) {
        LOG.debug("*** isHttpError ***");
        return !((statusCode >= 200 && statusCode <= 299) || (statusCode >= 100 && statusCode <= 199));
    }

    public static DataSetMember getMemberFromDataSet(String param) {
        LOG.debug("*** getMemberFromDataSet ***");
        final var index = param.indexOf("(");
        final var dataset = param.substring(0, index);
        if (!Util.isDataSet(dataset)) {
            return null;
        }

        final var member = param.substring(index + 1, param.length() - 1);
        return new DataSetMember(dataset, member);
    }

    public static List<String> getMembers(TextTerminal<?> terminal, ZOSConnection connection, String dataSet) {
        LOG.debug("*** getMembers ***");
        final var member = new Member(new ZosDsnList(connection));
        final List<String> members;
        try {
            members = member.getMembers(dataSet);
        } catch (Exception e) {
            Util.printError(terminal, e.getMessage());
            return new ArrayList<>();
        }
        return members;
    }

    private static boolean isSegment(String segment) {
        LOG.debug("*** isSegment ***");
        // Each segment cannot be more than 8 characters
        // Each segment's first letter is a letter or #, @, $.
        // The remaining seven characters in a segment can be letters, numbers, and #, @, $, -
        var size = segment.length();
        if (size > 8) {
            return false;
        }

        if (size == 1) {
            final var p = Pattern.compile(PATTERN_STRING_FOR_ONE_CHAR);
            final var m = p.matcher(segment);
            return m.matches();
        }

        final var p = Pattern.compile(PATTERN_STRING_MORE_THAN_ONE_CHAR);
        final var m = p.matcher(segment);
        return m.matches();
    }

    public static String getMsgAfterArrow(String msg) {
        LOG.debug("*** getMsgAfterArrow ***");
        if (!msg.contains(Constants.ARROW)) {
            return msg;
        }
        final var index = msg.indexOf(Constants.ARROW) + Constants.ARROW.length();
        return msg.substring(index);
    }

    @SuppressWarnings("SameReturnValue")
    public static String getPrompt() {
        LOG.debug("*** getPrompt ***");
        return Constants.DEFAULT_PROMPT;
    }

    public static void writeTextFile(String content, String directoryPath, String fileNamePath) throws IOException {
        LOG.debug("*** writeTextFile ***");
        Files.createDirectories(Paths.get(directoryPath));
        Files.write(Paths.get(fileNamePath), content.getBytes());
    }

    public static String[] stripEmptyStrings(String[] command) {
        LOG.debug("*** stripEmptyStrings ***");
        final var list = new ArrayList<>(Arrays.asList(command));
        list.removeAll(Collections.singleton(""));
        command = list.toArray(new String[0]);
        return command;
    }

    public static void openFileLocation(String filePath) {
        LOG.debug("*** openFileLocation ***");
        if (filePath == null) {
            return;
        }

        try {
            if (SystemUtils.IS_OS_WINDOWS) {
                Runtime.getRuntime().exec("explorer.exe /select, " + filePath);
            } else if (SystemUtils.IS_OS_MAC_OSX) {
                final var arr = filePath.split("/");
                final var str = new StringBuilder();
                for (var i = 0; i < arr.length - 1; i++) {
                    str.append(arr[i]).append("/");
                }
                Runtime.getRuntime().exec("/usr/bin/open " + str);
            }
        } catch (IOException ignored) {
        }
    }

}
