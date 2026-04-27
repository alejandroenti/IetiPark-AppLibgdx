package io.github.donquixote.ietipark.configuration;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public class MessageParser {

    private static final JsonReader reader = new JsonReader();

    public static ParsedMessage parse(String raw) {
        JsonValue root = reader.parse(raw);
        ParsedMessage msg = new ParsedMessage();
        msg.type    = root.getString("type", null);
        msg.payload = root.get("payload");
        return msg;
    }

    public static PlayerMessage[] parsePlayers(JsonValue payload) {
        if (payload == null || payload.isNull()) return new PlayerMessage[0];
        PlayerMessage[] players = new PlayerMessage[payload.size];
        int i = 0;
        for (JsonValue p : payload) {
            PlayerMessage pm = new PlayerMessage();
            pm.id   = p.getString("id",   null);
            pm.name = p.getString("name", null);
            players[i++] = pm;
        }
        return players;
    }

    public static GameObjectMessage[] parseGameObjects(JsonValue payload) {
        if (payload == null || payload.isNull()) return new GameObjectMessage[0];
        JsonValue players = payload.get("players");
        if (players == null || players.isNull()) return new GameObjectMessage[0];
        GameObjectMessage[] gameObjects = new GameObjectMessage[players.size];
        int i = 0;
        for (JsonValue p : players) {
            GameObjectMessage gom = new GameObjectMessage();
            gom.name             = p.getString("name", null);
            gom.posX             = p.getFloat("x", 0f);
            gom.posY             = p.getFloat("y", 0f);
            gom.isMovingLeft     = p.getBoolean("isMovingLeft", false);
            gom.isMovingRight    = p.getBoolean("isMovingRight", false);
            gom.isJumping        = p.getBoolean("isJumping", false);
            gom.hasKey           = p.getBoolean("hasKey", false);
            gom.hasCompletedLevel = p.getBoolean("hasCompletedLevel", false);
            gameObjects[i++] = gom;
        }
        return gameObjects;
    }

    public static class ParsedMessage {
        public String    type;
        public JsonValue payload;
    }
}
