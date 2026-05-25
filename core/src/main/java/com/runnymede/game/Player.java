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

    public float getX(){
        return playerSprite.getX();
    }

    public float getY(){
        return playerSprite.getY();
    }

    public float getCenterX() {
        return playerSprite.getX() + playerSprite.getWidth() / 2;
    }
    public float getCenterY() {
        return playerSprite.getY() + playerSprite.getHeight() / 2;
    }

    public Sprite getSprite(){
        return playerSprite;
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
        totalHitBox.setPosition(playerSprite.getX(), playerSprite.getY());

    }

    public void moveRight(float dt){
        float changeAmt = speed * speedMult * dt;
        playerSprite.translate(changeAmt, 0);
        totalHitBox.setPosition(playerSprite.getX(), playerSprite.getY());
    }

    public void moveUp(float dt){
        float changeAmt = speed * speedMult * dt;
        playerSprite.translate(0, changeAmt);
        totalHitBox.setPosition(playerSprite.getX(), playerSprite.getY());
    }

    public void moveDown(float dt){
        float changeAmt = speed * speedMult * dt;
        playerSprite.translate(0, -changeAmt);
        totalHitBox.setPosition(playerSprite.getX(), playerSprite.getY());
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
