package zos.shell.service.help;

import org.beryx.textio.TextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.service.search.SearchCache;

import java.util.List;

public class HelpCmd {

    private static final Logger LOG = LoggerFactory.getLogger(HelpCmd.class);

    private static final List<String> HELP = List.of(
            "bj | browsejob <arg1> <arg2> - where arg1 is a job name and arg2 is optional",
            "                          if arg2 not specified, display job's JESMSGLG spool output",
            "                          if arg2 is equal to \"all\", display all job's spool output",
            "cancel <arg>            - where arg is a task/job name",
            "cat                     - display contents",
            "cd <arg>                - where arg is a dataset value or empty",
            "change <arg>            - where arg is a number representing a connection",
            "clear                   - clear the shell of all history and cached output for search command",
            "color <arg> <arg2>      - change color arg is prompt and text ",
            "                          and arg2 is background color, i.e. blue, yellow, cyan etc..",
            "connections             - a list of connection(s)",
            "count members           - return member count in dataset",
            "count datasets          - return dataset count in dataset",
            "cp | copy <arg> arg>    - where arg can be \".\", member, dataset or dataset(member)",
            "cps | copys <arg> arg>  - where at least one argument is a sequential dataset",
            "                        - for sequential dataset copying",
            "d | download <arg1> <arg2>  - download arg1 to local c:\\ZosShell\\pwd where arg1 is member or sequential dataset",
            "                          and arg2 is optional and only accepts \"-b\" for binary download ",
            "dj | downloadjob <arg1> <arg2> - download the latest job log where <arg1> is job name",
            "                          if arg2 not specified, download job's JESMSGLG spool output",
            "                          if arg2 is equal to \"all\", download all job's spool output",
            "end                     - end session closes shell UI window",
            "env                     - display environment variables",
            "files                   - list all files under local pwd drive value",
            "g | grep <arg> <arg2>   - where arg is search string and arg2 is member value",
            "h | help                - list commands",
            "history <arg>           - where arg is optional and indicates the number to display from bottom",
            "hostname                - display current hostname connection",
            "!n                      - where n is a number, to execute command number n in history list",
            "!string                 - will execute the last history command starting with that \"string\"",
            "!!                      - will execute the last history command",
            "ls <arg>                - where arg is optional and indicates a dataset or member value",
            "                        - for member value only you can specified * wild card as last character",
            "ls -l <arg>             - where arg is optional and indicates a dataset or member value",
            "                        - for member value only you can specified * wild card as last character",
            "ls --l <arg>            - same as above without attribute info",
            "mkdir <arg>             - where arg is a dataset",
            "mvs <arg>               - execute a mvs command where arg is a command string within double quotes",
            "ps                      - display all processes running",
            "ps <arg>                - where arg is a task/job name",
            "pj | purgejob <arg>     - purge a job name or job is arg can represent either",
            "pwd                     - show current working dataset",
            "rm <arg>                - where arg is \"*\", member, dataset, or dataset with member value",
            "save <arg>              - save arg where arg is a file name from files command to the current pwd",
            "search <arg>            - search for arg within last job browse, tailjob or member cat command",
            "set <arg>               - set environment variable with arg value in the following format: key=value",
            "stop <arg>              - where arg is a task/job name",
            "submit <arg>            - where arg is a member name",
            "tail <arg1> <arg2> <arg3> - where arg1 is job name and arg2 and arg3 are optional",
            "                          use arg2 to specify either line limit or \"all\" value",
            "                          if \"all\" is specified, display output from all of job's spool content",
            "                          line limit is 25 by default if not specified in arg2",
            "t | timeout <arg>       - where arg is optional, with arg value you set new timeout, without shows current value",
            "touch <arg>             - create member arg if it does not already exist",
            "tso <arg>               - execute a tso command where arg is a command string within double quotes",
            "uname                   - show current connected host name",
            "ussh <arg>              - execute USS/UNIX command via SSH connection where arg is a command string within double quotes",
            "vi <arg>                - where arg is a sequential dataset or member name, arg will be downloaded",
            "                          and displayed for editing, use save command to save changes",
            "v | visited             - a list of visited datasets",
            "whoami                  - show current connected user name");

    public static SearchCache display(TextTerminal<?> terminal) {
        LOG.debug("*** displayHelp ***");
        final var str = new StringBuilder();
        HELP.forEach(item -> {
            str.append(item).append("\n");
            terminal.println(item);
        });
        return new SearchCache("help", str);
    }

}
