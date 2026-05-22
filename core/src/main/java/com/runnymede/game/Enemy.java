package com.runnymede.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Enemy {
    private float speed;
    private int health;
    private int damage;
    private Texture texture;
    private Sprite sprite;
    private boolean alive;


    public Enemy(float xPos, float yPos, float speed, int health, int damage) {
        this.speed = speed;
        this.health = health;
        this.damage = damage;
        this.texture = new Texture("enemySquare.png");
        this.sprite = new Sprite(texture);
        this.sprite.setSize(0.4f, 0.4f);
        this.sprite.setPosition(xPos, yPos);
        this.alive = true;

    }

    public float getXPos() {
        return sprite.getX();
    }

    public float getYPos() {
        return sprite.getY();
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
        sprite.setPosition(xPos, yPos);
        alive = true;
    }

    public void setPosition(float xPos, float yPos) {
        setPosition(xPos, yPos);
    }

    public void remove() {
        // Implementation for removing the enemy
        sprite.setPosition(-100f, -100f);
        health = 0; // Ensure health is set to 0 to indicate removal
        alive = false;
    }

    public float getAngleToPoint(float targetX, float targetY) {
        float deltaX = targetX - getXPos();
        float deltaY = targetY - getYPos();
        float resultingAngle = (float) Math.atan2(deltaY, deltaX);
        return resultingAngle;
    }

    public void moveTowardsPoint(float deltaTime, float targetX, float targetY) {
        float angle = getAngleToPoint(targetX, targetY); // Gets angle to point

        // Calculates the movement amount based on sohcahtoa math
        float xMoveAmount = (float) (speed * Math.cos(angle) * deltaTime);
        float yMoveAmount = (float) (speed * Math.sin(angle) * deltaTime);

        // Calculates the new positions
        float newXPos = getXPos() + xMoveAmount;
        float newYPos = getYPos() + yMoveAmount;

        // Sets the new positions
        sprite.setPosition(newXPos, newYPos);

    }

    public void draw(SpriteBatch batch) {
        sprite.draw(batch);
    }

}
