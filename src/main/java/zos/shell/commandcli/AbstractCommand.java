package zos.shell.commandcli;

import org.apache.commons.cli.*;

import java.io.PrintWriter;
import java.util.Arrays;

public abstract class AbstractCommand implements CommandHandler {

    protected abstract String name();

    protected String[] aliases() {              // optional aliases
        return new String[0];
    }

    protected abstract String description();

    protected abstract Options options();

    protected abstract void run(CommandContext ctx, CommandLine cmd) throws Exception;

    @Override
    public final void execute(CommandContext ctx, String input) {
        try {
            String[] tokens = input.trim().split("\\s+");
            String[] args = Arrays.copyOfRange(tokens, 1, tokens.length);

            Options opts = options();

            // auto help flags
            opts.addOption("h", "help", false, "display help");

            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(opts, args);

            if (cmd.hasOption("h")) {
                printHelp(ctx);
                return;
            }

            run(ctx, cmd);

        } catch (ParseException e) {
            ctx.terminal.println(e.getMessage());
            printHelp(ctx);
        } catch (Exception e) {
            ctx.terminal.println(e.getMessage());
            e.printStackTrace();
        }
    }

    protected void printHelp(CommandContext ctx) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(120);

        // Create a PrintWriter that writes to your terminal
        PrintWriter writer = new PrintWriter(new TerminalWriter(ctx.terminal), true);

        formatter.printHelp(
                writer,
                120,
                name(),
                description(),
                options(),
                2,  // left padding
                2,          // desc padding
                null,
                true
        );
        writer.flush();
    }

}
