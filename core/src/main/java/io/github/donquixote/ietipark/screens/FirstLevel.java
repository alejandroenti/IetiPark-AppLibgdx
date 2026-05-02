package io.github.donquixote.ietipark.screens;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;

import io.github.donquixote.ietipark.DonQuixote;
import io.github.donquixote.ietipark.configuration.AnimatedGameObject;
import io.github.donquixote.ietipark.configuration.GameObject;
import io.github.donquixote.ietipark.configuration.GameObjectMessage;
import io.github.donquixote.ietipark.configuration.LevelLoader;
import io.github.donquixote.ietipark.configuration.LevelStateMessage;
import io.github.donquixote.ietipark.configuration.MessageParser;
import io.github.donquixote.ietipark.configuration.PlayerMessage;

public class FirstLevel implements Screen, IScreen {
    private final DonQuixote game;

    private static class TileLayer {
        Texture texture;
        int[][] tileMap;
        int tileW, tileH;
        float offsetX, offsetY;
    }

    private ArrayList<TileLayer> tileLayers;
    private HashMap<String, GameObject> gameObjectsByName;
    private Map<String, Texture> textureCache;
    private Map<String, LevelLoader.AnimationData> animations;
    private Map<String, LevelLoader.AnimationData> animationsByName;
    private LevelLoader.LevelData levelData;

    private Texture backgroundTexture;
    private GlyphLayout glyphLayout;
    private Texture touchpadBgTexture;
    private Texture touchpadKnobTexture;
    private Texture jumpBtnTexture;

    private Stage uiStage;
    private Touchpad touchpad;
    private ImageButton jumpButton;

    private int dir;
    private boolean jumpPressed;

    public FirstLevel(DonQuixote game) {
        this.game = game;
        gameObjectsByName = new HashMap<>();
        textureCache = new HashMap<>();

        // Load level data from JSON
        levelData = LevelLoader.loadLevel("first_level");
        if (levelData == null) {
            Gdx.app.error("FirstLevel", "Failed to load level 'prova'");
            return;
        }

        // Load animations from JSON
        animations = LevelLoader.loadAnimations();
        animationsByName = new HashMap<>();
        for (LevelLoader.AnimationData anim : animations.values()) {
            animationsByName.put(anim.name, anim);
        }

        // Load tile layers
        tileLayers = new ArrayList<>();
        if (levelData.layers != null) {
            for (LevelLoader.LayerData layerData : levelData.layers) {
                TileLayer tl = new TileLayer();
                if (textureCache.containsKey(layerData.tilesSheetFile)) {
                    tl.texture = textureCache.get(layerData.tilesSheetFile);
                } else {
                    tl.texture = new Texture(Gdx.files.internal("levels/" + layerData.tilesSheetFile));
                    textureCache.put(layerData.tilesSheetFile, tl.texture);
                }
                tl.tileMap = LevelLoader.loadTileMap(layerData.tileMapFile);
                tl.tileW = layerData.tilesWidth;
                tl.tileH = layerData.tilesHeight;
                tl.offsetX = layerData.x;
                tl.offsetY = layerData.y;
                tileLayers.add(tl);
            }
        }

        // Load sprites from the level data
        if (levelData.sprites != null) {
            for (LevelLoader.SpriteData spriteData : levelData.sprites) {
                loadSpriteFromData(spriteData);
            }
        }

        // Load player sprites
        for (String pName : game.config.players) {
            loadPlayersSprites(levelData.sprites.get(0), pName);
        }

        removePlayerPlaceholder();

        // Set up UI elements
        setupUI();

        // GlyphLayout for player name labels
        glyphLayout = new GlyphLayout();

        dir = 0;
        jumpPressed = false;
    }

    private void loadSpriteFromData(LevelLoader.SpriteData spriteData) {
        try {
            Texture spriteTexture;
            String texturePath = "levels/" + spriteData.imageFile;
            if (textureCache.containsKey(spriteData.imageFile)) {
                spriteTexture = textureCache.get(spriteData.imageFile);
            } else {
                spriteTexture = new Texture(Gdx.files.internal(texturePath));
                textureCache.put(spriteData.imageFile, spriteTexture);
            }

            float spriteY = this.game.viewport.getWorldHeight() - spriteData.y - spriteData.height;
            AnimatedGameObject gameObject = new AnimatedGameObject(
                spriteData.name,
                new TextureRegion(spriteTexture),
                spriteData.x,
                spriteY,
                spriteData.width,
                spriteData.height,
                AnimatedGameObject.GameObjectType.INTERACTABLE
            );

            // Load all animations into this instance
            addAllAnimationsTo(gameObject);

            // Play the animation assigned to this sprite
            if (gameObject.getName().equals("door")) {
                gameObject.playAnimation("door_animation");
                gameObject.setFrame(0);
            } else if (spriteData.animationId != null && animations.containsKey(spriteData.animationId)) {
                gameObject.playAnimation(animations.get(spriteData.animationId).name);
            }

            gameObjectsByName.put(gameObject.getName(), gameObject);
        } catch (Exception e) {
            Gdx.app.error("FirstLevel", "Failed to load sprite: " + spriteData.name, e);
        }
    }

    private void loadPlayersSprites(LevelLoader.SpriteData spriteData, String name) {
        try {
            Texture spriteTexture;
            String texturePath = "levels/" + spriteData.imageFile;
            if (textureCache.containsKey(spriteData.imageFile)) {
                spriteTexture = textureCache.get(spriteData.imageFile);
            } else {
                spriteTexture = new Texture(Gdx.files.internal(texturePath));
                textureCache.put(spriteData.imageFile, spriteTexture);
            }

            float spriteY = this.game.viewport.getWorldHeight() - spriteData.y - spriteData.height;
            AnimatedGameObject gameObject = new AnimatedGameObject(
                name,
                new TextureRegion(spriteTexture),
                spriteData.x,
                spriteY,
                spriteData.width,
                spriteData.height,
                AnimatedGameObject.GameObjectType.PLAYER
            );

            // Load all animations into this instance
            addAllAnimationsTo(gameObject);

            // Play the animation assigned to this sprite
            if (spriteData.animationId != null && animations.containsKey(spriteData.animationId)) {
                gameObject.playAnimation(animations.get(spriteData.animationId).name);
            }

            gameObjectsByName.put(gameObject.getName(), gameObject);
        } catch (Exception e) {
            Gdx.app.error("FirstLevel", "Failed to load sprite: " + spriteData.name, e);
        }
    }

    private void removePlayerPlaceholder() {
        gameObjectsByName.remove("quixote");
    }

    private void addAllAnimationsTo(AnimatedGameObject obj) {
        for (LevelLoader.AnimationData animData : animationsByName.values()) {
            try {
                Texture animTexture;
                if (textureCache.containsKey(animData.mediaFile)) {
                    animTexture = textureCache.get(animData.mediaFile);
                } else {
                    com.badlogic.gdx.files.FileHandle fh = Gdx.files.internal("levels/" + animData.mediaFile);
                    if (!fh.exists()) {
                        Gdx.app.error("FirstLevel", "Animation texture not found, skipping: " + animData.mediaFile);
                        continue;
                    }
                    animTexture = new Texture(fh);
                    textureCache.put(animData.mediaFile, animTexture);
                }
                int fw = animData.frameWidth  > 0 ? animData.frameWidth  : (int) obj.getDimenX();
                int fhv = animData.frameHeight > 0 ? animData.frameHeight : (int) obj.getDimenY();
                obj.addAnimation(animData.name, animTexture, fw, fhv,
                    animData.startFrame, animData.endFrame, animData.fps, animData.loop);
            } catch (Exception e) {
                Gdx.app.error("FirstLevel", "Failed to load animation: " + animData.name, e);
            }
        }
    }

    private void setupUI() {
        float worldHeight = this.game.viewport.getWorldHeight();
        float worldWidth  = this.game.viewport.getWorldWidth();

        int bgSize    = (int) (worldHeight * 0.14f);
        int knobSize  = (int) (worldHeight * 0.08f);
        int padSize   = (int) (worldHeight * 0.14f);
        int padMargin = (int) (worldHeight * 0.02f);

        touchpadBgTexture = createCircleTexture(bgSize, new Color(0.3f, 0.3f, 0.3f, 0.5f));
        touchpadKnobTexture = createCircleTexture(knobSize, new Color(0.7f, 0.7f, 0.7f, 0.8f));
        jumpBtnTexture = createCircleTexture(knobSize, new Color(0.7f, 0.7f, 0.7f, 0.8f));

        Drawable touchpadBg = new TextureRegionDrawable(new TextureRegion(touchpadBgTexture));
        Drawable touchpadKnob = new TextureRegionDrawable(new TextureRegion(touchpadKnobTexture));
        Drawable jumpBtn = new TextureRegionDrawable(new TextureRegion(jumpBtnTexture));

        Touchpad.TouchpadStyle touchpadStyle = new Touchpad.TouchpadStyle();
        touchpadStyle.background = touchpadBg;
        touchpadStyle.knob = touchpadKnob;

        touchpad = new Touchpad(padSize * 0.05f, touchpadStyle);
        touchpad.setBounds(padMargin, padMargin, padSize, padSize);

        ImageButton.ImageButtonStyle jumpStyle = new ImageButton.ImageButtonStyle();
        jumpStyle.imageUp = jumpBtn;
        jumpButton = new ImageButton(jumpStyle);
        int jumpBtnSize = (int) (knobSize * 1.5f);
        jumpButton.setBounds(
            worldWidth - padMargin - jumpBtnSize,
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
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        input();
        updateAnimations(delta);
        draw();
        if (uiStage != null) {
            uiStage.act(Gdx.graphics.getDeltaTime());
            uiStage.draw();
        }
    }

    private void updateAnimations(float delta) {
        for (GameObject go : gameObjectsByName.values()) {
            if (go instanceof AnimatedGameObject) {
                ((AnimatedGameObject) go).update(delta);
            }
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
        // Dispose all cached textures
        if (textureCache != null) {
            for (Texture texture : textureCache.values()) {
                texture.dispose();
            }
            textureCache.clear();
        }

        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }

        if (uiStage != null) {
            uiStage.dispose();
            if (touchpadBgTexture != null) touchpadBgTexture.dispose();
            if (touchpadKnobTexture != null) touchpadKnobTexture.dispose();
            if (jumpBtnTexture != null) jumpBtnTexture.dispose();
        }

        gameObjectsByName.clear();
    }

    @Override
    public void handleMessage(String message) {
        Gdx.app.postRunnable(() -> {
            MessageParser.ParsedMessage parsed = MessageParser.parse(message);
            if (parsed.type == null) return;

            switch (parsed.type) {
                case "GAME STATE":
                    handleGameState(MessageParser.parseGameObjects(parsed.payload));
                    handleLevelState(MessageParser.parseLevelState(parsed.payload));
                    break;
                case "PLAYERS":
                    handlePlayers(MessageParser.parsePlayers(parsed.payload));
                    break;
                default:
                    break;
            }
        });
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
        // Parse background color from level data
        Color bgColor = Color.BLACK;
        if (levelData != null && levelData.backgroundColorHex != null) {
            try {
                bgColor = Color.valueOf(levelData.backgroundColorHex);
            } catch (Exception e) {
                Gdx.app.log("FirstLevel", "Invalid background color hex: " + levelData.backgroundColorHex);
            }
        }

        ScreenUtils.clear(bgColor);
        this.game.viewport.apply();
        this.game.batch.setProjectionMatrix(this.game.viewport.getCamera().combined);

        this.game.batch.begin();

        // Draw tile layers
        for (TileLayer tl : tileLayers) {
            if (tl.tileMap == null || tl.texture == null) continue;
            int atlasColumns = tl.texture.getWidth() / tl.tileW;
            int rows = tl.tileMap.length;
            for (int r = 0; r < rows; r++) {
                int[] row = tl.tileMap[r];
                for (int c = 0; c < row.length; c++) {
                    int tileIdx = row[c];
                    if (tileIdx < 0) continue;
                    int tileCol = tileIdx % atlasColumns;
                    int tileRow = tileIdx / atlasColumns;
                    float drawX = tl.offsetX + c * tl.tileW;
                    float drawY = tl.offsetY + (rows - 1 - r) * tl.tileH;
                    this.game.batch.draw(tl.texture, drawX, drawY, tl.tileW, tl.tileH,
                        tileCol * tl.tileW, tileRow * tl.tileH, tl.tileW, tl.tileH, false, false);
                }
            }
        }

        // Draw background if available
        if (backgroundTexture != null) {
            this.game.batch.draw(backgroundTexture, 0, 0, this.game.viewport.getWorldWidth(), this.game.viewport.getWorldHeight());
        }

        // Draw all game objects
        // posX/posY is the anchor (center), so offset by half dimensions to get bottom-left
        for (GameObject go : gameObjectsByName.values()) {
            TextureRegion tex = go.getTexture();
            float drawX = go.getPosX() + go.getDimenX() / 2f;
            float drawY = go.getPosY() + go.getDimenY() / 2f;
            if (go instanceof AnimatedGameObject && ((AnimatedGameObject) go).isFlipX()) {
                this.game.batch.draw(tex,
                    drawX + go.getDimenX(), drawY,
                    -go.getDimenX(), go.getDimenY());
            } else {
                this.game.batch.draw(tex, drawX, drawY, go.getDimenX(), go.getDimenY());
            }
        }

        // Draw player name labels above each PLAYER object
        for (GameObject go : gameObjectsByName.values()) {
            if (go.getType() == AnimatedGameObject.GameObjectType.PLAYER) {
                float drawX = go.getPosX() + go.getDimenX() / 2f;
                float drawY = go.getPosY() + go.getDimenY() / 2f;
                glyphLayout.setText(game.font, go.getName());
                float nameX = drawX + go.getDimenX() / 2f - glyphLayout.width / 2f;
                float nameY = drawY + go.getDimenY() + glyphLayout.height + 4f;
                game.font.draw(this.game.batch, glyphLayout, nameX, nameY);

                // Draw key icon above name if player has the key
                if (go.getHasKey()) {
                    GameObject keyObj = gameObjectsByName.get("key");
                    keyObj.setPosX(drawX + go.getDimenX() / 2f - keyObj.getDimenX());
                    keyObj.setPosY(nameY + 4f);
                }
            }
        }

        this.game.batch.end();
    }

    private void handleGameState(GameObjectMessage[] gameObjectsMsg) {
        ArrayList<GameObject> gameObjectsToDelete = new ArrayList<>();

        for (GameObjectMessage gom : gameObjectsMsg) {
            if (gom.name == null) continue;
            for (GameObject go : gameObjectsByName.values()) {
                if (gom.name.equals(go.getName())) {
                    go.setPosX(gom.posX);
                    go.setPosY(game.viewport.getWorldHeight() - gom.posY - go.getDimenY());
                    go.setHasKey(gom.hasKey);

                    // Animation priority: jump > walk > idle
                    if (gom.isJumping) {
                        if (!go.getIsJumping()) {
                            go.setIsJumping(true);
                            changeAnimation(go.getName(), "quixote_walk_sheet_7");
                        }
                    } else {
                        if (go.getIsJumping()) {
                            go.setIsJumping(false);
                        }
                        if (gom.isMovingLeft || gom.isMovingRight) {
                            changeAnimation(go.getName(), "quixote_walk");
                        } else {
                            changeAnimation(go.getName(), "quixote_idle_anim");
                        }
                    }

                    // Flip texture based on movement direction
                    if (gom.isMovingLeft) {
                        go.setIsMovingLeft(true);
                        go.setIsMovingRight(false);
                        setFlipX(go.getName(), true);
                    } else if (gom.isMovingRight) {
                        go.setIsMovingLeft(false);
                        go.setIsMovingRight(true);
                        setFlipX(go.getName(), false);
                    }

                    if (gom.hasCompletedLevel) {
                        gameObjectsToDelete.add(go);
                        GameObject keyObj = gameObjectsByName.get("key");
                        if (keyObj != null) {
                            gameObjectsToDelete.add(gameObjectsByName.get("key"));
                        }
                    }


                }
            }
        }

        for (GameObject go : gameObjectsToDelete) {
            gameObjectsByName.remove(go.getName());
        }
    }

    private void setFlipX(String gameObjectName, boolean flip) {
        for (GameObject go : gameObjectsByName.values()) {
            if (go.getName().equals(gameObjectName) && go instanceof AnimatedGameObject) {
                ((AnimatedGameObject) go).setFlipX(flip);
                break;
            }
        }
    }

    private void handlePlayers(PlayerMessage[] players) {
        ArrayList<String> playerNames = new ArrayList<>();
        ArrayList<GameObject> gameObjectsToDelete = new ArrayList<>();

        for (GameObject go : gameObjectsByName.values()) {
            playerNames.add(go.getName());
        }

        game.config.players.clear();
        for (PlayerMessage player : players) {
            game.config.players.add(player.name);

            if (!playerNames.contains(player.name)) {
                if (textureCache.isEmpty()) {
                    Gdx.app.error("FirstLevel", "No textures loaded, cannot add player: " + player.name);
                    continue;
                }
                Texture spriteTexture = textureCache.values().iterator().next();
                LevelLoader.SpriteData spriteData = levelData.sprites.get(0);
                float spriteY = this.game.viewport.getWorldHeight() - spriteData.y - spriteData.height;
                AnimatedGameObject newPlayer = new AnimatedGameObject(
                    player.name,
                    new TextureRegion(spriteTexture),
                    spriteData.x,
                    spriteY,
                    spriteData.width,
                    spriteData.height,
                    AnimatedGameObject.GameObjectType.PLAYER
                );
                addAllAnimationsTo(newPlayer);
                newPlayer.playAnimation("quixote_idle_anim");
                gameObjectsByName.put(newPlayer.getName(), newPlayer);
            }
        }

        for (GameObject go : gameObjectsByName.values()) {
            if (!game.config.players.contains(go.getName()) &&
                go.getType() == AnimatedGameObject.GameObjectType.PLAYER) {
                gameObjectsToDelete.add(go);
            }
        }

        for (GameObject go : gameObjectsToDelete) {
            gameObjectsByName.remove(go.getName());
        }
    }

    private void handleLevelState(LevelStateMessage levelState) {
        if (levelState == null) return;
        GameObject door = gameObjectsByName.get("door");
        ((AnimatedGameObject) door).setFrame(levelState.isDoorOpen ? 1 : 0);
    }

    /**
     * Change animation for a specific game object by animation name (as defined in animations.json)
     * e.g. changeAnimation("qweqweqwe", "quixote_idle_anim")
     */
    public void changeAnimation(String gameObjectName, String animationName) {
        GameObject go = gameObjectsByName.get(gameObjectName);
        if (go instanceof AnimatedGameObject) {
            ((AnimatedGameObject) go).playAnimation(animationName);
        }
    }
}
