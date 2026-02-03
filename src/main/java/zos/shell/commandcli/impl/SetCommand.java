package zos.shell.commandcli.impl;

import org.apache.commons.cli.CommandLine;
import zos.shell.commandcli.CommandContext;
import zos.shell.commandcli.NoOptionCommand;
import zos.shell.controller.container.ControllerFactoryContainerHolder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SetCommand extends NoOptionCommand {

    // Regex: KEY = VALUE (optional spaces around =), exactly one equals
    // KEY: letters, numbers, underscores, starts with letter or underscore
    // VALUE: anything except '='
    private static final Pattern KEY_VALUE_PATTERN = Pattern.compile("^([A-Za-z_][A-Za-z0-9_]*)\\s*=\\s*([^=]*)$");

    @Override
    protected String name() {
        return "set <KEY=VALUE>";
    }

    @Override
    protected String description() {
        return "Set an environment variable in the form KEY=VALUE. " +
                "KEY is converted to uppercase and spaces around '=' are allowed.";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        var args = cmd.getArgList();

        if (args.isEmpty()) {
            printHelp(ctx);
            return;
        }

        String kv = String.join(" ", args).trim();
        Matcher matcher = KEY_VALUE_PATTERN.matcher(kv);

        if (!matcher.matches()) {
            ctx.terminal.println("Invalid syntax. Expected KEY=VALUE (exactly one '=' allowed)");
            printHelp(ctx);
            return;
        }

        // Normalize key and value
        String key = matcher.group(1).toUpperCase();
        String value = matcher.group(2).trim();

        var env = ControllerFactoryContainerHolder
                .container()
                .getEnvVariableController();

        ctx.terminal.println(env.set(key + "=" + value));
    }
}
