package zos.shell.singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zos.shell.command.CommandHandler;

import java.util.HashMap;
import java.util.Map;

public final class CommandRegistrySingleton {

    private static final Logger LOG = LoggerFactory.getLogger(CommandRegistrySingleton.class);

    public static Map<String, CommandHandler> commands = new HashMap<>();

    private static class Holder {
        private static final CommandRegistrySingleton instance = new CommandRegistrySingleton();
    }

    public static CommandRegistrySingleton getInstance() {
        LOG.debug("*** getInstance ***");
        return CommandRegistrySingleton.Holder.instance;
    }

    public Map<String, CommandHandler> getRegistry() {
        LOG.debug("*** get ***");
        return commands;
    }

    public void set(Map<String, CommandHandler> commands) {
        LOG.debug("*** register ***");
        CommandRegistrySingleton.commands = commands;
    }

}
