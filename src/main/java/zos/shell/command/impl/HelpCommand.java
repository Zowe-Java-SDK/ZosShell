package zos.shell.command.impl;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.command.CommandContext;
import zos.shell.command.CommandHandler;
import zos.shell.command.NoOptionCommand;
import zos.shell.singleton.CommandRegistrySingleton;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class HelpCommand extends NoOptionCommand {

    private static final Logger LOG = LoggerFactory.getLogger(HelpCommand.class);

    private Map<String, CommandHandler> commands;

    private CommandContext ctx;

    @Override
    protected String name() {
        return "help";
    }

    @Override
    protected String[] aliases() {
        return new String[]{"h"};
    }

    @Override
    protected String description() {
        return "Show help for commands";
    }

    @Override
    protected String usage() {
        return "help [COMMAND_NAME]...";
    }

    @Override
    protected void run(CommandContext ctx, CommandLine cmd) {
        LOG.debug("*** HelpCommand.run ***");
        this.commands = CommandRegistrySingleton.getInstance().getRegistry();
        this.ctx = ctx;
        if (cmd.getArgList().isEmpty()) {
            printAllCommands();
        } else {
            printCommandHelp(cmd);
        }
    }

    private void printAllCommands() {
        commands.keySet().stream()
                .sorted()
                .forEach(ctx.terminal::println);
    }

    private void printCommandHelp(CommandLine cmd) {
        AtomicInteger count = new AtomicInteger(1);
        AtomicInteger size = new AtomicInteger(cmd.getArgList().size());
        cmd.getArgList().forEach(c -> {
            if (commands.containsKey(c)) {
                commands.get(c).printHelp(ctx);
                System.out.println(size.get() + " " + count.get());
                if (size.get() != count.get()) {
                    ctx.terminal.println();
                }
                count.incrementAndGet();
            }
        });
    }

}
