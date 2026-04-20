package io.github.donquixote.ietipark.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.donquixote.ietipark.DonQuixote;
import io.github.donquixote.ietipark.gametools.LevelData;
import io.github.donquixote.ietipark.gametools.LevelLoader;
import io.github.donquixote.ietipark.gametools.LevelRenderer;
import io.github.donquixote.ietipark.gametools.LevelRenderer.SpriteRuntimeState;
import io.github.donquixote.ietipark.gametools.RuntimeTransform;

public class FirstLevel implements Screen, IScreen {
    private final DonQuixote game;

    private static final float MOVE_SPEED = 80f;
    private static final String WALK_ANIM_ID = "anim_1776702668697288";
    private static final String IDLE_ANIM_ID = "anim_1776702618726446";

    private LevelData levelData;
    private LevelRenderer levelRenderer;
    private AssetManager assetManager;
    private OrthographicCamera camera;
    private Viewport viewport;

    private Array<SpriteRuntimeState> spriteRuntimeStates;
    private Array<RuntimeTransform> layerRuntimeTransforms;
    private boolean assetsLoaded;

    private int playerSpriteIndex = -1;
    private float playerYDown;
    private float animTimer;
    private boolean touchMovingLeft;
    private boolean touchMovingRight;

    public FirstLevel(DonQuixote game) {
        this.game = game;
    }

    @Override
    public void show() {
        levelData = LevelLoader.loadLevel(0);
        levelRenderer = new LevelRenderer();
        assetManager = new AssetManager();

        camera = new OrthographicCamera();
        viewport = new FitViewport(levelData.viewportWidth, levelData.viewportHeight, camera);

        // Queue texture assets for layers
        for (int i = 0; i < levelData.layers.size; i++) {
            LevelData.LevelLayer layer = levelData.layers.get(i);
            if (!assetManager.isLoaded(layer.tilesTexturePath, Texture.class)) {
                assetManager.load(layer.tilesTexturePath, Texture.class);
            }
        }

        // Queue texture assets for sprites
        for (int i = 0; i < levelData.sprites.size; i++) {
            LevelData.LevelSprite sprite = levelData.sprites.get(i);
            if (!assetManager.isLoaded(sprite.texturePath, Texture.class)) {
                assetManager.load(sprite.texturePath, Texture.class);
            }
        }

        // Load walk animation texture
        LevelData.AnimationClip walkClip = levelData.animationClips.get(WALK_ANIM_ID);
        if (walkClip != null && walkClip.texturePath != null) {
            if (!assetManager.isLoaded(walkClip.texturePath, Texture.class)) {
                assetManager.load(walkClip.texturePath, Texture.class);
            }
        }

        // Find ground Y from tilemap (first non-empty row)
        float groundYDown = levelData.worldHeight;
        if (levelData.layers.size > 0) {
            LevelData.LevelLayer layer = levelData.layers.get(0);
            for (int row = 0; row < layer.tileMap.length; row++) {
                boolean hasContent = false;
                for (int col = 0; col < layer.tileMap[row].length; col++) {
                    if (layer.tileMap[row][col] >= 0) {
                        hasContent = true;
                        break;
                    }
                }
                if (hasContent) {
                    groundYDown = layer.y + row * layer.tileHeight;
                    break;
                }
            }
        }

        // Initialize sprite runtime states and place player on the ground
        spriteRuntimeStates = new Array<>(levelData.sprites.size);
        for (int i = 0; i < levelData.sprites.size; i++) {
            LevelData.LevelSprite sprite = levelData.sprites.get(i);
            float spawnX = sprite.x;
            float spawnY = sprite.y;

            if ("quixote".equals(sprite.type) || "quixote".equals(sprite.name)) {
                playerSpriteIndex = i;
                spawnX = levelData.viewportWidth / 2f;
                spawnY = groundYDown - sprite.height * (1f - sprite.anchorY);
                playerYDown = spawnY;
            }

            spriteRuntimeStates.add(new SpriteRuntimeState(
                sprite.frameIndex,
                sprite.anchorX,
                sprite.anchorY,
                spawnX,
                spawnY,
                true,
                sprite.flipX,
                sprite.flipY,
                Math.max(1, Math.round(sprite.width)),
                Math.max(1, Math.round(sprite.height)),
                sprite.texturePath,
                sprite.animationId
            ));
        }

        // Initialize layer runtime transforms
        layerRuntimeTransforms = new Array<>(levelData.layers.size);
        for (int i = 0; i < levelData.layers.size; i++) {
            LevelData.LevelLayer layer = levelData.layers.get(i);
            layerRuntimeTransforms.add(new RuntimeTransform(layer.x, layer.y));
        }

        // Center camera on the player
        updateCameraPosition();

        // Input handling for touch movement
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                float halfScreen = Gdx.graphics.getWidth() / 2f;
                if (screenX < halfScreen) {
                    touchMovingLeft = true;
                } else {
                    touchMovingRight = true;
                }
                return true;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                touchMovingLeft = false;
                touchMovingRight = false;
                return true;
            }
        });

        assetsLoaded = false;
        animTimer = 0f;
    }

    private void updateCameraPosition() {
        if (playerSpriteIndex < 0) return;
        SpriteRuntimeState player = spriteRuntimeStates.get(playerSpriteIndex);

        float halfW = levelData.viewportWidth / 2f;
        float halfH = levelData.viewportHeight / 2f;

        // Convert player y-down to y-up for camera
        float camX = MathUtils.clamp(player.worldX, halfW, levelData.worldWidth - halfW);
        float camYUp = levelData.worldHeight - player.worldY;
        camYUp = MathUtils.clamp(camYUp, halfH, levelData.worldHeight - halfH);

        camera.position.set(camX, camYUp, 0f);
        camera.update();
    }

    @Override
    public void render(float delta) {
        if (assetManager == null) {
            return;
        }
        if (!assetsLoaded) {
            if (assetManager.update()) {
                assetsLoaded = true;
            } else {
                Gdx.gl.glClearColor(
                    levelData.backgroundColor.r,
                    levelData.backgroundColor.g,
                    levelData.backgroundColor.b,
                    levelData.backgroundColor.a
                );
                Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
                return;
            }
        }

        // Handle input
        float dx = 0f;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A) || touchMovingLeft) {
            dx -= MOVE_SPEED * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D) || touchMovingRight) {
            dx += MOVE_SPEED * delta;
        }

        // Update player sprite
        if (playerSpriteIndex >= 0) {
            SpriteRuntimeState player = spriteRuntimeStates.get(playerSpriteIndex);
            LevelData.LevelSprite playerSprite = levelData.sprites.get(playerSpriteIndex);
            player.worldX += dx;
            player.worldX = MathUtils.clamp(player.worldX, playerSprite.width * player.anchorX,
                levelData.worldWidth - playerSprite.width * (1f - player.anchorX));
            player.worldY = playerYDown;

            boolean moving = dx != 0f;

            // Flip sprite based on movement direction
            if (dx < 0f) player.flipX = true;
            if (dx > 0f) player.flipX = false;

            // Switch between walk and idle animation
            LevelData.AnimationClip walkClip = levelData.animationClips.get(WALK_ANIM_ID);
            LevelData.AnimationClip idleClip = levelData.animationClips.get(IDLE_ANIM_ID);

            if (moving && walkClip != null) {
                player.texturePath = walkClip.texturePath;
                player.animationId = WALK_ANIM_ID;
                player.frameWidth = walkClip.frameWidth;
                player.frameHeight = walkClip.frameHeight;
                animTimer += delta;
                float frameDuration = 1f / walkClip.fps;
                int totalFrames = walkClip.endFrame - walkClip.startFrame + 1;
                player.frameIndex = walkClip.startFrame + ((int) (animTimer / frameDuration) % totalFrames);
            } else if (idleClip != null) {
                player.texturePath = idleClip.texturePath;
                player.animationId = IDLE_ANIM_ID;
                player.frameWidth = idleClip.frameWidth;
                player.frameHeight = idleClip.frameHeight;
                player.frameIndex = idleClip.startFrame;
                animTimer = 0f;
            }

            updateCameraPosition();
        }

        // Render
        Gdx.gl.glClearColor(
            levelData.backgroundColor.r,
            levelData.backgroundColor.g,
            levelData.backgroundColor.b,
            levelData.backgroundColor.a
        );
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        levelRenderer.render(
            levelData,
            assetManager,
            game.batch,
            camera,
            spriteRuntimeStates,
            null,
            layerRuntimeTransforms
        );
        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        updateCameraPosition();
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        if (assetManager != null) {
            assetManager.dispose();
        }
    }

    @Override
    public void handleMessage(String message) {}
}