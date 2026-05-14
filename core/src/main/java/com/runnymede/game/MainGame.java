package com.runnymede.game;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.*;

public class MainGame implements ApplicationListener{
    SpriteBatch spriteBatch;
    ArrayList<Enemy> enemies;
    float timer;
    float deltaTime;
    Texture enemyTexture;
    Player player;

    public void create(){
        spriteBatch = new SpriteBatch();
        enemies = new ArrayList<Enemy>();
        timer = 1f;
        enemyTexture = new Texture("enemyTexture.png");
        player = new Player(2.0f, 2.0f, 0.6f, 3);


    }

    public void resize(int width, int height) {
    }

    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();
        input(deltaTime);
        logic(deltaTime);
        draw();
    }


    public void pause() {
    }

    public void resume() {
    }

    public void dispose() {
        spriteBatch.dispose();
    }

    public void input(float deltaTime){

    }

    public void logic(float deltaTime){
        float playerXPos = player.getPlayerX();
        float playerYPos = player.getPlayerY();
        doEnemyTimer(deltaTime);
        moveEnemiesTowardPlayer(deltaTime, playerXPos, playerYPos);



    }

    public void doEnemyTimer(float dt){
        if(timer > 0){
            timer -= dt;
        } else{
            Enemy enemy = new Enemy(0f, 0f, 0.2f, 3, 1, enemyTexture);
            enemies.add(enemy);
            timer = 1;
        }
    }

    public void moveEnemiesTowardPlayer(float dt, float playerX, float playerY){
        for(Enemy e : enemies){
            e.moveTowardsPoint(dt, playerX, playerY);
        }
    }

    public void draw(){

    }
}
