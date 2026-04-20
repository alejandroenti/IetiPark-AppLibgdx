package io.github.donquixote.ietipark.infrastructure;

import com.github.czyzby.websocket.WebSocket;
import com.github.czyzby.websocket.WebSocketListener;

import io.github.donquixote.ietipark.DonQuixote;
import io.github.donquixote.ietipark.screens.IScreen;

public class WebSocketClient implements WebSocketListener {

    private DonQuixote game;

    public WebSocketClient(DonQuixote game) {
        this.game = game;
    }

    @Override
    public boolean onOpen(WebSocket webSocket) {
        System.out.println("Opening...");
        game.config.players.add("xd");
        return false;
    }

    @Override
    public boolean onClose(WebSocket webSocket, int closeCode, String reason) {
        System.out.println("Closing...");
        return false;
    }

    @Override
    public boolean onMessage(WebSocket webSocket, String packet) {
        System.out.println("Message: " + packet);
        if (game.getScreen() instanceof IScreen) {
            ((IScreen) game.getScreen()).handleMessage(packet);
        }
        return false;
    }

    @Override
    public boolean onMessage(WebSocket webSocket, byte[] packet) {
        System.out.println("Message: " + packet);
        return false;
    }

    @Override
    public boolean onError(WebSocket webSocket, Throwable error) {
        System.out.println("ERROR: " + error.toString());
        return false;
    }
}
