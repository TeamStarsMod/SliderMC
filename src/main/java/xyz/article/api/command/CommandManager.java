package xyz.article.api.command;

public interface CommandManager {
    void executeCommand (String commandName, CommandSender sender, String[] args, String textBoard);
}
