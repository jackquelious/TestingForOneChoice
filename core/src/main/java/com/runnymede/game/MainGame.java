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
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.*;
import com.badlogic.gdx.utils.viewport.Viewport;

public class MainGame implements ApplicationListener{
    private static final float enemySpeed = 1.3f;
    private static final float playerSpeed = 1.8f;


    private SpriteBatch spriteBatch;
    private Viewport viewport;

    private ArrayList<Rectangle> walls;
    private Texture wallTexture;

    private ArrayList<Enemy> enemies;
    private float timer;
    private float deltaTime;
    private Player player;
    private GridManager gridManager;

    public void create() {
        spriteBatch = new SpriteBatch();
        enemies = new ArrayList<Enemy>();
        timer = 1f;
        player = new Player(4.0f, 2.0f, playerSpeed, 3);
        viewport = new FitViewport(8, 5);

        walls = new ArrayList<Rectangle>();
        wallTexture = new Texture("wallTexture.jpg");

        // Creates the room boundaries
        walls.add(new Rectangle(0, 0, 8, 0.5f));
        walls.add(new Rectangle(0, 4.5f, 8, 0.5f));
        walls.add(new Rectangle(0, 0, 0.5f, 5));
        walls.add(new Rectangle(7.5f, 0, 0.5f, 5));

        // The pillar
        walls.add(new Rectangle(3, 2, 1, 1));

        //Intiantiates gridManager
        gridManager = new GridManager(walls);
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
        spriteBatch.begin(); // Starts the sprite batch

        player.draw(spriteBatch); // draws player
        drawEnemies(); // draws enemies
        drawWalls(); // draws walls

        spriteBatch.end(); // ends sprite batch
    }

    public void doEnemyTimer(float dt){
        if(timer > 0){
            timer -= dt;
        } else{
            Enemy enemy = new Enemy(0f, 0f, enemySpeed, 3, 1);
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
        // Gets the old player cordinates
        float playerX = player.getPlayerX();
        float playerY = player.getPlayerY();



        int numOfKeysPressed = 0;
        player.setSpeedMult(1);
        if(Gdx.input.isKeyPressed(Input.Keys.W)) numOfKeysPressed += 1;
        if(Gdx.input.isKeyPressed(Input.Keys.A)) numOfKeysPressed += 1;
        if(Gdx.input.isKeyPressed(Input.Keys.S)) numOfKeysPressed += 1;
        if(Gdx.input.isKeyPressed(Input.Keys.D)) numOfKeysPressed += 1;

        // Slightly reduce speed so going diagonal isn't crazy fast
        if(numOfKeysPressed > 1){
            player.setSpeedMult(0.85f);
        }

        // Doing x movement first so you can check for walls
        if(Gdx.input.isKeyPressed(Input.Keys.A)) player.moveLeft(deltaTime);
        if(Gdx.input.isKeyPressed(Input.Keys.D)) player.moveRight(deltaTime);

        // If after moving you contact a wall reset the position
        for(Rectangle w : walls) {
            if (player.getHitBox().overlaps(w)){
                player.setPosition(playerX, playerY);
            }
        }

        playerX = player.getPlayerX();

        // Then doing Y to check for the y-axis walls
        if(Gdx.input.isKeyPressed(Input.Keys.W)) player.moveUp(deltaTime);
        if(Gdx.input.isKeyPressed(Input.Keys.S)) player.moveDown(deltaTime);

        for(Rectangle w : walls){
            if(player.getHitBox().overlaps(w)){
                player.setPosition(playerX, playerY);
            }
        }

        player.setTotalHitBox(player.getPlayerX(), player.getPlayerY());
    }

    public void drawEnemies(){
        for(Enemy e : enemies){
            e.draw(spriteBatch);
        }
    }

    public void drawWalls(){
        for(Rectangle w : walls){
            spriteBatch.draw(wallTexture, w.x, w.y, w.width, w.height);
        }
    }
}
