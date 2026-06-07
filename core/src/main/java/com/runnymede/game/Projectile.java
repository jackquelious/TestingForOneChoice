package com.runnymede.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

import java.util.List;
import java.util.Random;


public class Projectile {
    // CLASS VARIABLES:
    private boolean belongsToPlayer; // tracks if it is an enemy or player projectile

    private Texture texture;
    private Sprite sprite;

    private float speed;
    private float angle;

    private Rectangle hitBox;
    private boolean active; // tracks whether the projectile is active or not
    private float despawnTimer;

    // Variable stats
    private float damage; // damage done by each bullet
    private float bulletSize;
    private float critChance;
    private float lifeSteal; // Heals the player by a percent of damage
    private int bulletBounces; // Number of times the bullet can bounce off of
    private int pierce; // Number of enemies the bullet can hit without despawning


    // constructor
    // Takes a start point, target point
    public Projectile(boolean belongsToPlayer, float speed, float bulletSize, float critChance,
                      float lifeSteal, float baseDamage, int pierce, int bulletBounces,
                      float centerX, float centerY, float targetX, float targetY, Texture texture) {

        // Gets the angle to the target to be used in the update method
        this.angle = getAngleToTarget(centerX, centerY, targetX, targetY);

        // Calculates the starting pos (bottom left)
        float spawnX = centerX - (bulletSize / 2.0f);
        float spawnY = centerY - (bulletSize / 2.0f);

        // Initializes all variables
        this.speed = speed;
        this.damage = baseDamage;
        this.bulletSize = bulletSize;
        this.critChance = critChance;
        this.lifeSteal = lifeSteal;
        this.pierce = pierce;
        this.bulletBounces = bulletBounces;
        this.belongsToPlayer = belongsToPlayer;


        this.texture = texture;

        // Makes a sprite and configures it
        this.sprite = new Sprite(texture);
        sprite.setSize(bulletSize, bulletSize);
        sprite.setPosition(spawnX, spawnY);

        // Makes a hitBox for it
        hitBox = new Rectangle(spawnX, spawnY, sprite.getWidth(), sprite.getHeight());
        this.active = true; // Has a boolean for active or not

        despawnTimer = 2.0f; // Time until bullet despawns
    }

    // GETTERS:
    public Rectangle getHitBox(){return hitBox;}
    public boolean getActive(){return active;}

    public float getDespawnTimer(){return despawnTimer;}

    public float getX(){return sprite.getX();}
    public float getY(){return sprite.getY();}

    public float getCenterX(){return sprite.getX() + (sprite.getWidth() / 2.0f);}
    public float getCenterY(){return sprite.getY() + (sprite.getHeight() / 2.0f);}

    public boolean getOwner() {return belongsToPlayer;}
    public Sprite getSprite(){return sprite;}
    public float getDamage(){return damage;}
    public float getSpeed(){return speed;}
    public float getBulletSize(){return bulletSize;}
    public float getCritChance(){return critChance;}
    public float getLifeSteal(){return lifeSteal;}
    public int getBulletBounces(){return bulletBounces;}
    public int getPierce(){return pierce;}

    // SETTERS
    public void setOwner(boolean belongsToPlayer){this.belongsToPlayer = belongsToPlayer;}
    public void setActive(boolean active) {this.active = active;}
    public void setDamage(float damage) {this.damage = damage;}
    public void setSpeed(float speed) {this.speed = speed;}
    public void setCritChance(float critChance) {this.critChance = critChance;}
    public void setLifeSteal(float lifeSteal) {this.lifeSteal = lifeSteal;}
    public void setBulletBounces(int bulletBounces) {this.bulletBounces = bulletBounces;}
    public void setPierce(int pierce) {this.pierce = pierce;}
    public void setPosition(float x, float y){
        sprite.setPosition(x, y);
        hitBox.setPosition(x, y);
    }



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
    // Then checks for wall collisions
    public void update(float dt, GridManager gridManager) {
        Random rand = new Random(); // Declares new instance of random class

        // Calculates the amount the bullet should change positions for each axis
        float changeX =  (float) Math.cos(angle) * dt * this.speed;
        float changeY =  (float) Math.sin(angle) * dt * this.speed;

        // Gets the previous cordinates so we can roll back if we hit a wall
        float oldX = sprite.getX();
        float oldY = sprite.getY();

        // Moves the sprite in the X direction
        sprite.translate(changeX, 0);
        hitBox.setPosition(sprite.getX(), sprite.getY());

        // Checks all walls for collision
        if(gridManager.checkWallCollision(hitBox)){

            // If they overlap roll back position and change the angle
            changeX = -changeX; // Reverses the X velocity

            // Reset the positions
            sprite.setPosition(oldX, oldY);
            hitBox.setPosition(oldX, oldY);
            this.angle = (float) Math.atan2(changeY, changeX); // Calculates new angle
            this.bulletBounces -= 1;
        }

        // Moves the sprite in the Y direction
        sprite.translate(0, changeY);
        hitBox.setPosition(sprite.getX(), sprite.getY());

        // Checks all walls for collision
        if(gridManager.checkWallCollision(hitBox)){
            // If they overlap roll back position and change the angle
            changeY = -changeY; // Reverses the X velocity

            // Reset the positions
            sprite.setPosition(oldX, oldY);
            hitBox.setPosition(oldX, oldY);

            // Re-calculates the angle
            this.angle = (float) Math.atan2(changeY, changeX);
            this.bulletBounces -= 1; // Subtracts bullet bounces

        }

        // If there is no more bullet bounces despawn the bullet
        if(this.bulletBounces <= 0){
            sprite.setPosition(-55, 50);
            hitBox.setPosition(-55, 50);
            this.active = false;
        }

        // Sets the hitbox pos after the movement
        hitBox.setPosition(sprite.getX(), sprite.getY()); // Sets the hitBox to the sprites position
    }


    // draw method
    public void draw(SpriteBatch batch) {sprite.draw(batch);}

}
