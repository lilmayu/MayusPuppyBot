package dev.mayuna.puppy.console.commands;

import dev.mayuna.puppy.console.commands.generic.GenericConsoleCommand;
import dev.mayuna.puppy.util.Config;
import dev.mayuna.puppy.util.MayoLogger;

public class SaveConsoleCommand extends GenericConsoleCommand {

    public SaveConsoleCommand() {
        this.name = "save";
    }

    @Override
    public void execute(String arguments) {
        MayoLogger.info("Saving...");

        Config.save();
    }
}
