package zos.shell.commandcli;

import zos.shell.commandcli.impl.*;

import java.util.HashMap;
import java.util.Map;

public class CommandRegistry {

    private final Map<String, CommandHandler> commands = new HashMap<>();

    public CommandRegistry() {
        // Browse Job
        register("bj", new BrowseJobCommand());
        register("browsejob", new BrowseJobCommand());

        // Download / Download Job
        register("d", new DownloadCommand());
        register("download", new DownloadCommand());
        register("dj", new DownloadJobCommand());
        register("downloadjob", new DownloadJobCommand());

        // File operations
        register("mkdir", new MkdirCommand());
        register("vi", new ViCommand());
        register("v", new VisitedCommand());
        register("visited", new VisitedCommand());
        register("g", new GrepCommand());
        register("grep", new GrepCommand());
        register("cat", new CatCommand());
        register("ls", new LsCommand());
        register("touch", new TouchCommand());
        register("rm", new RmCommand());
        register("remove", new RmCommand());
        register("save", new SaveCommand());
        register("cp", new CopyCommand());
        register("copy", new CopyCommand());
        register("rn", new RenameCommand());
        register("rename", new RenameCommand());

        // Job / Process commands
        register("ps", new PsCommand());
        register("submit", new SubmitCommand());
        register("cancel", new CancelCommand());
        register("p", new PurgeCommand());
        register("purge", new PurgeCommand());
        register("stop", new StopCommand());

        // Connection / Environment
        register("cd", new CdCommand());
        register("pwd", new PwdCommand());
        register("connections", new ConnectionsCommand());
        register("change", new ChangeCommand());

        // Terminal / Shell
        register("t", new TimeoutCommand());
        register("timeout", new TimeoutCommand());
        register("cls", new ClearCommand());
        register("clear", new ClearCommand());
        register("color", new ColorCommand());
        register("env", new EnvCommand());
        register("history", new HistoryCommand());
        register("hostname", new HostnameCommand());
        register("whoami", new WhoamiCommand());
        register("uname", new UnameCommand());
        register("set", new SetCommand());

        // Search / Help
        register("search", new SearchCommand());
        register("h", new HelpCommand());
        register("help", new HelpCommand());

        // User management
        register("usermod", new UsermodCommand());

        // TSO / USS / MVS
        register("tso", new TsoCommand());
        register("uss", new UssCommand());
        register("mvs", new MvsCommand());
        register("echo", new EchoCommand());
        register("files", new FilesCommand());
        register("count", new CountCommand());
    }

    private void register(String name, CommandHandler handler) {
        commands.put(name.toLowerCase(), handler);
    }

    public CommandHandler get(String name) {
        return commands.get(name.toLowerCase());
    }

}

