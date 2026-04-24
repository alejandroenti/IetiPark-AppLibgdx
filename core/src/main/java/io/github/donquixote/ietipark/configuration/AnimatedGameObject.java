package io.github.donquixote.ietipark.configuration;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class AnimatedGameObject extends GameObject {
    private TextureRegion[][] animationFrames;
    private int currentFrameIndex;
    private float frameDuration;
    private float elapsedTime;
    private boolean isLooping;
    private String currentAnimationId;
    private boolean flipX;

    public AnimatedGameObject(String name, TextureRegion texture, float posX, float posY, float dimenX, float dimenY) {
        super(name, texture, posX, posY, dimenX, dimenY);
        this.currentFrameIndex = 0;
        this.elapsedTime = 0;
        this.isLooping = true;
    }

    public void setAnimation(Texture spriteSheet, int frameWidth, int frameHeight, 
                           int startFrame, int endFrame, float fps, boolean loop, String animId) {
        this.animationFrames = TextureRegion.split(spriteSheet, frameWidth, frameHeight);
        this.currentFrameIndex = startFrame;
        this.frameDuration = 1f / fps;
        this.elapsedTime = 0;
        this.isLooping = loop;
        this.currentAnimationId = animId;
        
        // Set initial frame
        if (animationFrames.length > 0 && animationFrames[0].length > startFrame) {
            setTexture(animationFrames[0][startFrame]);
        }
    }

    public void update(float delta) {
        if (animationFrames == null || animationFrames.length == 0) return;
        
        elapsedTime += delta;
        
        // Calculate total frames in animation
        int totalFrames = animationFrames[0].length;
        int frameCount = totalFrames; // Assuming single row of frames
        
        if (elapsedTime >= frameDuration) {
            elapsedTime -= frameDuration;
            currentFrameIndex++;
            
            if (currentFrameIndex >= frameCount) {
                if (isLooping) {
                    currentFrameIndex = 0;
                } else {
                    currentFrameIndex = frameCount - 1;
                }
            }
            
            if (animationFrames.length > 0 && animationFrames[0].length > currentFrameIndex) {
                setTexture(animationFrames[0][currentFrameIndex]);
            }
        }
    }

    public String getCurrentAnimationId() {
        return currentAnimationId;
    }

    public void setCurrentAnimationId(String animId) {
        this.currentAnimationId = animId;
    }

    public boolean isFlipX() {
        return flipX;
    }

    public void setFlipX(boolean flipX) {
        this.flipX = flipX;
    }
}
