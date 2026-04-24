package io.github.donquixote.ietipark.configuration;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LevelLoader {
    private static final JsonReader reader = new JsonReader();

    public static class AnimationData {
        public String id;
        public String name;
        public String mediaFile;
        public int startFrame;
        public int endFrame;
        public float fps;
        public boolean loop;
        public int frameWidth;
        public int frameHeight;
    }

    public static class SpriteData {
        public String name;
        public String type;
        public String animationId;
        public float x;
        public float y;
        public int width;
        public int height;
        public String imageFile;
        public boolean flipX;
        public boolean flipY;
    }

    public static class LevelData {
        public String name;
        public String description;
        public ArrayList<SpriteData> sprites;
        public int viewportWidth;
        public int viewportHeight;
        public String backgroundColorHex;
    }

    public static Map<String, AnimationData> loadAnimations() {
        // Build tile size lookup from mediaAssets in game_data.json
        Map<String, int[]> mediaTileSizes = new HashMap<>();
        try {
            JsonValue gameData = reader.parse(Gdx.files.internal("levels/game_data.json"));
            JsonValue mediaAssets = gameData.get("mediaAssets");
            if (mediaAssets != null) {
                for (JsonValue asset : mediaAssets) {
                    String fileName = asset.getString("fileName");
                    int tw = asset.getInt("tileWidth", 0);
                    int th = asset.getInt("tileHeight", 0);
                    mediaTileSizes.put(fileName, new int[]{tw, th});
                }
            }
        } catch (Exception e) {
            Gdx.app.error("LevelLoader", "Failed to load mediaAssets", e);
        }

        Map<String, AnimationData> animations = new HashMap<>();
        try {
            JsonValue root = reader.parse(Gdx.files.internal("levels/animations/animations.json"));
            JsonValue animArray = root.get("animations");

            if (animArray != null) {
                for (JsonValue anim : animArray) {
                    AnimationData animData = new AnimationData();
                    animData.id = anim.getString("id");
                    animData.name = anim.getString("name");
                    animData.mediaFile = anim.getString("mediaFile");
                    animData.startFrame = anim.getInt("startFrame", 0);
                    animData.endFrame = anim.getInt("endFrame", 0);
                    animData.fps = anim.getFloat("fps", 12f);
                    animData.loop = anim.getBoolean("loop", true);
                    int[] tileSizes = mediaTileSizes.get(animData.mediaFile);
                    if (tileSizes != null) {
                        animData.frameWidth  = tileSizes[0];
                        animData.frameHeight = tileSizes[1];
                    }
                    animations.put(animData.id, animData);
                }
            }
        } catch (Exception e) {
            Gdx.app.error("LevelLoader", "Failed to load animations", e);
        }
        return animations;
    }

    public static LevelData loadLevel(String levelName) {
        LevelData levelData = null;
        try {
            JsonValue root = reader.parse(Gdx.files.internal("levels/game_data.json"));
            JsonValue levels = root.get("levels");
            
            if (levels != null) {
                for (JsonValue level : levels) {
                    if (levelName.equals(level.getString("name"))) {
                        levelData = new LevelData();
                        levelData.name = level.getString("name");
                        levelData.description = level.getString("description", "");
                        levelData.viewportWidth = level.getInt("viewportWidth", 1920);
                        levelData.viewportHeight = level.getInt("viewportHeight", 1080);
                        levelData.backgroundColorHex = level.getString("backgroundColorHex", "#000000");
                        
                        levelData.sprites = new ArrayList<>();
                        JsonValue sprites = level.get("sprites");
                        if (sprites != null) {
                            for (JsonValue sprite : sprites) {
                                SpriteData spriteData = new SpriteData();
                                spriteData.name = sprite.getString("name");
                                spriteData.type = sprite.getString("type");
                                spriteData.animationId = sprite.getString("animationId");
                                spriteData.x = sprite.getFloat("x", 0);
                                spriteData.y = sprite.getFloat("y", 0);
                                spriteData.width = sprite.getInt("width", 32);
                                spriteData.height = sprite.getInt("height", 32);
                                spriteData.imageFile = sprite.getString("imageFile");
                                spriteData.flipX = sprite.getBoolean("flipX", false);
                                spriteData.flipY = sprite.getBoolean("flipY", false);
                                levelData.sprites.add(spriteData);
                            }
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Gdx.app.error("LevelLoader", "Failed to load level: " + levelName, e);
        }
        return levelData;
    }
}
