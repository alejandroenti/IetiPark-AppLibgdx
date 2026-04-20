package io.github.donquixote.ietipark.configuration;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public class MessageParser {

    private static final JsonReader reader = new JsonReader();

    public static ParsedMessage parse(String raw) {
        JsonValue root = reader.parse(raw);
        ParsedMessage msg = new ParsedMessage();
        msg.type    = root.getString("type");
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

    public static class ParsedMessage {
        public String    type;
        public JsonValue payload;
    }
}
