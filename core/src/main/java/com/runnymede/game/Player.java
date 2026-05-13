package com.runnymede.game;

public class Player {
    float playerX;
    float playerY;

    public Player(){
        playerX = 0.0f;
        playerY = 0.0f;
    }

    public Player(float setX, float setY) {
        playerX = setX;
        playerY = setY;
    }

    public void setPosition(float x, float y){
        playerX = x;
        playerY =  y;
    }

    public void move (int direction, float speed, float deltaTime){
        playerX += Math.cos(direction) * speed * deltaTime;
        playerY += Math.sin(direction) * speed * deltaTime;
    }
}
