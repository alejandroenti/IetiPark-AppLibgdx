package io.github.donquixote.ietipark.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.ScreenUtils;

import io.github.donquixote.ietipark.DonQuixote;

public class MainMenu  implements Screen {

    final DonQuixote game;

    private Stage stage;
    private TextField playerNameInput;

    public MainMenu(DonQuixote game) {
        this.game = game;

        stage = new Stage(this.game.viewport);

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        // Taula per a tenir el contingut centrat
        Table content = new Table();
        content.setBackground(this.game.skin.getDrawable("window"));
        content.pad(40);

        // Títol
        Label title = new Label("IETI Park", this.game.skin, "title");
        content.add(title).padBottom(32).row();

        // Entrada del nom
        playerNameInput = new TextField("", this.game.skin);
        playerNameInput.setMessageText("Entra el teu nom d'usuari");
        content.add(playerNameInput).width(500).padBottom(24).row();

        // Connectar amb el servidor
        TextButton addButton = new TextButton("Jugar!", this.game.skin);
        content.add(addButton).width(220).height(110).row();

        root.add(content);

        Gdx.input.setInputProcessor(stage);
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
}
