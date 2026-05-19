package com.runnymede.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Player {
    private Texture playerTexture;
    private Sprite playerSprite;
    private int health;
    private float speed;
    private float speedMult;


    public Player(float x, float y, float speed, int health) {
        playerTexture = new Texture("playerTexture.png");
        playerSprite = new Sprite(playerTexture);
        playerSprite.setSize(2.0f, 2.0f);
        playerSprite.setPosition(x, y);
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

    public void draw(SpriteBatch spriteBatch){
        playerSprite.draw(spriteBatch);
    }

}
