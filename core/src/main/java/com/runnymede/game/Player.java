package com.runnymede.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Player {
    float playerX;
    float playerY;
    Texture playerTexture;
    Sprite playerSprite;


    public Player(float x, float y, float speed, int health) {
        playerX = 0.0f;
        playerY = 0.0f;
        playerTexture = new Texture("playerTexture.png");
        playerSprite = new Sprite(playerTexture);

    }

    public float getPlayerX(){
        return playerX;
    }

    public float getPlayerY(){
        return playerY;
    }


    public void setPosition(float x, float y){
        playerX = x;
        playerY =  y;
    }

    public void changeX(float change){
        playerX += change;
    }

    public void changeY(float change){
        playerY += change;
    }

    public void draw(SpriteBatch spriteBatch){
        playerSprite.setPosition(playerX, playerY);
        playerSprite.draw(spriteBatch);
    }

}
