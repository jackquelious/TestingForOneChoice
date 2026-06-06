package com.runnymede.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;


public class Player {
    //  CONSTANTS (default values):
    private final float PLAYER_SIZE = 0.55f;
    private final int DAMAGE = 1;
    private final float BULLET_SIZE = 0.2f;
    private final float BULLET_SPEED = 5.0f;
    private final float CRIT_CHANCE = 0.0f;
    private final float LIFE_STEAL = 0.0f;
    private final int BULLET_BOUNCES = 1;
    private final int PIERCE = 1;



    // CLASS VARIABLES:
    private Rectangle totalHitBox; // The hit box centered on the player

    // The player textures and sprite
    private Texture playerTexture;
    private Sprite playerSprite;

    // Variable stats
    private int maxHealth;
    private int health;
    private float speed;
    private int damage; // damage done by each bullet
    private float bulletSize;
    private float bulletSpeed;
    private float critChance;
    private float lifeSteal; // Heals the player by a percent of damage
    private int bulletBounces; // Number of times the bullet can bounce off of
    private int pierce; // Number of enemies the bullet can hit without despawning

    private float speedMult; // used to change speeds depending on player state


    // Constructor
    public Player(float x, float y, float speed, int maxHealth) {
        // Instantiates and initializes all variables
        playerTexture = new Texture("playerSquare.png");
        playerSprite = new Sprite(playerTexture);
        playerSprite.setSize(PLAYER_SIZE, PLAYER_SIZE);
        playerSprite.setPosition(x, y);

        totalHitBox = new Rectangle(x, y, PLAYER_SIZE, PLAYER_SIZE);

        this.health = maxHealth;
        this.maxHealth = maxHealth;
        this.speed = speed;

        // default values
        this.damage = DAMAGE;
        this.bulletSize = BULLET_SIZE;
        this.bulletSpeed = BULLET_SPEED;
        this.critChance = CRIT_CHANCE;
        this.lifeSteal = LIFE_STEAL;
        this.bulletBounces = BULLET_BOUNCES;
        this.pierce = PIERCE;

        speedMult = 1f; // default state has no speed mult

    }

    // GETTERS
    public float getX(){return playerSprite.getX();}
    public float getY(){return playerSprite.getY();}

    // These gets the center cords instead of bottom left corner
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
    public int getDamage(){return damage;}

    // Setters
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
    public void setDamage(int newdmg){damage = newdmg;}

    public void heal(float healAmt){
        if((health + healAmt) <= maxHealth) health += healAmt;
        else health = maxHealth;
    }

    // MOVEMENT METHODS:
    // calculates the change amount with delta time, speed, and speed multiplier
    // Then moves the player by that amount
    public void moveLeft(float dt, GridManager gridManager){
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




    public void draw(SpriteBatch spriteBatch){
        playerSprite.draw(spriteBatch);
    }

}
