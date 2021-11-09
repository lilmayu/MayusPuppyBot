package dev.mayuna.puppy.console;

import dev.mayuna.mayusjdautils.arguments.ArgumentParser;
import dev.mayuna.puppy.console.commands.*;
import dev.mayuna.puppy.console.commands.generic.GenericConsoleCommand;
import dev.mayuna.puppy.util.MayoLogger;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class ConsoleManager {

    // Data
    private static @Getter final List<GenericConsoleCommand> consoleCommands = new ArrayList<>();

    // Runtime
    private static @Getter Thread commandThread;

    public static void init() {
        consoleCommands.add(new HelpConsoleCommand());

        consoleCommands.add(new StopConsoleCommand());
        consoleCommands.add(new ReloadConsoleCommand());
        consoleCommands.add(new SaveConsoleCommand());

        consoleCommands.add(new TestWriteConsoleCommand());

        startCommandThread();
    }

    private static void processCommand(String command) {
        if (command == null) {
            return;
        }

        ArgumentParser argumentParser = new ArgumentParser(command);

        if (!argumentParser.hasAnyArguments()) {
            MayoLogger.error("Unknown command.");
            return;
        }

        String name = argumentParser.getArgumentAtIndex(0).getValue();
        String arguments = "";

        if (argumentParser.hasArgumentAtIndex(1)) {
            arguments = argumentParser.getAllArgumentsAfterIndex(1).getValue();
        }

        for (GenericConsoleCommand genericConsoleCommand : consoleCommands) {
            if (genericConsoleCommand.name.equalsIgnoreCase(name)) {
                try {
                    genericConsoleCommand.execute(arguments);
                } catch (Exception exception) {
                    exception.printStackTrace();
                    MayoLogger.error("Exception occurred while executing command '" + command + "'!");
                }
                return;
            }
        }

        MayoLogger.error("Unknown command.");
    }

    private static void startCommandThread() {
        commandThread = new Thread(() -> {
            while (true) {
                String command = System.console().readLine();
                processCommand(command);
            }
        });
        commandThread.start();
    }
}
