package zos.shell.constants;

public final class Constants {

    private Constants() {
        throw new IllegalStateException("Constants class");
    }

    public static final String APP_TITLE = "ZosShell";
    public static final String ARROW = " ==> ";
    public static final String BROWSE_TIMEOUT = "timeout, log may be too large to display, try again...";
    public static final String COMMAND_EXECUTION_ERROR_MSG = "error executing command, try again...";
    public static final String COMMAND_EXTRA_TEXT_INVALID_COMMAND =
            "invalid command, remove extra text beyond second double quote and try again...";
    public static final String CONNECTION_REFUSED = "connection refused";
    public static final String COMMAND_INVALID_COMMAND =
            "invalid command, please use two double quotes only around the command string and try again...";
    public static final String COPY_NOTHING_WARNING = "nothing to copy, try again...";
    public static final String COPY_ITSELF_ERROR =
            "first argument invalid for copy, cant copy from same location, try again...";
    public static final String COPY_NO_MEMBER_ERROR =
            "first argument invalid for copy, specified a member, try again...";
    public static final String COPY_NO_MEMBER_AND_DATASET_ERROR =
            "first argument invalid for copy, specified valid dataset or member or dataset(member), try again...";
    public static final String DATASET_OR_HIGH_QUALIFIER_ERROR =
            "invalid dataset or cant change to high qualifier level, try again...";
    public static final String DATASET_NOT_SPECIFIED = "no dataset specified, try again...";
    public static final String DELETE_OPS_NO_MEMBER_AND_DATASET_ERROR =
            "first argument invalid for rm operation, specified valid dataset or member or dataset(member), try again...";
    public static final String DEFAULT_PROMPT = ">";
    public static final String DELETE_NOTHING_ERROR = "nothing to delete, try again...";
    public static final String DOWNLOAD_FAIL = "download failed, try again...";
    public static final String DOWNLOAD_NOTHING_WARNING = "nothing to download, try again...";
    public static final long FUTURE_TIMEOUT_VALUE = 10;
    public static final int HISTORY_SIZE = 200;
    public static final String INVALID_ARGUMENTS = "invalid arguments, try again...";
    public static final String INVALID_COMMAND = "invalid command, try again...";
    public static final String INVALID_DATASET = "invalid dataset specified, try again...";
    public static final String INVALID_DATASET_AND_MEMBER = "invalid dataset or member specified, try again...";
    public static final String INVALID_NUMBER = "specify a number, try again...";
    public static final String INVALID_MEMBER = "invalid member name format specified, try again...";
    public static final String INVALID_PARAMETER = "invalid parameter, try again...";
    public static final String INVALID_VALUE = "invalid value, try again...";
    public static final String HIGH_QUALIFIER_ERROR = "cant change to high qualifier level, try again...";
    public static final String NO_CONNECTIONS = "no connection(s) made or defined...";
    public static final String NO_CONNECTION_INFO = "no info, check connection settings...";
    public static final String NO_CONNECTION = "no connection to change too...";
    public static final String NO_FILES = "no files within " + Constants.PATH_FILE_DIRECTORY_WINDOWS + ", try again...";
    public static final String NO_HISTORY = "no history, try again...";
    public static final String NO_INFO = "no info...";
    public static final String NO_LISTING =
            "no listing results returned, connection problem or data set may not exist, try again...";
    public static final String NO_MEMBERS = "no members...";
    public static final String NOTHING_FOUND = "nothing found, try again...";
    public static final String NO_PROCESS_FOUND = "no process found, try again...";
    public static final String MAKE_DIR_EXIT_MSG = "exited mkdir command...";
    public static final String MAC_EDITOR_NAME = "open -a TextEdit";
    public static final String MISSING_COUNT_PARAM = "specified either \"count members\" or \"count datasets\"";
    public static final String MISSING_PARAMETERS = "missing parameter(s), try again...";
    public static final String MVS_EXECUTION_SUCCESS = "mvs command executed...";
    public static final String PATH_FILE_DIRECTORY_MAC = "/ZosShell";
    public static final String PATH_FILE_DIRECTORY_WINDOWS = "C:\\ZosShell";
    public static final String SECURITY_CONFIG_PATH_FILE_MAC = PATH_FILE_DIRECTORY_MAC + "/credentials.txt";
    public static final String SECURITY_CONFIG_PATH_FILE_WINDOWS = PATH_FILE_DIRECTORY_WINDOWS + "\\credentials.txt";
    public static final String SEQUENTIAL_DIRECTORY_LOCATION = "SEQUENTIAL_DATASET";
    public static final String CONFIG_PATH_FILE_MAC = PATH_FILE_DIRECTORY_MAC + "/config.txt";
    public static final String CONFIG_PATH_FILE_WINDOWS = PATH_FILE_DIRECTORY_WINDOWS + "\\config.txt";
    public static final String SEVERE_ERROR = "server error, check connection...";
    public static final int STRING_PAD_LENGTH = 8;
    public static final int THREAD_POOL_MIN = 1;
    public static final int THREAD_POOL_MAX = 10;
    public static final String TIMEOUT_MESSAGE = "command exceeded time out value, double check and try again...";
    public static final String TOO_MANY_PARAMETERS = "too many parameters, try again...";
    public static final String UTF8 = "UTF8";
    public static final String WINDOWS_EDITOR_NAME = "notepad";
    public static final String OS_ERROR = "command is only supported on Windows and Mac OSX...";

}
