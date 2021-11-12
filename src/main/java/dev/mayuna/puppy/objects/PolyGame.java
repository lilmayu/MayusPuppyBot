package dev.mayuna.puppy.objects;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.mayuna.mayusjdautils.managed.ManagedMessage;
import dev.mayuna.mayusjdautils.utils.DiscordUtils;
import dev.mayuna.mayusjsonutils.data.Savable;
import dev.mayuna.mayusjsonutils.objects.MayuJson;
import dev.mayuna.puppy.Main;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PolyGame implements Savable {

    private @Getter @Setter ManagedMessage managedMessage;

    private @Getter @Setter String gameId;
    private @Getter @Setter String winner = "N/A";
    private @Getter @Setter MapSize mapSize;
    private @Getter @Setter List<User> players;

    public PolyGame(JsonObject jsonObject) {
        fromJsonObject(jsonObject);
    }

    public PolyGame(Message message, String gameId, MapSize mapSize) {
        this(message, gameId, mapSize, new ArrayList<>());
    }

    public PolyGame(Message message, String gameId, MapSize mapSize, List<User> players) {
        this.gameId = gameId;
        this.mapSize = mapSize;
        this.players = players;

        managedMessage = new ManagedMessage("poly_game_" + gameId + "__" + UUID.randomUUID(), message.getGuild(), message.getChannel(), message);
    }

    public boolean addPlayer(User player) {
        if (players.contains(player)) {
            return false;
        }

        players.add(player);
        return true;
    }

    public boolean movePlayer(int positionFrom, int positionTo) {
        if (positionFrom < 0 || positionTo < 0 || positionFrom > players.size() || positionTo > players.size()) {
            return false;
        }

        User userFrom = players.get(positionFrom - 1);
        User userTo = players.get(positionTo - 1);

        players.set(positionTo - 1, userFrom);
        players.set(positionFrom - 1, userTo);

        return true;
    }

    public String getSafeGameId() {
        if (gameId.length() >= 8) {
            return gameId.substring(0, 8);
        }

        return gameId;
    }

    public void send() {
        managedMessage.sendOrEditMessage(new MessageBuilder().setEmbeds(createGameEmbed().build()));
    }

    public EmbedBuilder createGameEmbed() {
        EmbedBuilder embedBuilder = DiscordUtils.getDefaultEmbed();

        embedBuilder.setTitle("Polytopia Game (`" + managedMessage.getMessageID() + "`)");
        embedBuilder.setDescription("**Game ID**: `" + gameId + "`\n" + "**Map Size**: " + mapSize.name() + "(" + mapSize.tiles + ")\n" + "**No. Players**: " + players.size() + "\n" + "**Winner**: " + winner);

        embedBuilder.addField(getPlayersField());
        embedBuilder.setFooter("Version 1.0");

        return embedBuilder;
    }

    public MessageEmbed.Field getPlayersField() {
        String content = "";

        int counter = 1;
        for (User player : players) {
            content += "`[" + counter + "]` " + player.getAsMention() + "\n";
            counter++;
        }

        if (content.equals("")) {
            content += "No players.";
        }

        return new MessageEmbed.Field("Players", content, false);
    }

    @Override
    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.add("managedMessage", managedMessage.toJsonObject());
        jsonObject.addProperty("winner", winner);
        jsonObject.addProperty("gameId", gameId);
        jsonObject.addProperty("mapSize", mapSize.name());

        JsonArray jsonArray = new JsonArray();
        for (User player : players) {
            jsonArray.add(player.getIdLong());
        }
        jsonObject.add("playersId", jsonArray);

        return jsonObject;
    }

    @Override
    public void fromJsonObject(JsonObject jsonObject) {
        MayuJson mayuJson = new MayuJson(jsonObject);

        managedMessage = new ManagedMessage(mayuJson.getOrCreate("managedMessage", new JsonObject()).getAsJsonObject());
        winner = mayuJson.getOrCreate("winner", new JsonPrimitive("N/A")).getAsString();
        gameId = mayuJson.getOrCreate("gameId", new JsonPrimitive("N/A")).getAsString();
        mapSize = MapSize.get(mayuJson.getOrCreate("mapSize", new JsonPrimitive(MapSize.UNKNOWN.name())).getAsString());

        managedMessage.updateEntries(Main.getJda());

        List<User> players = new ArrayList<>();
        for (JsonElement jsonElement : mayuJson.getOrCreate("playersId", new JsonArray()).getAsJsonArray()) {
            if (jsonElement.isJsonPrimitive()) {
                long userId = jsonElement.getAsLong();
                User user = Main.getJda().retrieveUserById(userId).complete();

                players.add(user);
            }
        }
        this.players = players;
    }

    public enum MapSize {

        TINY(121), SMALL(196), NORMAL(256), LARGE(324), HUGE(400), MASSIVE(900), UNKNOWN(-1);

        private final @Getter int tiles;

        MapSize(int tiles) {
            this.tiles = tiles;
        }

        public static MapSize get(String nameOrTiles) {
            try {
                return MapSize.valueOf(nameOrTiles);
            } catch (Exception ignored) {
            }

            switch (nameOrTiles) {
                case "121" -> {
                    return MapSize.TINY;
                }
                case "196" -> {
                    return MapSize.SMALL;
                }
                case "256" -> {
                    return MapSize.NORMAL;
                }
                case "324" -> {
                    return MapSize.LARGE;
                }
                case "400" -> {
                    return MapSize.HUGE;
                }
                case "900" -> {
                    return MapSize.MASSIVE;
                }
            }

            return null;
        }
    }
}
