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
    public boolean active;

    public Projectile(float speed, float startingX, float startingY, float targetX, float targetY, Texture texture) {
        this.speed = speed;

        this.texture = texture;
        this.sprite = new Sprite(texture);
        sprite.setSize(bulletSize, bulletSize);
        sprite.setPosition(startingX, startingY);

        hitBox = new Rectangle(startingX, startingY, sprite.getWidth(), sprite.getHeight());
        this.active = true;

        this.angle = getAngleToTarget(targetX, targetY);
    }

    // Returns
    public float getAngleToTarget(float targetX, float targetY) {
        float x = sprite.getX();
        float y = sprite.getY();
        float deltaX = targetX - x;
        float deltaY = targetY - y;

        float angle = (float) Math.atan2(deltaY, deltaX);
        return angle;
    }

    public void update(float dt) {
        float x = sprite.getX();
        float y = sprite.getY();

        float changeX =  (float) Math.cos(angle) * dt * speed;
        float changeY =  (float) Math.sin(angle) * dt * speed;

        sprite.translate(changeX, changeY);
        hitBox.setPosition(sprite.getX(), sprite.getY());
    }

    public Rectangle getHitBox(){
        return hitBox;
    }

    public void draw(SpriteBatch batch) {
        sprite.draw(batch);
    }

}
