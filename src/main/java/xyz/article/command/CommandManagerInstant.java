package xyz.article.command;

import xyz.article.api.command.CommandManager;
import xyz.article.api.command.CommandName;
import xyz.article.api.command.CommandSender;
import xyz.article.api.command.Command;

import java.util.ArrayList;
import java.util.List;

public class CommandManagerInstant implements CommandManager {
    private final List<CommandName> commands = new ArrayList<>();

    @Override
    public void executeCommand(String commandName, CommandSender sender, String[] args, String textBoard) {
        boolean found = false;
        for (CommandName command : commands) {
            if (command.isThisCommand(commandName)) {
                command.getCommand().execute(sender, args, textBoard);
                found = true;
            }
        }
        if (!found) sender.sendMessage("未知的指令！");
    }
    
    /**
     * 注册命令
     * @param name 命令名称
     * @param command 命令实例
     */
    public void registerCommand(String name, Command command) {
        commands.add(new CommandName(false, name, command));
    }
}
