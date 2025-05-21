package zos.shell.service.help;

import org.beryx.textio.TextTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.service.search.SearchCache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class HelpService {

    private static final Logger LOG = LoggerFactory.getLogger(HelpService.class);

    private static final Map<String, Map.Entry<String, String>> HELP = Map.ofEntries(
            Map.entry("browsejob", Map.entry("bj | browsejob <arg1> <arg2>", "arg1=task/job name; display job's JESMSGLG spool output; arg2=optional or \"all\"; all=display all job's spool output")),
            Map.entry("cancel", Map.entry("cancel <arg>", "arg=task/job name; cancel ")),
            Map.entry("cat", Map.entry("cat", "display contents")),
            Map.entry("cd", Map.entry("cd <arg>", "arg is a dataset value or ..")),
            Map.entry("change", Map.entry("change <arg>", "arg=connection number, ordered connections defined in config.json")),
            Map.entry("clear", Map.entry("clear", "clear screen contents and search cache")),
            Map.entry("color", Map.entry("color <arg> <arg2>", "change color arg is prompt and text and arg2 is background color, i.e. blue, yellow, cyan etc..")),
            Map.entry("connections", Map.entry("connections", "connection(s) list from config.json")),
            Map.entry("count members", Map.entry("count members", "number of members in PWD")),
            Map.entry("count datasets", Map.entry("count datasets", "number of datasets in PWD")),
            Map.entry("copy", Map.entry("cp | copy <arg> arg>", "arg can be \".\", member, dataset or dataset(member)")),
            Map.entry("download", Map.entry("d | download <arg1> <arg2>", "arg1=member or sequential dataset; download to \\ZosShell\\pwd; arg2=optional or -b for binary download")),
            Map.entry("downloadjob", Map.entry("dj | downloadjob <arg1> <arg2>", "arg1=task/job name; download JESMSGLG spool output; arg2=\"all\", download all spool content")),
            Map.entry("echo", Map.entry("echo <arg>", "echo arg value and translate any env value delimited with $")),
            Map.entry("end", Map.entry("end", "exit UI shell")),
            Map.entry("env", Map.entry("env", "echo all env variables")),
            Map.entry("files", Map.entry("files", "list all files under local PWD drive value")),
            Map.entry("grep", Map.entry("g | grep <arg> <arg2>", "arg is search string and arg2 is member value")),
            Map.entry("help", Map.entry("h | help <arg>", "list all commands-details; arg=-l, list all command names; arg=command, list command-detail")),
            Map.entry("history", Map.entry("history <arg>", "arg is optional and indicates the number to display from bottom")),
            Map.entry("hostname", Map.entry("hostname", "echo hostname connection")),
            Map.entry("!n", Map.entry("!n", "n=number, echo command from history list")),
            Map.entry("!string", Map.entry("!string", "execute previous command from history list")),
            Map.entry("!!", Map.entry("!!", "execute previous command")),
            Map.entry("ls", Map.entry("ls <arg>", "arg is optional; list members/datasets in PWD; arg=filter(*); filter is dataset or member value")),
            Map.entry("ls -l", Map.entry("ls -l <arg>", "arg is optional; list members/datasets with attributes in PWD; arg=filter(*); filter is dataset or member value")),
            Map.entry("ls --l", Map.entry("ls --l <arg>", "same as ls -l without attribute info")),
            Map.entry("mkdir", Map.entry("mkdir <arg>", "arg is a dataset")),
            Map.entry("mvs", Map.entry("mvs <arg>", "execute console command, arg=command in double quotes")),
            Map.entry("ps", Map.entry("ps <arg>", "arg is optional; list all tasks/jobs; arg=filter(*)")),
            Map.entry("purge", Map.entry("p | purge <arg>", "arg=job name or id; purge")),
            Map.entry("pwd", Map.entry("pwd", "current working dataset location")),
            Map.entry("rename", Map.entry("rn | rename <arg1> <arg2>", "rename member or sequential, arg1=old arg2=new")),
            Map.entry("rm", Map.entry("rm <arg>", "arg is member with wildcard \"*\", member, dataset, or dataset(member)")),
            Map.entry("save", Map.entry("save <arg>", "arg=file name from files command to the current PWD")),
            Map.entry("search", Map.entry("search <arg>", "search previous command contents")),
            Map.entry("set", Map.entry("set <arg>", "set environment variable with arg value in the following format: key=value")),
            Map.entry("stop", Map.entry("stop <arg>", "arg=task/job name; stop")),
            Map.entry("submit", Map.entry("submit <arg>", "arg=task/job name; submit")),
            Map.entry("tail", Map.entry("tail <arg1> <arg2> <arg3>", "arg1=task/job name; display content from bottom; arg2 & arg3 = optional; arg2=limit num (25 default) and arg3=all spool content")),
            Map.entry("timeout", Map.entry("t | timeout <arg>", "echo current timeout value or change value with arg")),
            Map.entry("touch", Map.entry("touch <arg>", "create empty member if does not exist, arg represents a member or dataset(member)")),
            Map.entry("tso", Map.entry("tso <arg>", "execute tso command, arg=command in double quotes")),
            Map.entry("uname", Map.entry("uname", "echo connection hostname and z/OS version")),
            Map.entry("usermod", Map.entry("usermod <arg>", "modify username or password of current connection, arg can be either -u or -p")),
            Map.entry("ussh", Map.entry("ussh <arg>", "execute OMVS/USS command via SSH connection, arg=command within double quotes")),
            Map.entry("vi", Map.entry("vi <arg>", "arg is a member, sequential dataset, or dataset(member), arg will be downloaded")),
            Map.entry("visited", Map.entry("v | visited", "list of visited datasets")),
            Map.entry("whoami", Map.entry("whoami", "current connection's username"))
    );

    public static SearchCache display(TextTerminal<?> terminal) {
        LOG.debug("*** display ***");
        var keys = new ArrayList<>(HELP.keySet());
        Collections.sort(keys);
        var str = new StringBuilder();
        for (var key : keys) {
            var value = HELP.get(key);
            var columnFormat = "%-30s - %-11s";
            var helpValue = String.format(columnFormat, key, value.getValue());
            str.append(helpValue).append("\n");
            terminal.println(helpValue);
        }
        return new SearchCache("help", str);
    }

    public static SearchCache displayCommandNames(TextTerminal<?> terminal) {
        LOG.debug("*** displayCommandNames ***");
        var keys = new ArrayList<>(HELP.keySet());
        Collections.sort(keys);
        var str = new StringBuilder();
        for (var key : keys) {
            str.append(key).append("\n");
            terminal.println(key);
        }
        return new SearchCache("help", str);
    }

    public static SearchCache displayCommand(TextTerminal<?> terminal, String command) {
        LOG.debug("*** displayCommand ***");
        var value = HELP.get(command);
        if (value == null) {
            terminal.println("command not found, try again...");
            return new SearchCache("help", new StringBuilder());
        }
        var columnFormat = "%-30s - %-11s";
        var helpValue = String.format(columnFormat, command, value.getValue());
        terminal.println(helpValue);
        return new SearchCache("help", new StringBuilder(helpValue));
    }

}
