package dev.mayuna.puppy.console.commands.generic;

public abstract class GenericConsoleCommand {

    public String name;

    public abstract void execute(String arguments);

}
