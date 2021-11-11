package dev.mayuna.puppy.listeners;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import dev.mayuna.mayusjdautils.utils.MessageInfo;
import dev.mayuna.mayuslibrary.exceptionreporting.ExceptionReporter;
import dev.mayuna.puppy.util.MayoLogger;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandListener implements com.jagrosh.jdautilities.command.CommandListener {

    @Override
    public void onCommand(CommandEvent event, Command command) {
        MayoLogger.command("PrefixCommand @ " + event.getResponseNumber());
        MayoLogger.command("- Name: '" + command.getName() + "'; Arguments: '" + event.getArgs() + "'; Full message: '" + event.getMessage().getContentRaw() + "'");
        MayoLogger.command("- Author: " + event.getAuthor());
        MayoLogger.command("- ChannelType: " + event.getChannelType().name());
        if (event.isFromType(ChannelType.TEXT)) {
            MayoLogger.command("- Guild: '" + event.getGuild() + "' @ " + event.getChannel());
        }
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event, SlashCommand command) {
        MayoLogger.command("SlashCommand @ " + event.getResponseNumber());
        MayoLogger.command("- Name: '" + command.getName() + "'; Full: '" + event.getCommandString() + "'");
        MayoLogger.command("- Author: " + event.getUser());
        MayoLogger.command("- ChannelType: " + event.getChannelType().name());
        if (event.getChannelType() == ChannelType.TEXT) {
            MayoLogger.command("- Guild: '" + event.getGuild() + "' @ " + event.getChannel());
        }
    }

    @Override
    public void onCompletedCommand(CommandEvent event, Command command) {
        MayoLogger.command("PrefixCommand Completed @ " + event.getResponseNumber());
    }

    @Override
    public void onCompletedSlashCommand(SlashCommandEvent event, SlashCommand command) {
        MayoLogger.command("SlashCommand Completed @ " + event.getResponseNumber());
    }

    @Override
    public void onTerminatedCommand(CommandEvent event, Command command) {
        MayoLogger.command("PrefixCommand Terminated @ " + event.getResponseNumber());
    }

    @Override
    public void onTerminatedSlashCommand(SlashCommandEvent event, SlashCommand command) {
        MayoLogger.command("SlashCommand Terminated @ " + event.getResponseNumber());
    }

    @Override
    public void onCommandException(CommandEvent event, Command command, Throwable throwable) {
        MayoLogger.warning("PrefixCommand Exception @ " + event.getResponseNumber());
        MayoLogger.warning(" - Full message: '" + event.getMessage().getContentRaw() + "'");
        MayoLogger.warning(" - Please, see errors below.");

        try {
            MessageInfo.Builder.create()
                    .setType(MessageInfo.Type.ERROR)
                    .setEmbed(true)
                    .setClosable(true)
                    .setCloseAfterSeconds(10)
                    .addOnInteractionWhitelist(event.getAuthor())
                    .setContent("There was an exception while processing " + event.getAuthor().getAsMention() + "'s prefix command `" + event.getMessage().getContentRaw() + "`.\nThis will be automatically reported. Sorry for inconvenience and please, try again.")
                    .addCustomField(new MessageEmbed.Field("Technical details", MessageInfo.formatExceptionInformationField(throwable), false))
                    .send(event.getChannel());

            ExceptionReporter.getInstance().uncaughtException(Thread.currentThread(), throwable);
        } catch (Exception exception) {
            exception.printStackTrace();
            MayoLogger.warning("Exception occurred while editing original message from command which resulted in exception! Probably safe to ignore.");
        }
    }

    @Override
    public void onSlashCommandException(SlashCommandEvent event, SlashCommand command, Throwable throwable) {
        MayoLogger.warning("SlashCommand Exception @ " + event.getResponseNumber());
        MayoLogger.warning(" - Full: '" + event.getCommandString() + "'");
        MayoLogger.warning(" - Please, see errors below.");

        try {
            MessageInfo.Builder.create()
                    .setType(MessageInfo.Type.ERROR)
                    .setEmbed(true)
                    .setCloseAfterSeconds(10)
                    .setContent("There was an exception while processing " + event.getInteraction().getUser() + "'s slash command `" + event.getCommandString() + "`.\nThis will be automatically reported. Sorry for inconvenience and please, try again.")
                    .addCustomField(new MessageEmbed.Field("Technical details", MessageInfo.formatExceptionInformationField(throwable), false))
                    .send(event.getHook());

            ExceptionReporter.getInstance().uncaughtException(Thread.currentThread(), throwable);
        } catch (Exception exception) {
            exception.printStackTrace();
            MayoLogger.warning("Exception occurred while editing original message from command which resulted in exception! Probably safe to ignore.");
        }
    }

    @Override
    public void onNonCommandMessage(MessageReceivedEvent event) {
        //Main.getMessageWaiterManager().processEvent(event);
    }
}
