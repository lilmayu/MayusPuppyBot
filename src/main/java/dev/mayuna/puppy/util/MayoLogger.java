package dev.mayuna.puppy.util;

import dev.mayuna.mayuslibrary.logging.Logger;
import dev.mayuna.puppy.util.types.*;

public class MayoLogger {

    public static void info(String text) {
        Logger.info(text);
    }

    public static void warning(String text) {
        Logger.warning(text);
    }

    public static void error(String text) {
        Logger.error(text);
    }

    public static void debug(String text) {
        if (Config.isDebug()) {
            Logger.debug(text);
        }
    }

    public static void trace(String text) {
        Logger.trace(text);
    }

    public static void success(String text) {
        Logger.custom(new SuccessLogType(), text);
    }

    public static void fatal(String text) {
        Logger.custom(new FatalLogType(), text);
    }

    public static void command(String text) {
        Logger.custom(new CommandLogType(), text);
    }

    public static void server(String text) {
        Logger.custom(new ServerLogType(), text);
    }

    public static void client(String text) {
        Logger.custom(new ClientLogType(), text);
    }
}
