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
    private static final float bulletSpeed = 3.0f;


    private SpriteBatch spriteBatch;
    private Viewport viewport;

    private ArrayList<Rectangle> walls;
    private Texture wallTexture;

    private ArrayList<Enemy> enemies;
    private float timer;
    private float deltaTime;
    private Player player;
    private GridManager gridManager;
    private Pathfinder pathfinder;

    private Texture bulletTexture;
    private ArrayList<Projectile> projectiles;

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

        //Intiantiates gridManager and pathfinder
        gridManager = new GridManager(walls);
        pathfinder  = new Pathfinder(gridManager);

        bulletTexture = new Texture("bullet.png");
        projectiles = new ArrayList<Projectile>();
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
        bulletTexture.dispose();
    }

    public void input(float deltaTime){
        doPlayerMovement(deltaTime);
        doProjectileInput();
    }



    public void logic(float deltaTime){
        float playerXPos = player.getCenterX();
        float playerYPos = player.getCenterY();
        updateProjectiles(deltaTime);
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
        drawProjectiles(); // draws projectiles

        spriteBatch.end(); // ends sprite batch
    }

    public void doEnemyTimer(float dt){
        if(timer > 0){
            timer -= dt;
        } else{
            Enemy enemy = new Enemy(1.0f, 1.0f, enemySpeed, 3, 1);
            enemies.add(enemy);
            timer = 5;
        }
    }

    public void moveEnemiesTowardPlayer(float dt, float playerX, float playerY){
        for(Enemy e : enemies){
            float enemyCenterX = e.getCenterXPos();
            float enemyCenterY = e.getCenterYPos();
            if (!e.areWallsNearby(enemyCenterX, enemyCenterY, 0.4f, gridManager)) {
                // Direct tracking logic
                e.moveTowardsPoint(dt, playerX, playerY);
                if (e.getCurrentPath() != null) e.getCurrentPath().clear();
                continue;
            }
            e.navigateTowardsPlayer(dt, playerX, playerY, pathfinder);
        }
    }

    public void updateProjectiles(float deltaTime){
        for (int i = projectiles.size() - 1; i >= 0; i--) {
            Projectile currentProjectile = projectiles.get(i);
            Rectangle projectileHitbox = currentProjectile.getHitBox();
            currentProjectile.update(deltaTime);

            // Checks for enemies and damages them on collision
            for(int e = enemies.size() - 1; e >= 0; e --){
                // Gets the enemy and it's hitbox
                Enemy currentEnemy =  enemies.get(e);
                Rectangle curEnemyHitbox = currentEnemy.getHitBox();

                // WHen thiey overlap remove projectile and damage the enemy
                if(curEnemyHitbox.overlaps(projectileHitbox)){
                    projectiles.remove(currentProjectile);
                    currentEnemy.takeDamage(2);
                    if(!currentEnemy.isAlive()){
                        enemies.remove(currentEnemy);
                    }
                    System.out.println("hit");
                }
            }

            for(Rectangle w : walls) {
                if(currentProjectile.getHitBox().overlaps(w)) {
                    projectiles.remove(currentProjectile);
                    break;
                }
            }
        }
    }

    public void doPlayerMovement(float deltaTime){
        // Gets the old player cordinates
        float playerX = player.getX();
        float playerY = player.getY();



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

        playerX = player.getX();

        // Then doing Y to check for the y-axis walls
        if(Gdx.input.isKeyPressed(Input.Keys.W)) player.moveUp(deltaTime);
        if(Gdx.input.isKeyPressed(Input.Keys.S)) player.moveDown(deltaTime);

        for(Rectangle w : walls){
            if(player.getHitBox().overlaps(w)){
                player.setPosition(playerX, playerY);
            }
        }

        player.setTotalHitBox(player.getX(), player.getY());
    }

    public void doProjectileInput(){
        // Triggers on left click
        if (com.badlogic.gdx.Gdx.input.isButtonJustPressed(com.badlogic.gdx.Input.Buttons.LEFT)) {

            // gets the x and y positions in a vector container
            com.badlogic.gdx.math.Vector3 mousePos = new com.badlogic.gdx.math.Vector3(
                com.badlogic.gdx.Gdx.input.getX(), com.badlogic.gdx.Gdx.input.getY(), 0);

            // converts the pixel inputs into game units (8x5)
            viewport.getCamera().unproject(mousePos);

            // 3. Spawn the bullet from the center of the player towards the mouse
            float spawnX = player.getX() + (player.getSprite().getWidth() / 2);
            float spawnY = player.getY() + (player.getSprite().getHeight() / 2);

            Projectile newBullet = new Projectile(bulletSpeed, spawnX, spawnY, mousePos.x, mousePos.y, bulletTexture);

            projectiles.add(newBullet);
        }
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

    public void drawProjectiles(){
        for(Projectile p : projectiles){
            p.draw(spriteBatch);
        }
    }
}
