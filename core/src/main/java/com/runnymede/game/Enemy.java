package com.runnymede.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Enemy {
    private float xPos;
    private float yPos;
    private float speed;
    private int health;
    private int damage;
    private Texture texture;
    private Sprite sprite;
    private boolean alive;

    public Enemy(float xPos, float yPos, float speed, int health, int damage, Texture texture) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.speed = speed;
        this.health = health;
        this.damage = damage;
        this.texture = texture;
        this.sprite = new Sprite(texture);
        this.alive = true;

    }

    public float getxPos() {
        return xPos;
    }

    public float getyPos() {
        return yPos;
    }

    public float getSpeed() {
        return speed;
    }

    public int getHealth() {
        return health;
    }

    public int getDamage() {
        return damage;
    }

    public Texture getTexture() {
        return texture;
    }

    public Sprite getSprite() {
        return sprite;
    }

    public void takeDamage(int damage) {
        health -= damage;
        if (health <= 0) {
            remove();
        }
    }

    public void spawn(float xPos, float yPos) {
        this.xPos = xPos;
        this.yPos = yPos;
        alive = true;
    }   

    public void setPosition(float xPos, float yPos) {
        this.xPos = xPos;
        this.yPos = yPos;
    }

    public void remove() {
        // Implementation for removing the enemy
        xPos = -100; 
        yPos = -100;
        health = 0; // Ensure health is set to 0 to indicate removal
        alive = false;
    }

    public float getAngleToPoint(float targetX, float targetY) {
        float deltaX = targetX - xPos;
        float deltaY = targetY - yPos;
        float resultingAngle = (float) Math.atan2(deltaY, deltaX);
        return resultingAngle;
    }

    public void moveTowardsPoint(float deltaTime, float targetX, float targetY) {
        float angle = getAngleToPoint(targetX, targetY);
        xPos += speed * Math.cos(angle) * deltaTime;
        yPos += speed * Math.sin(angle) * deltaTime;
    }

    public void draw(SpriteBatch batch) {
        sprite.setPosition(xPos, yPos);
        sprite.draw(batch);
    }

}