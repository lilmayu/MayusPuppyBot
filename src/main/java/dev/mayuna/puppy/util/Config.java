package dev.mayuna.puppy.util;

import com.google.gson.JsonPrimitive;
import dev.mayuna.mayusjsonutils.JsonUtil;
import dev.mayuna.mayusjsonutils.objects.MayuJson;
import dev.mayuna.mayuslibrary.logging.LogPrefix;
import dev.mayuna.mayuslibrary.logging.Logger;
import lombok.Getter;
import lombok.Setter;

public class Config {

    private static @Getter @Setter String prefix = "u!";
    private static @Getter @Setter String token = "### YOUR TOKEN HERE ###";
    private static @Getter @Setter long exceptionMessageChannelID = 0;
    private static @Getter @Setter long ownerID = 0;
    private static @Getter @Setter boolean debug = false;

    public static void init() {
        Logger.addLogPrefix(new LogPrefix("[Config]", Config.class));
    }

    public static boolean load() {
        try {
            MayuJson mayuJson = JsonUtil.createOrLoadJsonFromFile(Constants.CONFIG_PATH);

            prefix = mayuJson.getOrCreate("prefix", new JsonPrimitive(prefix)).getAsString();
            token = mayuJson.getOrCreate("token", new JsonPrimitive(token)).getAsString();
            exceptionMessageChannelID = mayuJson.getOrCreate("exceptionMessageChannelID", new JsonPrimitive(exceptionMessageChannelID)).getAsLong();
            ownerID = mayuJson.getOrCreate("ownerID", new JsonPrimitive(ownerID)).getAsLong();
            debug = mayuJson.getOrCreate("debug", new JsonPrimitive(debug)).getAsBoolean();

            mayuJson.saveJson();

            MayoLogger.success("Config loading done!");
            MayoLogger.debug("- Using prefix: " + prefix);
            MayoLogger.debug("- Owner ID: " + ownerID);
            MayoLogger.debug("- Exception MSG Channel: " + exceptionMessageChannelID);
            MayoLogger.debug("- Debug: " + debug);

            return true;
        } catch (Exception exception) {
            exception.printStackTrace();
            MayoLogger.fatal("Error occurred while loading config from path " + Constants.CONFIG_PATH + "!");
            return false;
        }
    }

    public static void save() {
        try {
            MayuJson mayuJson = JsonUtil.createOrLoadJsonFromFile(Constants.CONFIG_PATH);

            mayuJson.add("prefix", prefix);
            mayuJson.add("token", token);
            mayuJson.add("exceptionMessageChannelID", exceptionMessageChannelID);
            mayuJson.add("ownerID", ownerID);
            mayuJson.add("debug", debug);

            mayuJson.saveJson();

            MayoLogger.success("Successfully saved config!");
        } catch (Exception exception) {
            exception.printStackTrace();
            MayoLogger.fatal("Error occurred while saving config to path " + Constants.CONFIG_PATH + "!");
        }
    }
}
