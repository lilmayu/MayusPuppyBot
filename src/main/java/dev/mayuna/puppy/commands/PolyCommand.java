package dev.mayuna.puppy.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.SlashCommand;
import dev.mayuna.mayusjdautils.utils.DiscordUtils;
import dev.mayuna.mayusjdautils.utils.MessageInfo;
import dev.mayuna.puppy.managers.PolyGamesManager;
import dev.mayuna.puppy.objects.PolyGame;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;

public class PolyCommand {

    public static void register(CommandClientBuilder client) {
        client.addSlashCommands(new PolySlashCommand());
    }

    private static class PolyPrefixCommand extends Command {

        public PolyPrefixCommand() {
            this.name = "polytopia";

            this.aliases = new String[]{"poly"};
        }

        @Override
        protected void execute(CommandEvent commandEvent) {

        }
    }

    private static class PolySlashCommand extends SlashCommand {

        public PolySlashCommand() {
            this.name = "polytopia";

            this.children = new SlashCommand[]{new CreateGameSubCommand(), new EditGameSubCommand(), new MovePlayerSubCommand(), new RemoveSubCommand()};
        }

        @Override
        protected void execute(SlashCommandEvent slashCommandEvent) {

        }

        private static class RemoveSubCommand extends SlashCommand {

            public RemoveSubCommand() {
                this.name = "remove";

                List<OptionData> options = new ArrayList<>();
                options.add(new OptionData(OptionType.STRING, "edit_by_game_id", "Selects Poly Game by its Game ID", false));
                options.add(new OptionData(OptionType.STRING, "edit_by_message_id", "Selects Poly Game by its Message ID", false));
                this.options = options;
            }

            @Override
            protected void execute(SlashCommandEvent event) {
                event.deferReply(true).complete();
                InteractionHook hook = event.getHook();

                PolyGame polyGame = null;

                if (event.getOption("edit_by_game_id") != null) {
                    polyGame = PolyGamesManager.getPolyGameByGameId(event.getOption("edit_by_game_id").getAsString());
                }

                if (event.getOption("edit_by_message_id") != null) {
                    polyGame = PolyGamesManager.getPolyGameByMessageID(Long.parseLong(event.getOption("edit_by_message_id").getAsString()));
                }

                if (polyGame == null) {
                    hook.editOriginalEmbeds(MessageInfo.errorEmbed("Cannot find specified Poly Game or no Poly Game was specified!").build()).queue();
                    return;
                }

                if (PolyGamesManager.removeGame(polyGame)) {
                    PolyGamesManager.save();

                    hook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully removed selected Poly Game.").build()).queue();
                } else {
                    hook.editOriginalEmbeds(MessageInfo.errorEmbed("There was an error while removing Poly Game!").build()).queue();
                }
            }
        }

        private static class MovePlayerSubCommand extends SlashCommand {

            public MovePlayerSubCommand() {
                this.name = "move-player";

                List<OptionData> options = new ArrayList<>();
                options.add(new OptionData(OptionType.STRING, "from", "From place", true));
                options.add(new OptionData(OptionType.STRING, "to", "To place", true));
                options.add(new OptionData(OptionType.STRING, "edit_by_game_id", "Selects Poly Game by its Game ID", false));
                options.add(new OptionData(OptionType.STRING, "edit_by_message_id", "Selects Poly Game by its Message ID", false));
                this.options = options;
            }

            @Override
            protected void execute(SlashCommandEvent event) {
                event.deferReply(true).complete();
                InteractionHook hook = event.getHook();

                PolyGame polyGame = null;

                if (event.getOption("edit_by_game_id") != null) {
                    polyGame = PolyGamesManager.getPolyGameByGameId(event.getOption("edit_by_game_id").getAsString());
                }

                if (event.getOption("edit_by_message_id") != null) {
                    polyGame = PolyGamesManager.getPolyGameByMessageID(Long.parseLong(event.getOption("edit_by_message_id").getAsString()));
                }

                if (polyGame == null) {
                    hook.editOriginalEmbeds(MessageInfo.errorEmbed("Cannot find specified Poly Game or no Poly Game was specified!").build()).queue();
                    return;
                }

                int from, to;
                try {
                    from = Integer.parseInt(event.getOption("from").getAsString());
                    to = Integer.parseInt(event.getOption("to").getAsString());
                } catch (NumberFormatException ignored) {
                    hook.editOriginalEmbeds(MessageInfo.errorEmbed("Invalid numbers!").build()).queue();
                    return;
                }

                if (polyGame.movePlayer(from, to)) {
                    polyGame.send();
                    PolyGamesManager.save();

                    hook.editOriginalEmbeds(MessageInfo.successEmbed("Successfully moved players.").build()).queue();
                } else {
                    hook.editOriginalEmbeds(MessageInfo.errorEmbed("There was an error while moving players! Please, check your numbers.").build()).queue();
                }
            }
        }

        private static class EditGameSubCommand extends SlashCommand {

            public EditGameSubCommand() {
                this.name = "edit";

                List<OptionData> options = new ArrayList<>();
                options.add(new OptionData(OptionType.STRING, "edit_by_game_id", "Selects Poly Game by its Game ID", false));
                options.add(new OptionData(OptionType.STRING, "edit_by_message_id", "Selects Poly Game by its Message ID", false));
                options.add(new OptionData(OptionType.STRING, "game_id", "Game ID", false));
                options.add(new OptionData(OptionType.STRING, "winner", "Winner", false));

                OptionData mapSizeOption = new OptionData(OptionType.STRING, "map_size", "Map size", false);
                for (PolyGame.MapSize mapSize : PolyGame.MapSize.values()) {
                    mapSizeOption.addChoice(mapSize.name(), mapSize.getTiles() + "");
                }
                options.add(mapSizeOption);

                for (int x = 1; x <= 16; x++) {
                    options.add(new OptionData(OptionType.USER, "add_user_" + x, "User to add", false));
                }

                this.options = options;
            }

            @Override
            protected void execute(SlashCommandEvent event) {
                event.deferReply(true).complete();
                InteractionHook hook = event.getHook();

                PolyGame polyGame = null;

                if (event.getOption("edit_by_game_id") != null) {
                    polyGame = PolyGamesManager.getPolyGameByGameId(event.getOption("edit_by_game_id").getAsString());
                }

                if (event.getOption("edit_by_message_id") != null) {
                    polyGame = PolyGamesManager.getPolyGameByMessageID(Long.parseLong(event.getOption("edit_by_message_id").getAsString()));
                }

                if (polyGame == null) {
                    hook.editOriginalEmbeds(MessageInfo.errorEmbed("Cannot find specified Poly Game or no Poly Game was specified!").build()).queue();
                    return;
                }

                String madeChanges = "";

                if (event.getOption("game_id") != null) {
                    polyGame.setGameId(event.getOption("game_id").getAsString());
                    madeChanges += "Changed Game ID to `" + polyGame.getGameId() + "`\n";
                }

                if (event.getOption("winner") != null) {
                    polyGame.setWinner(event.getOption("winner").getAsString());
                    madeChanges += "Changed Winner to `" + polyGame.getWinner() + "`\n";
                }

                if (event.getOption("map_size") != null) {
                    polyGame.setMapSize(PolyGame.MapSize.get(event.getOption("map_size").getAsString()));
                    madeChanges += "Changed Map Size to `" + polyGame.getMapSize().name() + "`\n";
                }

                String addedUsers = "";

                for (int x = 1; x <= 16; x++) {
                    OptionMapping optionMapping = event.getOption("add_user_" + x);
                    if (optionMapping != null) {
                        polyGame.addPlayer(optionMapping.getAsUser());
                        addedUsers += optionMapping.getAsUser().getAsMention() + "; ";
                    }
                }

                if (!addedUsers.equals("")) {
                    madeChanges += "Addde user(s): " + addedUsers;
                }

                if (!madeChanges.equals("")) {
                    polyGame.send();
                    PolyGamesManager.save();

                    MessageInfo.Builder.create()
                            .setType(MessageInfo.Type.INFORMATION)
                            .setEmbed(true)
                            .setCustomTitle("Poly Game Edit (`" + polyGame.getGameId() + "`)")
                            .addCustomField(new MessageEmbed.Field("Changes", madeChanges, false))
                            .send(hook);
                } else {
                    hook.editOriginalEmbeds(MessageInfo.errorEmbed("You haven't changed anything!").build()).queue();
                }
            }
        }

        private static class CreateGameSubCommand extends SlashCommand {

            public CreateGameSubCommand() {
                this.name = "create";

                List<OptionData> options = new ArrayList<>();
                options.add(new OptionData(OptionType.STRING, "game_id", "Game ID", false));

                OptionData mapSizeOption = new OptionData(OptionType.STRING, "map_size", "Map size", false);
                for (PolyGame.MapSize mapSize : PolyGame.MapSize.values()) {
                    mapSizeOption.addChoice(mapSize.name(), mapSize.getTiles() + "");
                }
                options.add(mapSizeOption);

                for (int x = 1; x <= 16; x++) {
                    options.add(new OptionData(OptionType.USER, "user_" + x, "User to add", false));
                }

                this.options = options;
            }

            @Override
            protected void execute(SlashCommandEvent event) {
                event.deferReply(true).complete();

                String gameId = "N/A";
                PolyGame.MapSize mapSize = PolyGame.MapSize.UNKNOWN;

                if (event.getOption("game_id") != null) {
                    gameId = event.getOption("game_id").getAsString();
                }

                if (event.getOption("map_size") != null) {
                    mapSize = PolyGame.MapSize.get(event.getOption("map_size").getAsString());
                }

                List<User> players = new ArrayList<>();
                for (int x = 1; x <= 16; x++) {
                    OptionMapping optionMapping = event.getOption("user_" + x);
                    if (optionMapping != null) {
                        players.add(optionMapping.getAsUser());
                    }
                }

                Message message = event.getTextChannel().sendMessageEmbeds(DiscordUtils.getDefaultEmbed().build()).complete();
                PolyGame polyGame = new PolyGame(message, gameId, mapSize, players);
                polyGame.send();

                PolyGamesManager.addGame(polyGame);
                PolyGamesManager.save();

                event.getHook().editOriginalEmbeds(MessageInfo.successEmbed("Successfully created Polytopia Game " + gameId + "!").build()).queue();
            }
        }
    }
}
