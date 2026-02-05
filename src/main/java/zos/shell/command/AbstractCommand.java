package zos.shell.command;

import org.apache.commons.cli.*;

import java.io.PrintWriter;
import java.util.Arrays;

/**
 * Base class for commands.
 * Provides clean usage, help support, and argument parsing via Apache Commons CLI.
 */
public abstract class AbstractCommand implements CommandHandler {

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
    protected abstract void run(CommandContext ctx, CommandLine cmd) throws Exception;

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
            ctx.terminal.println("Error: " + e.getMessage());
            printHelp(ctx);
        } catch (Exception e) {
            ctx.terminal.println("Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Print help/usage cleanly
     */
    public void printHelp(CommandContext ctx) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(120);

        // terminal writer for Commons CLI
        PrintWriter writer = new PrintWriter(new TerminalWriter(ctx.terminal), true);

        // print usage line using our custom usage() method
        ctx.terminal.println("usage: " + usage());

        // print the description of the command right after usage
        String desc = description();
        if (desc != null && !desc.isBlank()) {
            ctx.terminal.println(desc);
        }

        // print options if any
        if (!options().getOptions().isEmpty()) {
            formatter.printOptions(writer,
                    formatter.getWidth(),
                    options(),
                    formatter.getLeftPadding(),
                    formatter.getDescPadding());
        }

        writer.flush();
    }
}
