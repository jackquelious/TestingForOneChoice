package com.runnymede.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;


public class Projectile {
    // CONSTANTS:
    private final float bulletSize = 0.2f;

    // CLASS VARIABLES:
    private Texture texture;
    private Sprite sprite;

    private float speed;
    private float angle;

    private Rectangle hitBox;
    public boolean active; // tracks whether the projectile is active or not


    // constructor
    // Takes a start point, target point
    public Projectile(float speed, float centerX, float centerY, float targetX, float targetY) {
        // Gets the angle to the target to be used in the update method
        this.angle = getAngleToTarget(centerX, centerY, targetX, targetY);

        // Calculates the starting pos (bottom left)
        float spawnX = centerX - (bulletSize / 2.0f);
        float spawnY = centerY - (bulletSize / 2.0f);

        // Initializes all variables
        this.speed = speed;

        this.texture = new Texture("bullet.png");

        // Makes a sprite and configures it
        this.sprite = new Sprite(texture);
        sprite.setSize(bulletSize, bulletSize);
        sprite.setPosition(spawnX, spawnY);

        // Makes a hitBox for it
        hitBox = new Rectangle(spawnX, spawnY, sprite.getWidth(), sprite.getHeight());
        this.active = true; // Has a boolean for active or not


    }

    // GETTERS:
    public Rectangle getHitBox(){return hitBox;}

    public float getX(){return sprite.getX();}
    public float getY(){return sprite.getY();}

    public float getCenterX(){return sprite.getX() +(sprite.getWidth() / 2.0f);}
    public float getCenterY(){return sprite.getY() +(sprite.getHeight() / 2.0f);}

    // helper method that returns the angle between the sprite and the target cordinates
    public float getAngleToTarget(float startX, float startY, float targetX, float targetY) {
        // Gets the reletive position of the target and uses basic trig to get angles
        float deltaX = targetX - startX;
        float deltaY = targetY - startY;

        float angle = (float) Math.atan2(deltaY, deltaX);
        return angle;
    }

    // The important method
    // This method updates the bullets positions every frame
    public void update(float dt) {

        // Calculates the amount the bullet should change positions for each axis
        float changeX =  (float) Math.cos(angle) * dt * speed;
        float changeY =  (float) Math.sin(angle) * dt * speed;

        // Moves the sprite and updates the hitbox position
        sprite.translate(changeX, changeY);
        hitBox.setPosition(sprite.getX(), sprite.getY());
    }


    // draw method
    public void draw(SpriteBatch batch) {sprite.draw(batch);}

}
