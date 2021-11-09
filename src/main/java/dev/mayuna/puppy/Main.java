package dev.mayuna.puppy;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import dev.mayuna.mayusjdautils.data.MayuCoreListener;
import dev.mayuna.mayusjdautils.utils.DiscordUtils;
import dev.mayuna.mayusjdautils.utils.MessageInfo;
import dev.mayuna.mayuslibrary.console.colors.Color;
import dev.mayuna.mayuslibrary.console.colors.Colors;
import dev.mayuna.mayuslibrary.exceptionreporting.ExceptionListener;
import dev.mayuna.mayuslibrary.exceptionreporting.ExceptionReporter;
import dev.mayuna.mayuslibrary.logging.Log;
import dev.mayuna.mayuslibrary.logging.Logger;
import dev.mayuna.mayuslibrary.logging.coloring.ColoringString;
import dev.mayuna.puppy.commands.YoutubeCommand;
import dev.mayuna.puppy.console.ConsoleManager;
import dev.mayuna.puppy.listeners.CommandListener;
import dev.mayuna.puppy.util.Config;
import dev.mayuna.puppy.util.Constants;
import dev.mayuna.puppy.util.MayoLogger;
import dev.mayuna.puppy.util.PlatformType;
import dev.mayuna.puppy.util.types.CommandLogType;
import dev.mayuna.puppy.util.types.FatalLogType;
import dev.mayuna.puppy.util.types.SuccessLogType;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

public class Main {

    // Discord
    private static @Getter JDA jda;
    private static @Getter CommandClientBuilder client;

    // Runtime
    private static boolean configLoaded = false;
    private static @Getter long bootTime;

    public static void main(String[] args) {
        MayoLogger.info("Puppy - Mayu's Discord Bot");
        MayoLogger.info("Made by Mayuna (mayuna#8016)");
        MayoLogger.info("Starting up...");

        long startTime = System.currentTimeMillis();

        MayoLogger.info("Loading library settings...");
        loadLibrarySettings();

        MayoLogger.info("Loading config...");
        if (!Config.load()) {
            MayoLogger.fatal("There was fatal error while loading Config! Cannot proceed.");
            return;
        }
        configLoaded = true;

        MayoLogger.info("Loading ConsoleManager...");
        ConsoleManager.init();

        MayoLogger.info("Loading JDA stuff...");
        client = new CommandClientBuilder().useDefaultGame()
                .useHelpBuilder(false)
                .setOwnerId(String.valueOf(Config.getOwnerID()))
                .setActivity(Activity.listening("Mayuna"))
                .setPrefix(Config.getPrefix())
                .setAlternativePrefix(Constants.ALTERNATIVE_PREFIX)
                .setPrefixes(new String[]{"<@!891625740758560780>"})
                .setListener(new CommandListener());

        MayoLogger.info("Loading commands...");
        loadCommands();

        MayoLogger.info("Logging into Discord...");
        try {
            JDABuilder jdaBuilder = JDABuilder.createDefault(Config.getToken())
                    .addEventListeners(client.build())
                    .addEventListeners(new MayuCoreListener())
                    .enableIntents(GatewayIntent.GUILD_PRESENCES)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .enableIntents(GatewayIntent.DIRECT_MESSAGES)
                    .enableIntents(GatewayIntent.DIRECT_MESSAGE_REACTIONS)
                    .setMemberCachePolicy(MemberCachePolicy.ALL);
            jda = jdaBuilder.build().awaitReady();
        } catch (Exception exception) {
            exception.printStackTrace();
            MayoLogger.error("Error occurred while logging into Discord! Please, check your bot token in " + Constants.CONFIG_PATH + "!");
            System.exit(-1);
        }
        MayoLogger.success("Logged in!");

        MayoLogger.info("Finishing up modules...");

        MayoLogger.info("Loading managers...");
        loadManagers();

        bootTime = (System.currentTimeMillis() - startTime);
        MayoLogger.success("Loading done! Took " + bootTime + "ms");
    }

    private static void loadCommands() {
        client.addSlashCommands(new YoutubeCommand());
    }

    private static void loadManagers() {

    }

    private static void loadLibrarySettings() {
        ExceptionReporter.registerExceptionReporter();
        ExceptionReporter.getInstance().addListener(new ExceptionListener("default", "mayuna", exceptionReport -> {
            exceptionReport.getThrowable().printStackTrace();

            if (configLoaded) {
                MayoLogger.error("Exception occurred! Sending it to Puppy's exception Message channel.");

                if (Main.getJda() != null && Config.getExceptionMessageChannelID() != 0) {
                    MessageChannel messageChannel = Main.getJda().getTextChannelById(Config.getExceptionMessageChannelID());
                    if (messageChannel != null) {
                        MessageInfo.sendExceptionMessage(messageChannel, exceptionReport.getThrowable());
                    } else {
                        MayoLogger.error("Unable to send exception to Exception message channel! (Invalid ExceptionMessageChannelID)");
                    }
                } else {
                    MayoLogger.error("Unable to send exception to Exception message channel! (JDA is null / ExceptionMessageChannelID is not set)");
                }
            }

        }));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (configLoaded) {
                MayoLogger.info("Shutting down...");

                Config.save();

                MayoLogger.info("o/");
            }
        }));

        Logger.reflectionDepth = 5;
        Logger.setFormat("# [{type}][{time}][{method}]{prefix} {symbol} -> {text}");
        Log.setCustomFormatConsumer((log, string) -> {
            switch (log.getBaseLogType().getName()) {
                case "INFO" : {
                    string = string.replace("{symbol}", "❯");
                }
                case "WARNING" : {
                    string = string.replace("{symbol}", "⚠");
                }
                case "ERROR" : {
                    string = string.replace("{symbol}", "✖");
                }
                case "DEBUG" : {
                    string = string.replace("{symbol}", "…");
                }
                case "SUCCESS" : {
                    string = string.replace("{symbol}", "✔");
                }
                case "FATAL" : {
                    string = string.replace("{symbol}", "✖ FATAL");
                }
                default : {
                    string = string.replace("{symbol}", "…");
                }
            }

            return string;
        });

        Logger.addColoringString(new ColoringString(new CommandLogType(), new Color().setForeground(Colors.DARK_GRAY).build(), Color.RESET));
        Logger.addColoringString(new ColoringString(new SuccessLogType(), new Color().setForeground(Colors.LIGHT_GREEN).build(), Color.RESET));
        Logger.addColoringString(new ColoringString(new FatalLogType(), new Color().setForeground(Colors.RED).changeBold(true).build(), Color.RESET));
        DiscordUtils.setDefaultEmbed(new EmbedBuilder().setFooter("Powered by Puppy").setColor(new java.awt.Color(0xDAACFF)).setDescription("Loading..."));
        MayuCoreListener.enableExperimentalInteractionBehavior = true;
        MessageInfo.useSystemEmotes = true;

        Config.init();

    }
}