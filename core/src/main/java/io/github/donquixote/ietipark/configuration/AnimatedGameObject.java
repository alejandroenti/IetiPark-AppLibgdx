package io.github.donquixote.ietipark.configuration;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.HashMap;
import java.util.Map;

public class AnimatedGameObject extends GameObject {

    private static class AnimationEntry {
        TextureRegion[] frames;
        float frameDuration;
        boolean loop;
    }

    private final Map<String, AnimationEntry> animations = new HashMap<>();
    private AnimationEntry currentAnimation;
    private String currentAnimationName;
    private int currentFrameIndex;
    private float elapsedTime;
    private boolean flipX;
    private boolean paused;

    public AnimatedGameObject(String name, TextureRegion texture, float posX, float posY, float dimenX, float dimenY, GameObjectType type) {
        super(name, texture, posX, posY, dimenX, dimenY, type);
    }

    /**
     * Pre-load an animation. The spriteSheet texture is shared; TextureRegion frames are per-instance.
     */
    public void addAnimation(String animName, Texture spriteSheet, int frameWidth, int frameHeight,
                             int startFrame, int endFrame, float fps, boolean loop) {
        TextureRegion[][] all = TextureRegion.split(spriteSheet, frameWidth, frameHeight);
        int totalCols = all[0].length;
        int count = endFrame - startFrame + 1;
        TextureRegion[] frames = new TextureRegion[count];
        for (int i = 0; i < count; i++) {
            int idx = startFrame + i;
            frames[i] = all[idx / totalCols][idx % totalCols];
        }
        AnimationEntry entry = new AnimationEntry();
        entry.frames = frames;
        entry.frameDuration = 1f / fps;
        entry.loop = loop;
        animations.put(animName, entry);
    }

    /** Switch to the named animation. No-op if already playing or name not found. */
    public void playAnimation(String animName) {
        if (animName.equals(currentAnimationName)) return;
        AnimationEntry entry = animations.get(animName);
        if (entry == null) return;
        currentAnimation = entry;
        currentAnimationName = animName;
        currentFrameIndex = 0;
        elapsedTime = 0;
        paused = false;
        if (entry.frames.length > 0) {
            setTexture(entry.frames[0]);
        }
    }

    /** Jump to a specific frame index and freeze the animation there. */
    public void setFrame(int index) {
        if (currentAnimation == null || currentAnimation.frames.length == 0) return;
        currentFrameIndex = Math.max(0, Math.min(index, currentAnimation.frames.length - 1));
        elapsedTime = 0;
        paused = true;
        setTexture(currentAnimation.frames[currentFrameIndex]);
    }

    public void update(float delta) {
        if (paused || currentAnimation == null || currentAnimation.frames.length == 0) return;

        elapsedTime += delta;
        if (elapsedTime >= currentAnimation.frameDuration) {
            elapsedTime -= currentAnimation.frameDuration;
            currentFrameIndex++;
            if (currentFrameIndex >= currentAnimation.frames.length) {
                currentFrameIndex = currentAnimation.loop ? 0 : currentAnimation.frames.length - 1;
            }
            setTexture(currentAnimation.frames[currentFrameIndex]);
        }
    }

    public String getCurrentAnimationName() {
        return currentAnimationName;
    }

    public boolean isFlipX() {
        return flipX;
    }

    public void setFlipX(boolean flipX) {
        this.flipX = flipX;
    }
}
