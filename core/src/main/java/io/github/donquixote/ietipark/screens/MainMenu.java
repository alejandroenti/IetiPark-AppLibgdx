package io.github.donquixote.ietipark.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ScreenUtils;

import io.github.donquixote.ietipark.DonQuixote;
import io.github.donquixote.ietipark.configuration.JsonMessage;
import io.github.donquixote.ietipark.configuration.PlayerMessage;

public class MainMenu implements Screen, IScreen {

    private final DonQuixote game;

    private Stage stage;
    private TextField playerNameInput;
    private List<String> playerList;
    private Array<String> playerNames;

    public MainMenu(DonQuixote game) {
        this.game = game;

        stage = new Stage(this.game.viewport);

        playerNames = new Array<>();

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        // ── Panell esquerre: títol + formulari ──────────────────────────
        Table leftPanel = new Table();
        leftPanel.setBackground(this.game.skin.getDrawable("window"));
        leftPanel.pad(40).top();

        Label title = new Label("IETI Park", this.game.skin, "title");
        leftPanel.add(title).padBottom(32).row();

        playerNameInput = new TextField("", this.game.skin);
        playerNameInput.setMessageText("Entra el teu nom d'usuari");
        leftPanel.add(playerNameInput).width(300).padBottom(24).row();

        TextButton playButton = new TextButton("Jugar!", this.game.skin);
        leftPanel.add(playButton).width(220).height(100).row();
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.config.name = playerNameInput.getText().trim();
                playerNameInput.setText("");
                game.ws.send("{\"type\": \"JOIN\", \"payload\": \"" + game.config.name + "\"}");
            }
        });

        // ── Panell dret: llista de jugadors ─────────────────────────────
        Table rightPanel = new Table();
        rightPanel.setBackground(this.game.skin.getDrawable("window"));
        rightPanel.pad(24).top();

        Label playersTitle = new Label("Jugadors", this.game.skin);
        rightPanel.add(playersTitle).padBottom(16).row();

        playerList = new List<>(this.game.skin);
        playerList.setItems(playerNames);

        ScrollPane scrollPane = new ScrollPane(playerList, this.game.skin);
        scrollPane.setFadeScrollBars(false);
        rightPanel.add(scrollPane).expand().fill();

        // ── Layout principal ─────────────────────────────────────────────
        root.add(leftPanel).expandY().fillY().width(420).padRight(16);
        root.add(rightPanel).expand().fill();

        Gdx.input.setInputProcessor(stage);

        game.initializeWebSocketServer();
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    @Override
    public void handleMessage(String message) {
        Json msg = new Json();
        JsonMessage jsonMessage = msg.fromJson(JsonMessage.class, message);

        switch (jsonMessage.type) {
            case "ACCEPTED JOIN":
                handleJoin();
                break;
            case "REFUSED JOIN":
                handleRefusedJoin();
                break;
            case "PLAYERS":
                handlePlayers(jsonMessage.payload);
                break;
            default:
                throw new AssertionError();
        }
    }

    private void handleJoin() {
        game.setScreen(new FirstLevel(game));
    }

    private void handleRefusedJoin() {
        game.ws.close();
        Gdx.app.postRunnable(() -> {
            Dialog dialog = new Dialog("Error de connexió", this.game.skin) {
                @Override
                protected void result(Object object) {
                    hide();
                }
            };
            dialog.text("Nom d'usuari no disponible.\nTorna-ho a intentar.");
            dialog.button("D'acord");
            dialog.show(stage);
        });
    }

    private void handlePlayers(String payload) {
        Json msg = new Json();
        String[] players = msg.fromJson(String[].class, payload);
        game.config.players.clear();
        for (String player : players) {
            PlayerMessage pmsg = msg.fromJson(PlayerMessage.class, player);
            game.config.players.add(pmsg.name);
        }
        Gdx.app.postRunnable(() -> {
            playerNames.clear();
            for (String player : game.config.players) {
                playerNames.add(player);
            }
            playerList.setItems(playerNames);
        });
    }
}
