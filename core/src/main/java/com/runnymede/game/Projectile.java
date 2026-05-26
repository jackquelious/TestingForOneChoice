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
    public Projectile(float speed, float startingX, float startingY, float targetX, float targetY) {
        // Initializes all variables
        this.speed = speed;

        this.texture = new Texture("bullet.png");
        this.sprite = new Sprite(texture);
        sprite.setSize(bulletSize, bulletSize);
        sprite.setPosition(startingX, startingY);

        hitBox = new Rectangle(startingX, startingY, sprite.getWidth(), sprite.getHeight());
        this.active = true;

        // Gets the angle to the target to be used in the update method
        this.angle = getAngleToTarget(targetX, targetY);
    }

    public Rectangle getHitBox(){
        return hitBox;
    } // gets the hitbox to do collisions

    // helper method that returns the angle between the sprite and the target cordinates
    public float getAngleToTarget(float targetX, float targetY) {
        // Gets the reletive position of the target and uses basic trig to get angles
        float x = sprite.getX();
        float y = sprite.getY();
        float deltaX = targetX - x;
        float deltaY = targetY - y;

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
    public void draw(SpriteBatch batch) {
        sprite.draw(batch);
    }

}
