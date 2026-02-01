package zos.shell.commandcli;

public interface CommandHandler {

    public void execute(CommandContext ctx, String input);

}
