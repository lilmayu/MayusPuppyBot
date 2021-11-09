package dev.mayuna.puppy.console.commands;

import dev.mayuna.puppy.console.commands.generic.GenericConsoleCommand;
import dev.mayuna.puppy.console.ConsoleManager;
import dev.mayuna.puppy.util.MayoLogger;

public class HelpConsoleCommand extends GenericConsoleCommand {

    public HelpConsoleCommand() {
        this.name = "help";
    }

    @Override
    public void execute(String arguments) {
        MayoLogger.info("=== Commands (" + ConsoleManager.getConsoleCommands().size() + ") ===");
        for (var consoleCommand : ConsoleManager.getConsoleCommands()) {
            MayoLogger.info("- " + consoleCommand.name);
        }
    }
}
