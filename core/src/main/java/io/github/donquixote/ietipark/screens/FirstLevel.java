package io.github.donquixote.ietipark.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.Json;

import io.github.donquixote.ietipark.DonQuixote;
import io.github.donquixote.ietipark.configuration.JsonMessage;

public class FirstLevel implements Screen, IScreen {

    private final DonQuixote game;

    public FirstLevel(DonQuixote game) {
        this.game = game;
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {

    }

    @Override
    public void resize(int width, int height) {

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

    @Override
    public void handleMessage(String message) {
        Json msg = new Json();
        JsonMessage jsonMessage = msg.fromJson(JsonMessage.class, message);

        switch (jsonMessage.type) {
            case "ACCEPTED JOIN":

                break;
            case "REFUSED JOIN":

                break;
            default:
                throw new AssertionError();
        }
    }
}
