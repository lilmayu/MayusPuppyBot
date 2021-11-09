package dev.mayuna.puppy.console.commands;

import dev.mayuna.puppy.console.commands.generic.GenericConsoleCommand;
import dev.mayuna.puppy.util.MayoLogger;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public class TestWriteConsoleCommand extends GenericConsoleCommand {

    public TestWriteConsoleCommand() {
        this.name = "test-write";
    }

    @Override
    public void execute(String arguments) {
        File ytFolder = new File("./_yt/");
        MayoLogger.info("Information: " + ytFolder.isDirectory());

        File file = new File(ytFolder.getPath() + "/test.txt");
        try {
            FileOutputStream fis = new FileOutputStream(file);
            fis.write(arguments.getBytes(StandardCharsets.UTF_8));
            MayoLogger.info("Wrote: " + arguments);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
