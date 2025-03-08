package xyz.article.console;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.article.MinecraftServer;
import xyz.article.api.console.ConsoleCommandSender;

public class ConsoleCommandSenderInstant implements ConsoleCommandSender {
    private static final Logger log = LoggerFactory.getLogger(ConsoleCommandSenderInstant.class);

    @Override
    public void performCommand(String s) {
        String[] strings = s.split(" ");
        String commandName = strings[0];
        String[] args = new String[strings.length - 1];
        System.arraycopy(strings, 1, args, 0, strings.length - 1);
        MinecraftServer.getServer().getCommandManager().executeCommand(commandName, this, args, s);
    }

    @Override
    public void sendMessage(String msg) {
        log.info(msg);
    }
}
