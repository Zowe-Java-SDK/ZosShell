package zos.shell.commandcli;

public interface CommandHandler {

    void execute(CommandContext ctx, String input);

}
