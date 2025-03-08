package xyz.article.api.command;

public class CommandName {
    private final boolean is_alias;
    private final String name;
    private final Command command;

    public CommandName(boolean isAlias, String name, Command command) {
        this.is_alias = isAlias;
        this.name = name;
        this.command = command;
    }

    public boolean isAlias () {
        return is_alias;
    }

    public boolean isThisCommand (String commandName) {
        return name.equals(commandName);
    }

    public String getName () {
        return name;
    }

    public Command getCommand() {
        return command;
    }
}
