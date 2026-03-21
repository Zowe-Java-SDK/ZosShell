package zos.shell.command.impl;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.command.CommandContext;
import zos.shell.command.NoOptionCommand;
import zos.shell.controller.container.ControllerFactoryContainerHolder;

import java.util.regex.Pattern;

public class SetCommand extends NoOptionCommand {

    private static final Logger LOG = LoggerFactory.getLogger(SetCommand.class);

    private static final Pattern KEY_PATTERN =
            Pattern.compile("^[A-Za-z_][A-Za-z0-9_]*$");

    @Override
    protected String name() {
        return "set <KEY=VALUE>";
    }

    @Override
    protected String description() {
        return "Set an environment variable in the form KEY=VALUE. " +
                "Everything after '=' is treated as the value. " +
                "KEY is converted to uppercase and spaces around '=' are allowed.";
    }

    @Override
    protected void run(final CommandContext ctx, final CommandLine cmd) {
        LOG.debug("*** SetCommand.run ***");

        var args = cmd.getArgList();
        if (args.isEmpty()) {
            printHelp(ctx);
            return;
        }

        String input = String.join(" ", args).trim();
        int equalsIndex = input.indexOf('=');

        if (equalsIndex < 0 || equalsIndex != input.lastIndexOf('=')) {
            ctx.out("Invalid syntax. Expected KEY=VALUE (exactly one '=' allowed)");
            printHelp(ctx);
            return;
        }

        String key = input.substring(0, equalsIndex).trim();
        String value = input.substring(equalsIndex + 1).trim();

        if (key.isEmpty() || !KEY_PATTERN.matcher(key).matches()) {
            ctx.out("Invalid key. Key must start with a letter or underscore and " +
                    "contain only letters, numbers, or underscores.");
            printHelp(ctx);
            return;
        }

        if (hasUnmatchedQuotes(value)) {
            ctx.out("Invalid value. Unmatched quotes detected.");
            return;
        }

        value = stripOuterMatchingQuotes(value);

        var env = ControllerFactoryContainerHolder
                .container()
                .getEnvVariableController();

        ctx.out(env.set(key, value));
    }

    private boolean hasUnmatchedQuotes(final String value) {
        LOG.debug("*** SetCommand.hasUnmatchedQuotes ***");
        boolean inSingle = false;
        boolean inDouble = false;

        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);

            if (ch == '\'' && !inDouble) {
                inSingle = !inSingle;
            } else if (ch == '"' && !inSingle) {
                inDouble = !inDouble;
            }
        }

        return inSingle || inDouble;
    }

    private String stripOuterMatchingQuotes(final String value) {
        LOG.debug("*** SetCommand.stripOuterMatchingQuotes ***");
        if (value.length() >= 2) {
            if ((value.startsWith("\"") && value.endsWith("\"")) ||
                    (value.startsWith("'") && value.endsWith("'"))) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }

}
