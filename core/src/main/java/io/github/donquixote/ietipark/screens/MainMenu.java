package io.github.donquixote.ietipark.screens;

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
    private TextField taskInput;

    public MainMenu(DonQuixote game) {
        this.game = game;

        stage = new Stage(this.game.viewport);

        Table root = new Table();
        root.setFillParent(true);
        root.top().pad(20);
        stage.addActor(root);

        // Títol
        Label title = new Label("IETI Park", this.game.skin, "big");
        root.add(title).colspan(2).padBottom(20);
        root.row();

        // Entrada del nom
        taskInput = new TextField("", this.game.skin);
        taskInput.setMessageText("Entra el teu nom d'usuari");
        root.add(taskInput).expandX().fillX().padRight(10);
        root.row();

        // Connectar amb el servidor
        TextButton addButton = new TextButton("Jugar!", this.game.skin, "small");
        root.add(addButton).width(120).height(40);
        root.row();

    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);

        game.viewport.apply();
        game.batch.setProjectionMatrix(game.viewport.getCamera().combined);

        game.batch.begin();

        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {
        game.viewport.update(width, height, true);
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

    }
}
