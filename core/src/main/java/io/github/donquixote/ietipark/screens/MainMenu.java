package io.github.donquixote.ietipark.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import io.github.donquixote.ietipark.DonQuixote;
import io.github.donquixote.ietipark.configuration.LevelStateMessage;
import io.github.donquixote.ietipark.configuration.MessageParser;
import io.github.donquixote.ietipark.configuration.PlayerMessage;

public class MainMenu implements Screen, IScreen {

    private final DonQuixote game;

    private Stage stage;
    private TextField playerNameInput;
    private Table playerListTable;

    public MainMenu(DonQuixote game) {
        this.game = game;

        stage = new Stage(new ExtendViewport(800, 480));

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        // ── Panell esquerre: títol + formulari ──────────────────────────
        Table leftPanel = new Table();
        leftPanel.setBackground(this.game.skin.getDrawable("window"));
        leftPanel.pad(40).top();

        Label title = new Label("IETI Park", this.game.skin, "title");
        leftPanel.add(title).padBottom(24).row();

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
        rightPanel.top();

        // Títol del panell
        Label playersTitle = new Label("Jugadors connectats", this.game.skin, "title");
        rightPanel.add(playersTitle).padTop(24).padBottom(12).row();

        // Scroll amb el nom dels jugadors
        playerListTable = new Table();
        playerListTable.top().left();

        ScrollPane scrollPane = new ScrollPane(playerListTable, this.game.skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        rightPanel.add(scrollPane).expand().fill().pad(8);

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
        MessageParser.ParsedMessage parsed = MessageParser.parse(message);
        switch (parsed.type) {
            case "ACCEPTED JOIN":
                handleJoin();
                break;
            case "REFUSED JOIN":
                handleRefusedJoin();
                break;
            case "PLAYERS":
                handlePlayers(MessageParser.parsePlayers(parsed.payload));
                break;
            case "GAME STATE":
                handleLevelState(MessageParser.parseLevelState(parsed.payload));
                break;
            default:
                break;
        }
    }

    private void handleJoin() {
        Gdx.app.postRunnable(() -> game.setScreen(new FirstLevel(game)));
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

    private void handlePlayers(PlayerMessage[] players) {
        game.config.players.clear();
        for (PlayerMessage player : players) {
            game.config.players.add(player.name);
        }
        Gdx.app.postRunnable(() -> {
            playerListTable.clearChildren();
            for (String name : game.config.players) {
                Table row = new Table();
                row.setBackground(game.skin.getDrawable("textfield"));
                Label nameLabel = new Label(name, game.skin);
                row.add(nameLabel).expandX().left().pad(8, 16, 8, 16);
                playerListTable.add(row).expandX().fillX().padBottom(6).row();
            }
        });
    }

    private void handleLevelState(LevelStateMessage levelState) {
        if (levelState == null) return;

        switch (levelState.name) {
            case "first_level":
                game.setScreen(new FirstLevel(game));
                break;
            case "second_level":
                game.setScreen(new SecondLevel(game));
                break;
            default:
                break;
        }
    }
}
