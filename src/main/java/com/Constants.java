package com;

public class Constants {

    public static final String APP_TITLE = "ZosShell";
    public static final String BROWSE_TIMEOUT_MSG =
            "timeout, log may be too large to display, try again...";
    public static final CharSequence CONNECTION_REFUSED = "Connection refused";
    public static final String COPY_OPS_ITSELF_ERROR =
            "first argument invalid for copy operation, cant copy from same location, try again...";
    public static final String COPY_OPS_NO_MEMBER_ERROR =
            "first argument invalid for copy operation, specified a member, try again...";
    public static final String COPY_OPS_NO_MEMBER_AND_DATASET_ERROR =
            "first argument invalid for copy operation, specified valid dataset and member, try again...";
    public static final String DATASET_OR_HIGH_QUALIFIER_ERROR =
            "invalid dataset or cant change to high qualifier level, try again...";
    public static final String DATASET_NOT_SPECIFIED = "no dataset specified, try again...";
    public static final String DELETE_OPS_NO_MEMBER_AND_DATASET_ERROR =
            "first argument invalid for delete operation, specified valid dataset and member, try again...";
    public static final String DELETE_NOTHING_ERROR = "nothing to delete, try again...";
    public static final String DOWNLOAD_FAIL = "download failed, try again...";
    public static final int HISTORY_SIZE = 200;
    public static final String INVALID_COMMAND = "invalid command, try again...";
    public static final String INVALID_DATASET = "invalid dataset specified, try again...";
    public static final String INVALID_NUMBER = "specify a number, try again...";
    public static final String INVALID_MEMBER = "invalid member name format specified, try again...";
    public static final String INVALID_PARAMETER = "invalid parameter, try again...";
    public static final String HIGH_QUALIFIER_ERROR = "cant change to high qualifier level, try again...";
    public static final String NO_CONNECTIONS = "No connection(s) made or defined...";
    public static final String NO_CONNECTION_INFO = "no info, check connection settings...";
    public static final String NO_CONNECTION = "no connection to change too...";
    public static final String NO_FILES = "no files within " + Constants.PATH_FILE_DIRECTORY_WINDOWS + ", try again...";
    public static final String NO_HISTORY = "no history, try again...";
    public static final String NO_INFO = "no info...";
    public static final String NO_LISTING =
            "no listing results returned, connection problem or data set may not exist, try again...";
    public static final String NO_MEMBERS = "no members...";
    public static final String NO_PROCESS_FOUND = "no process found, try again...";
    public static final String MISSING_COUNT_PARAM = "specified either \"count members\" or \"count datasets\"";
    public static final String MISSING_PARAMETERS = "missing parameter(s), try again...";
    public static final String PATH_FILE_DIRECTORY_WINDOWS = "C:\\ZosShell";
    public static final String PATH_FILE = PATH_FILE_DIRECTORY_WINDOWS + "\\credentials.txt";
    public static final String SEVERE_ERROR = "server error, check connection...";
    public static final String TOO_MANY_PARAMETERS = "too many parameters, try again...";
    public static final String WINDOWS_ERROR_MSG = "this command is only supported on windows at the moment...";

}
