package dev.mayuna.puppy.managers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.mayuna.mayusjsonutils.JsonUtil;
import dev.mayuna.mayusjsonutils.objects.MayuJson;
import dev.mayuna.puppy.objects.PolyGame;
import dev.mayuna.puppy.util.MayoLogger;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class PolyGamesManager {

    public static final String DATA_FILE = "./data/poly_games.json";

    private static @Getter List<PolyGame> gameList = new ArrayList<>();

    public static void addGame(PolyGame polyGame) {
        gameList.add(polyGame);
    }

    public static boolean removeGame(PolyGame polyGame) {
        if (gameList.remove(polyGame)) {
            polyGame.getManagedMessage().getMessage().delete().queue();
            return true;
        }

        return false;
    }

    public static PolyGame getPolyGameByMessageID(long messageId) {
        for (PolyGame polyGame : gameList) {
            if (polyGame.getManagedMessage().getMessage().getIdLong() == messageId) {
                return polyGame;
            }
        }

        return null;
    }

    public static PolyGame getPolyGameByGameId(String gameId) {
        for (PolyGame polyGame : gameList) {
            if (polyGame.getGameId().equalsIgnoreCase(gameId)) {
                return polyGame;
            }
        }

        return null;
    }

    public static void load() {
        gameList.clear();

        try {
            MayuJson mayuJson = JsonUtil.createOrLoadJsonFromFile(DATA_FILE);
            JsonArray jsonArray = mayuJson.getOrCreate("games", new JsonArray()).getAsJsonArray();

            for (JsonElement jsonElement : jsonArray) {
                if (jsonElement.isJsonObject()) {
                    PolyGame polyGame = new PolyGame(jsonElement.getAsJsonObject());
                    polyGame.send();

                    gameList.add(polyGame);
                }
            }

            mayuJson.saveJson();
            MayoLogger.success("Loaded " + gameList.size() + " Poly Games!");
        } catch (Exception exception) {
            exception.printStackTrace();
            MayoLogger.error("Exception occurred while loading " + DATA_FILE + "!");
        }
    }

    public static void save() {
        try {
            MayuJson mayuJson = JsonUtil.createOrLoadJsonFromFile(DATA_FILE);
            JsonObject jsonObject = new JsonObject();
            JsonArray jsonArray = new JsonArray();

            for (PolyGame polyGame : gameList) {
                jsonArray.add(polyGame.toJsonObject());
            }
            jsonObject.add("games", jsonArray);

            mayuJson.setJsonObject(jsonObject);
            mayuJson.saveJson();

            MayoLogger.success("Saved " + jsonArray.size() + " Poly Games!");
        } catch (Exception exception) {
            exception.printStackTrace();
            MayoLogger.error("Exception occurred while saving " + DATA_FILE + "!");
        }

    }
}
