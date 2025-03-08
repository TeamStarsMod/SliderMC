package xyz.article.api.command;

public abstract class Command {
    public abstract boolean execute (CommandSender sender, String[] args, String textBoard);
}
