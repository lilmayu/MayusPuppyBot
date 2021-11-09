package dev.mayuna.puppy.console.commands;

import dev.mayuna.puppy.console.commands.generic.GenericConsoleCommand;
import dev.mayuna.puppy.util.MayoLogger;

public class StopConsoleCommand extends GenericConsoleCommand {

    public StopConsoleCommand() {
        this.name = "stop";
    }

    @Override
    public void execute(String arguments) {
        MayoLogger.info("Stopping...");
        Runtime.getRuntime().exit(0);
    }
}
