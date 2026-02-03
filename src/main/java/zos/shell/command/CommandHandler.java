package zos.shell.command;

public interface CommandHandler {

    void execute(CommandContext ctx, String input);

}
