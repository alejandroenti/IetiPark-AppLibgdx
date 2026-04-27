package io.github.donquixote.ietipark.configuration;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class GameObject {

    private String name;
    private TextureRegion texture;
    private float posX;
    private float lastPosX;
    private float posY;
    private float dimenX;
    private float dimenY;
    private boolean isJumping;
    private boolean isMovingLeft;
    private boolean isMovingRight;

    public GameObject(String name, TextureRegion texture, float posX, float posY, float dimenX, float dimenY) {
        this.name = name;
        this.texture = texture;
        this.posX = posX;
        this.lastPosX = this.posX;
        this.posY = posY;
        this.dimenX = dimenX;
        this.dimenY = dimenY;
        this.isJumping = false;
        this.isMovingLeft = false;
        this.isMovingRight = false;
    }

    public String getName() {
        return name;
    }
    public TextureRegion getTexture() {
        return texture;
    }
    public void setTexture(TextureRegion texture) {
        this.texture = texture;
    }
    public void setPosX(float posX) {
        this.posX = posX;
        this.lastPosX = posX;
    }
    public float getPosX() {
        return posX;
    }
    public void setPosY(float posY) {
        this.posY = posY;
    }
    public float getLastPosX() {
        return lastPosX;
    }
    public float getPosY() {
        return posY;
    }
    public void setDimenX(float dimenX) {
        this.dimenX = dimenX;
    }
    public float getDimenX() {
        return dimenX;
    }
    public void setDimenY(float dimenY) {
        this.dimenY = dimenY;
    }
    public float getDimenY() {
        return dimenY;
    }
    public void setIsJumping(boolean isJumping) {
        this.isJumping = isJumping;
    }
    public boolean getIsJumping() {
        return isJumping;
    }
    public void setIsMovingRight(boolean isMovingRight) {
        this.isMovingRight = isMovingRight;
    }
    public boolean getIsMovingRight() {
        return isMovingRight;
    }
    public void setIsMovingLeft(boolean isMovingLeft) {
        this.isMovingLeft = isMovingLeft;
    }
    public boolean getIsMovingLeft() {
        return isMovingLeft;
    }

    public void rotateTexture(boolean flip) {
        texture.flip(flip, false);
    }
}
