package xyz.article.api.console;

import xyz.article.api.command.CommandSender;

public interface ConsoleCommandSender extends CommandSender {
    void performCommand (String s);
}
