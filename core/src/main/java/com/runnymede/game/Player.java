package com.runnymede.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.graphics.Color;

public class Player {
    //  CONSTANTS (default values):
    private final float PLAYER_SIZE = 0.25f;
    private final float DAMAGE = 1;
    private final float BULLET_SIZE = 0.18f;
    private final float BULLET_SPEED = 5.0f;
    private final float CRIT_CHANCE = 0.0f;
    private final float LIFE_STEAL = 0;
    private final int BULLET_BOUNCES = 1;
    private final int PIERCE = 1;

    // DASH TUNING CONSTANTS:
    private float DASH_DURATION;
    private float DASH_COOLDOWN;
    private float DASH_SPEED_MULTIPLIER;

    // SHIELD TUNING CONSTANTS:
    private final float SHIELD_RECHARGE_TIME = 5.0f; // Seconds before bubble comes back
    private final float SHIELD_SIZE_MULTIPLIER = 2.5f; // How much larger the bubble is than the player

    // CLASS VARIABLES:
    private Rectangle totalHitBox;

    // The player textures and sprite
    private Texture playerTexture;
    private Sprite playerSprite;

    // Shield textures and sprite
    private Texture shieldTexture;
    private Sprite shieldSprite;

    // Variable stats
    private int maxHealth;
    private int health;
    private float speed;
    private float damage;
    private float bulletSize;
    private float bulletSpeed;
    private float critChance;
    private float lifeSteal;
    private int bulletBounces;
    private int pierce;

    // Special cool upgrades
    public boolean hasDash;
    public boolean hasUpgradedDash;

    public boolean hasShield;

    // Shield Tracking Variables
    public boolean shieldActive;
    private float shieldCooldownTimer = 0f;

    private float speedMult;

    // Dash Tracking Variables
    private float dashTimer = 0f;
    private float dashCooldownTimer = 0f;
    private float dashDirX = 0f;
    private float dashDirY = 0f;

    // Constructor
    public Player(float x, float y, float speed, int maxHealth, float DASH_DURATION,
                  float DASH_COOLDOWN, float DASH_SPEED_MULTIPLIER) {
        playerTexture = new Texture("playerSquare.png");
        playerSprite = new Sprite(playerTexture);
        playerSprite.setSize(PLAYER_SIZE, PLAYER_SIZE);
        playerSprite.setPosition(x, y);

        // Initialize the Shield Bubble Sprite
        shieldTexture = new Texture("shieldTexture.png"); // Make sure to drop this image in your assets!
        shieldSprite = new Sprite(shieldTexture);
        shieldSprite.setSize(PLAYER_SIZE * SHIELD_SIZE_MULTIPLIER, PLAYER_SIZE * SHIELD_SIZE_MULTIPLIER);
        shieldSprite.setColor(Color.PURPLE);
        shieldSprite.setAlpha(0.7f); // Makes the bubble 60% transparent

        totalHitBox = new Rectangle(x, y, PLAYER_SIZE, PLAYER_SIZE);

        this.health = maxHealth;
        this.maxHealth = maxHealth;
        this.speed = speed;

        this.damage = DAMAGE;
        this.bulletSize = BULLET_SIZE;
        this.bulletSpeed = BULLET_SPEED;
        this.critChance = CRIT_CHANCE;
        this.lifeSteal = LIFE_STEAL;
        this.bulletBounces = BULLET_BOUNCES;
        this.pierce = PIERCE;

        speedMult = 1f;

        this.DASH_DURATION = DASH_DURATION;
        this.DASH_COOLDOWN = DASH_COOLDOWN;
        this.DASH_SPEED_MULTIPLIER = DASH_SPEED_MULTIPLIER;

        this.hasDash = false;
        this.hasUpgradedDash = false;

        this.hasShield = false;
        this.shieldActive = false;


    }

    // GETTERS
    public float getX(){return playerSprite.getX();}
    public float getY(){return playerSprite.getY();}
    public float getCenterX() {return playerSprite.getX() + playerSprite.getWidth() / 2;}
    public float getCenterY() {return playerSprite.getY() + playerSprite.getHeight() / 2;}
    public Sprite getSprite(){return playerSprite;}
    public Rectangle getHitBox(){return totalHitBox;}
    public float getSpeed(){return speed;}
    public float getBulletSize(){return bulletSize;}
    public float getBulletSpeed(){return bulletSpeed;}
    public float getCritChance(){return critChance;}
    public float getLifeSteal(){return lifeSteal;}
    public int getHealth(){return health;}
    public int getMaxHealth(){return maxHealth;}
    public int getBulletBounces(){return bulletBounces;}
    public int getPierce(){return pierce;}
    public float getDamage(){return damage;}
    public boolean isDashing() { return dashTimer > 0f; }
    public boolean isAlive(){return health > 0;}

    // SETTERS
    public void setPosition(float x, float y){playerSprite.setPosition(x, y);}
    public void setTotalHitBox(float x, float y){totalHitBox.setPosition(x, y);}
    public void setSpeedMult(float newMult){speedMult = newMult;}
    public void setSpeed(float newSpeed){speed = newSpeed;}
    public void setBulletSize(float newSize){bulletSize = newSize;}
    public void setBulletSpeed(float newBSpeed){bulletSpeed = newBSpeed;}
    public void setCritChance(float newCritChance){critChance = newCritChance;}
    public void setLifeSteal(float newLS){lifeSteal = newLS;}
    public void setHealth(int newHealth){health = newHealth;}
    public void setMaxHealth(int newMaxHealth){maxHealth = newMaxHealth;}
    public void setBulletBounces(int newBB){bulletBounces = newBB;}
    public void setPierce(int newPierce){pierce = newPierce;}
    public void setDamage(float newdmg){damage = newdmg;}

    public void heal(float healAmt){
        if((health + healAmt) <= maxHealth) health += healAmt;
        else health = maxHealth;
    }

    public void resetStats(float baseSpeed, int baseHealth) {
        this.speed = baseSpeed;
        this.maxHealth = baseHealth;
        this.health = baseHealth;

        // Reset weapon/bullet attributes back to defaults
        this.damage = 1;
        this.bulletSize = 0.2f;
        this.bulletSpeed = 4.5f;
        this.bulletBounces = 1;
        this.critChance = 0.00f;
        this.lifeSteal = 0.0f;
        this.pierce = 1;

        // Reset permanent/unlocked system traits if needed
        this.hasDash = false;
        this.hasUpgradedDash = false;
        this.hasShield = false;
        this.shieldActive = false;
    }


    /**
     * Now returns a boolean! True if the player lost health, False if damage was dodged or blocked.
     */
    public boolean takeDamage(float damage){
        if (hasUpgradedDash && isDashing()) {
            return false; // Dodged!
        }

        if (hasShield && shieldActive) {
            shieldActive = false;
            shieldCooldownTimer = SHIELD_RECHARGE_TIME;
            System.out.println("Shield Popped! Negated " + damage + " damage.");
            return false; // Blocked!
        }

        health -= damage;
        if(health <= 0){
            System.out.println("Player has been defeated!");
        }
        return true; // Took actual health damage!
    }

    public void updateShieldSystem(float dt) {
        // Tick down the recharge timer if the shield is broken
        if (hasShield && !shieldActive) {
            shieldCooldownTimer -= dt;
            if (shieldCooldownTimer <= 0) {
                shieldActive = true;
                System.out.println("Shield Recharged!");
            }
        }

        // If the shield is active, snap its coordinates to perfectly center around the player
        if (shieldActive) {
            float shieldX = getCenterX() - (shieldSprite.getWidth() / 2f);
            float shieldY = getCenterY() - (shieldSprite.getHeight() / 2f);
            shieldSprite.setPosition(shieldX, shieldY);
        }
    }

    public void updateDashSystem(float dt, GridManager gridManager) {
        if (dashCooldownTimer > 0) dashCooldownTimer -= dt;

        if (isDashing()) {
            float oldX = playerSprite.getX();
            float oldY = playerSprite.getY();

            float length = (float) Math.sqrt(dashDirX * dashDirX + dashDirY * dashDirY);
            float moveX = 0;
            float moveY = 0;
            if (length > 0) {
                moveX = (dashDirX / length) * speed * DASH_SPEED_MULTIPLIER * dt;
                moveY = (dashDirY / length) * speed * DASH_SPEED_MULTIPLIER * dt;
            }

            playerSprite.translate(moveX, 0);
            totalHitBox.setPosition(playerSprite.getX(), playerSprite.getY());
            if (gridManager.checkWallCollision(totalHitBox)) {
                playerSprite.setX(oldX);
                totalHitBox.setPosition(oldX, oldY);
            }

            playerSprite.translate(0, moveY);
            totalHitBox.setPosition(playerSprite.getX(), playerSprite.getY());
            if (gridManager.checkWallCollision(totalHitBox)) {
                playerSprite.setY(oldY);
                totalHitBox.setPosition(playerSprite.getX(), oldY);
            }

            dashTimer -= dt;
            return;
        }

        if (hasDash && dashCooldownTimer <= 0 && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            dashDirX = 0f;
            dashDirY = 0f;

            if (Gdx.input.isKeyPressed(Input.Keys.A)) dashDirX -= 1f;
            if (Gdx.input.isKeyPressed(Input.Keys.D)) dashDirX += 1f;
            if (Gdx.input.isKeyPressed(Input.Keys.S)) dashDirY -= 1f;
            if (Gdx.input.isKeyPressed(Input.Keys.W)) dashDirY += 1f;

            if (dashDirX != 0f || dashDirY != 0f) {
                dashTimer = DASH_DURATION;
                dashCooldownTimer = DASH_COOLDOWN;
            }
        }
    }

    public void moveLeft(float dt, GridManager gridManager){
        if (isDashing()) return;
        float oldX = playerSprite.getX();
        float oldY = playerSprite.getY();
        float changeAmt = speed * speedMult * dt;
        playerSprite.translate(-changeAmt, 0);
        totalHitBox.setPosition(playerSprite.getX(), playerSprite.getY());
        if(gridManager.checkWallCollision(totalHitBox)){
            playerSprite.setX(oldX);
            totalHitBox.setPosition(oldX, oldY);
        }
    }

    public void moveRight(float dt, GridManager gridManager){
        if (isDashing()) return;
        float oldX = playerSprite.getX();
        float oldY = playerSprite.getY();
        float changeAmt = speed * speedMult * dt;
        playerSprite.translate(changeAmt, 0);
        totalHitBox.setPosition(playerSprite.getX(), playerSprite.getY());
        if(gridManager.checkWallCollision(totalHitBox)){
            playerSprite.setX(oldX);
            totalHitBox.setPosition(oldX, oldY);
        }
    }

    public void moveUp(float dt, GridManager gridManager){
        if (isDashing()) return;
        float oldX = playerSprite.getX();
        float oldY = playerSprite.getY();
        float changeAmt = speed * speedMult * dt;
        playerSprite.translate(0, changeAmt);
        totalHitBox.setPosition(playerSprite.getX(), playerSprite.getY());
        if(gridManager.checkWallCollision(totalHitBox)){
            playerSprite.setY(oldY);
            totalHitBox.setPosition(oldX, oldY);
        }
    }

    public void moveDown(float dt, GridManager gridManager){
        if (isDashing()) return;
        float oldX = playerSprite.getX();
        float oldY = playerSprite.getY();
        float changeAmt = speed * speedMult * dt;
        playerSprite.translate(0, -changeAmt);
        totalHitBox.setPosition(playerSprite.getX(), playerSprite.getY());
        if(gridManager.checkWallCollision(totalHitBox)){
            playerSprite.setY(oldY);
            totalHitBox.setPosition(oldX, oldY);
        }
    }

    public void applyUpgrade(MainGame.LoopUpgrade upgrade) {
        switch (upgrade) {
            case LIFESTEAL:
                this.lifeSteal += 1.0f;
                break;
            case BOUNCES:
                this.bulletBounces += 2;
                break;
            case SHIELD:
                this.hasShield = true;
                this.shieldActive = true; // Turn it on instantly when acquired!
                break;
            case DASH:
                this.hasDash = true;
                break;
            case UPGRADED_DASH:
                this.hasUpgradedDash = true;
                break;
        }
    }

    public void draw(SpriteBatch spriteBatch){
        playerSprite.draw(spriteBatch);

        // Only draw the bubble if they have the upgrade AND it isn't broken
        if (hasShield && shieldActive) {
            shieldSprite.draw(spriteBatch);
        }
    }
}
