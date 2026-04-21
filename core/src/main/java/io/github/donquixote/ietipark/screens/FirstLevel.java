package io.github.donquixote.ietipark.screens;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;

import io.github.donquixote.ietipark.DonQuixote;
import io.github.donquixote.ietipark.configuration.GameObject;
import io.github.donquixote.ietipark.configuration.GameObjectMessage;
import io.github.donquixote.ietipark.configuration.MessageParser;
import io.github.donquixote.ietipark.configuration.PlayerMessage;

public class FirstLevel implements Screen, IScreen {
    private final DonQuixote game;

    private ArrayList<GameObject> gameObjects;

    private Texture backgroundTexture;
    private Texture characterTexture;
    private Texture keyTexture;
    private Texture doorTexture;
    private Texture touchpadBgTexture;
    private Texture touchpadKnobTexture;
    private Texture jumpBtnTexture;

    TextureRegion[][] characterRegion;
    TextureRegion[][] keyRegion;
    TextureRegion[][] doorRegion;
    TextureRegion characterFrame;
    TextureRegion keyFrame;
    TextureRegion doorFrame;

    private Stage uiStage;
    private Touchpad touchpad;
    private ImageButton jumpButton;

    private int dir;
    private boolean jumpPressed;

    public FirstLevel(DonQuixote game) {
        this.game = game;
        gameObjects = new ArrayList<>();

        backgroundTexture = new Texture(Gdx.files.internal("mvp/background.png"));
        characterTexture = new Texture(Gdx.files.internal("mvp/quixote_1.png"));
        keyTexture = new Texture(Gdx.files.internal("mvp/key.png"));
        doorTexture = new Texture(Gdx.files.internal("mvp/door.png"));

        characterRegion = TextureRegion.split(characterTexture, characterTexture.getWidth(), characterTexture.getHeight());
        keyRegion = TextureRegion.split(keyTexture, keyTexture.getWidth(), keyTexture.getHeight());
        doorRegion = TextureRegion.split(doorTexture, doorTexture.getWidth(), doorTexture.getHeight());

        characterFrame = characterRegion[0][0];
        keyFrame = keyRegion[0][0];
        doorFrame = doorRegion[0][0];

        for (String name : this.game.config.players) {
            gameObjects.add(new GameObject(name, characterFrame, 0, 80, 96, 96));
        }
        gameObjects.add(new GameObject("key", keyFrame, 100, 100, 64, 64));
        gameObjects.add(new GameObject("door", doorFrame, 736, 0, 96, this.game.viewport.getWorldHeight()));

        float density = Gdx.graphics.getDensity();
        int bgSize = (int) (36 * density);
        int knobSize = (int) (24 * density);
        int padSize = (int) (36 * density);
        int padMargin = (int) (2 * density);

        touchpadBgTexture = createCircleTexture(bgSize, new Color(0.3f, 0.3f, 0.3f, 0.5f));
        touchpadKnobTexture = createCircleTexture(knobSize, new Color(0.7f, 0.7f, 0.7f, 0.8f));
        jumpBtnTexture = createCircleTexture(knobSize, new Color(0.7f, 0.7f, 0.7f, 0.8f));

        Drawable touchpadBg = new TextureRegionDrawable(new TextureRegion(touchpadBgTexture));
        Drawable touchpadKnob = new TextureRegionDrawable(new TextureRegion(touchpadKnobTexture));
        Drawable jumpBtn = new TextureRegionDrawable(new TextureRegion(jumpBtnTexture));

        Touchpad.TouchpadStyle touchpadStyle = new Touchpad.TouchpadStyle();
        touchpadStyle.background = touchpadBg;
        touchpadStyle.knob = touchpadKnob;

        touchpad = new Touchpad(5 * density, touchpadStyle);
        touchpad.setBounds(padMargin, padMargin, padSize, padSize);

        ImageButton.ImageButtonStyle jumpStyle = new ImageButton.ImageButtonStyle();
        jumpStyle.imageUp = jumpBtn;
        jumpButton = new ImageButton(jumpStyle);
        int jumpBtnSize = (int) (knobSize * 1.5f);
        jumpButton.setBounds(
            this.game.viewport.getWorldWidth() - padMargin - jumpBtnSize,
            padMargin,
            jumpBtnSize,
            jumpBtnSize
        );
        jumpButton.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                jumpPressed = true;
            }
        });

        uiStage = new Stage(this.game.viewport);
        uiStage.addActor(touchpad);
        uiStage.addActor(jumpButton);
        Gdx.input.setInputProcessor(uiStage);

        dir = 0;
        jumpPressed = false;
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        input();
        draw();
        if (uiStage != null) {
            uiStage.act(Gdx.graphics.getDeltaTime());
            uiStage.draw();
        }
    }

    @Override
    public void resize(int width, int height) {
        this.game.viewport.update(width, height, true);
        if (uiStage != null) {
            uiStage.getViewport().update(width, height, true);
        }
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        backgroundTexture.dispose();
        characterTexture.dispose();
        keyTexture.dispose();
        doorTexture.dispose();

        if (uiStage != null) {
            uiStage.dispose();
            touchpadBgTexture.dispose();
            touchpadKnobTexture.dispose();
            jumpBtnTexture.dispose();
        }
    }

    @Override
    public void handleMessage(String message) {
        MessageParser.ParsedMessage parsed = MessageParser.parse(message);

        switch (parsed.type) {
            case "GAME STATE":
                handleGameState(MessageParser.parseGameObjects(parsed.payload));
                break;
            case "PLAYERS":
                handlePlayers(MessageParser.parsePlayers(parsed.payload));
                break;
            default:
                break;
        }
    }

    private Texture createCircleTexture(int diameter, Color color) {
        Pixmap pixmap = new Pixmap(diameter, diameter, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fillCircle(diameter / 2, diameter / 2, diameter / 2 - 1);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private void input() {
        // Read from touchpad on Android
        float knobX = touchpad != null ? touchpad.getKnobPercentX() : 0;

        if (knobX > 0.2f) {
            if (dir != 1) {
                dir = 1;
                game.ws.send("{\"type\": \"MOVE\", \"payload\": \"RIGHT\"}");
            }
        } else if (knobX < -0.2f) {
            if (dir != -1) {
                dir = -1;
                game.ws.send("{\"type\": \"MOVE\", \"payload\": \"LEFT\"}");
            }
        } else {
            if (dir != 0) {
                dir = 0;
                game.ws.send("{\"type\": \"MOVE\", \"payload\": \"NONE\"}");
            }
        }

        if (jumpPressed) {
            jumpPressed = false;
            game.ws.send("{\"type\": \"JUMP\", \"payload\": null}");
        }
    }

    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        this.game.viewport.apply();
        this.game.batch.setProjectionMatrix(this.game.viewport.getCamera().combined);

        this.game.batch.begin();

        this.game.batch.draw(backgroundTexture, 0, 0, this.game.viewport.getWorldWidth(), this.game.viewport.getWorldHeight());
        for (GameObject go : gameObjects) {
            this.game.batch.draw(go.getTexture(), go.getPosX(), go.getPosY(), go.getDimenX(), go.getDimenY());
        }

        this.game.batch.end();
    }

    private void handleGameState(GameObjectMessage[] gameObjectsMsg) {
        for (GameObjectMessage gom : gameObjectsMsg) {
            for (GameObject go : gameObjects) {
                if (gom.name.equals(go.getName())) {
                    go.setPosX(gom.posX);
                    go.setPosY(gom.posY);
                }
            }
        }
    }

    private void handlePlayers(PlayerMessage[] players) {
        ArrayList<String> playerNames = new ArrayList<>();

        for (GameObject go : gameObjects) {
            playerNames.add(go.getName());
        }

        game.config.players.clear();
        for (PlayerMessage player : players) {
            game.config.players.add(player.name);

            if (!playerNames.contains(player.name)) {
                gameObjects.add(new GameObject(player.name, characterFrame, 0, 0, 128, 128));
            }
        }
    }
}
