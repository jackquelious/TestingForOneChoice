package com.runnymede.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Player {
    private Rectangle totalHitBox;
    private Texture playerTexture;
    private Sprite playerSprite;
    private int health;
    private float speed;
    private float speedMult;


    public Player(float x, float y, float speed, int health) {
        playerTexture = new Texture("playerSquare.png");
        playerSprite = new Sprite(playerTexture);
        playerSprite.setSize(0.55f, 0.55f);
        playerSprite.setPosition(x, y);
        totalHitBox = new Rectangle(x, y, 0.55f, 0.55f);
        this.health = health;
        this.speed = speed;
        speedMult = 1f;

    }

    public float getPlayerX(){
        return playerSprite.getX();
    }

    public float getPlayerY(){
        return playerSprite.getY();
    }


    public void setPosition(float x, float y){
        playerSprite.setPosition(x, y);
    }

    public void setSpeedMult(float newMult){
        speedMult = newMult;
    }

    // MOVEMENT METHODS:

    public void moveLeft(float dt){
        float changeAmt = speed * speedMult * dt;
        playerSprite.translate(-changeAmt, 0);

    }

    public void moveRight(float dt){
        float changeAmt = speed * speedMult * dt;
        playerSprite.translate(changeAmt, 0);
    }

    public void moveUp(float dt){
        float changeAmt = speed * speedMult * dt;
        playerSprite.translate(0, changeAmt);
    }

    public void moveDown(float dt){
        float changeAmt = speed * speedMult * dt;
        playerSprite.translate(0, -changeAmt);
    }

    //Sets the position of the hitBox
    public void setTotalHitBox(float x, float y){
        totalHitBox.setPosition(x, y);
    }

    //Returns the hitBox, to do collision
    public Rectangle getHitBox(){
         return totalHitBox;
    }

    public void draw(SpriteBatch spriteBatch){
        playerSprite.draw(spriteBatch);
    }

}
