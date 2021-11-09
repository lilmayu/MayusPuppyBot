package dev.mayuna.puppy.console.commands;

import dev.mayuna.puppy.console.commands.generic.GenericConsoleCommand;
import dev.mayuna.puppy.util.Config;
import dev.mayuna.puppy.util.MayoLogger;

public class ReloadConsoleCommand extends GenericConsoleCommand {

    public ReloadConsoleCommand() {
        this.name = "reload";
    }

    @Override
    public void execute(String arguments) {
        MayoLogger.info("Reloading...");

        Config.load();
    }
}
