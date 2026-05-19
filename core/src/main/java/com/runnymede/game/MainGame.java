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
import com.badlogic.gdx.utils.viewport.Viewport;

public class MainGame implements ApplicationListener{
    private static final float enemySpeed = 0.80f;
    private static final float playerSpeed = 1.2f;


    private SpriteBatch spriteBatch;
    private Viewport viewport;

    private ArrayList<Enemy> enemies;
    private float timer;
    private float deltaTime;
    private Texture enemyTexture;
    private Player player;

    public void create(){
        spriteBatch = new SpriteBatch();
        enemies = new ArrayList<Enemy>();
        timer = 1f;
        enemyTexture = new Texture("enemyTexture.png");
        player = new Player(4.0f, 2.0f, playerSpeed, 3);
        viewport = new FitViewport(8, 5);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true); // The 'true' centers the camera on the world
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
        doPlayerMovement(deltaTime);
    }



    public void logic(float deltaTime){
        float playerXPos = player.getPlayerX();
        float playerYPos = player.getPlayerY();
        doEnemyTimer(deltaTime);
        moveEnemiesTowardPlayer(deltaTime, playerXPos, playerYPos);



    }

    public void draw(){
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);

        ScreenUtils.clear(0, 0, 0, 1); // Clears the screen before each frame

        spriteBatch.begin();

        player.draw(spriteBatch);
        for(Enemy cur : enemies){
            cur.draw(spriteBatch);
        }
        spriteBatch.end();
    }

    public void doEnemyTimer(float dt){
        if(timer > 0){
            timer -= dt;
        } else{
            Enemy enemy = new Enemy(0f, 0f, enemySpeed, 3, 1, enemyTexture);
            enemies.add(enemy);
            timer = 1;
        }
    }

    public void moveEnemiesTowardPlayer(float dt, float playerX, float playerY){
        for(Enemy e : enemies){
            e.moveTowardsPoint(dt, playerX, playerY);
        }
    }

    public void doPlayerMovement(float deltaTime){
        int numOfKeysPressed = 0;
        player.setSpeedMult(1);
        if(Gdx.input.isKeyPressed(Input.Keys.W)) numOfKeysPressed += 1;
        if(Gdx.input.isKeyPressed(Input.Keys.A)) numOfKeysPressed += 1;
        if(Gdx.input.isKeyPressed(Input.Keys.S)) numOfKeysPressed += 1;
        if(Gdx.input.isKeyPressed(Input.Keys.D)) numOfKeysPressed += 1;

        if(numOfKeysPressed > 1){
            player.setSpeedMult(0.7f);
        }

        if(Gdx.input.isKeyPressed(Input.Keys.W)) player.moveUp(deltaTime);
        if(Gdx.input.isKeyPressed(Input.Keys.A)) player.moveLeft(deltaTime);
        if(Gdx.input.isKeyPressed(Input.Keys.S)) player.moveDown(deltaTime);
        if(Gdx.input.isKeyPressed(Input.Keys.D)) player.moveRight(deltaTime);



    }


}
