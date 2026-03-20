package zos.shell.command;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.utility.PromptUtil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

/**
 * Base class for commands.
 * Provides clean usage, help support, and argument parsing via Apache Commons CLI.
 */
@SuppressWarnings("unused")
public abstract class AbstractCommand implements CommandHandler {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractCommand.class);

    /**
     * Command name, e.g., "ls"
     */
    protected abstract String name();

    /**
     * Optional command aliases, e.g., "bj" for "browsejob"
     */
    protected String[] aliases() {
        return new String[0];
    }

    /**
     * Short description for help output
     */
    protected abstract String description();

    /**
     * Command options
     */
    protected abstract Options options();

    /**
     * Main command logic
     */
    protected abstract void run(CommandContext ctx, CommandLine cmd);

    /**
     * Custom usage string (for full control over placeholders)
     */
    protected String usage() {
        return name(); // the default usage is just the command name
    }

    protected boolean stopOptionParsing() {
        return false;
    }

    @Override
    public final void execute(CommandContext ctx, String input) {
        LOG.debug("*** execute ***");
        ctx.store(PromptUtil.getPrompt() + " " + input);

        try {
            String[] tokens = input.trim().split("\\s+");
            String[] args = Arrays.copyOfRange(tokens, 1, tokens.length);

            Options opts = options();

            // auto help flag
            opts.addOption("h", "help", false, "display help");

            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(opts, args, this.stopOptionParsing());

            if (cmd.hasOption("h")) {
                printHelp(ctx);
                return;
            }

            run(ctx, cmd);

        } catch (ParseException e) {
            ctx.out("Error: " + e.getMessage());
            printHelp(ctx);
        } catch (Exception e) {
            ctx.out("Exception: " + e.getMessage());
            // Capture the full stack trace as a string
            var sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            // display the full trace in UI
            ctx.out(sw.toString());
        }
    }

    /**
     * Print help/usage cleanly
     */
    public void printHelp(CommandContext ctx) {
        LOG.debug("*** printHelp ***");
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(120);

        // terminal writer for Commons CLI
        TerminalWriter terminalWriter = new TerminalWriter(ctx.terminalOutputService);
        PrintWriter writer = new PrintWriter(terminalWriter, true);

        // print usage line using our custom usage() method
        ctx.out("usage: " + usage());

        // print the description of the command right after usage
        String desc = description();
        if (desc != null && !desc.isBlank()) {
            ctx.out(desc);
        }

        // print options if any
        if (!options().getOptions().isEmpty()) {
            formatter.printOptions(
                    writer,
                    formatter.getWidth(),
                    options(),
                    formatter.getLeftPadding(),
                    formatter.getDescPadding()
            );
        }

        writer.flush();
    }

}
