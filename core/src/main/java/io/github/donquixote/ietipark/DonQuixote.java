package io.github.donquixote.ietipark;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.github.czyzby.websocket.WebSocket;
import com.github.czyzby.websocket.WebSockets;

import io.github.donquixote.ietipark.configuration.GameConfiguration;
import io.github.donquixote.ietipark.infrastructure.WebSocketClient;
import io.github.donquixote.ietipark.screens.MainMenu;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class DonQuixote extends Game {
    public SpriteBatch batch;
    public FitViewport viewport;
    public Skin skin;
    public WebSocket ws;
    public GameConfiguration config;

    @Override
    public void create() {
        batch = new SpriteBatch();
        skin = new Skin(Gdx.files.internal("skin/golden-ui-skin.json"));
        viewport = new FitViewport(1024, 512);

        this.setScreen(new MainMenu(this));
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        batch.dispose();
        skin.dispose();
    }

    public void initializeWebSocketServer() {
        ws = WebSockets.newSocket(WebSockets.toWebSocketUrl("10.0.2.2", 3000));
        //ws = WebSockets.newSocket("wss://pico1.ieti.site");
        ws.setSendGracefully(false);
        ws.addListener(new WebSocketClient(this));
        ws.connect();
    }
}
