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
    // CONSTANTS:
    private static final float critMultiplier = 2.5f;
    private static final float enemySpeed = 1.3f;
    private static final float playerSpeed = 1.8f;
    private static final float bulletSpeed = 3.0f;
    private static final float worldSize = 20.0f;


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

    private ArrayList<Projectile> projectiles;
    private ArrayList<Upgrade> upgrades;

    public void create() {
        spriteBatch = new SpriteBatch();
        enemies = new ArrayList<Enemy>();
        timer = 0.1f;
        player = new Player(10.0f, 10.0f, playerSpeed, 3);
        viewport = new FitViewport(8, 5);

        walls = new ArrayList<Rectangle>();
        wallTexture = new Texture("wallTexture.jpg");

        // Creates the room boundaries
        walls.add(new Rectangle(0, 0, 20, 0.5f));       // Bottom Wall
        walls.add(new Rectangle(0, 19.5f, 20, 0.5f));   // Top Wall
        walls.add(new Rectangle(0, 0, 0.5f, 20));       // Left Wall
        walls.add(new Rectangle(19.5f, 0, 0.5f, 20));   // Right Wall

        // The pillars
        walls.add(new Rectangle(3, 2, 1, 1));
        walls.add(new Rectangle(15, 3, 1, 1));
        walls.add(new Rectangle(17, 12, 1, 1));

        //Intiantiates gridManager and pathfinder
        gridManager = new GridManager(walls, worldSize, worldSize);
        pathfinder  = new Pathfinder(gridManager);

        projectiles = new ArrayList<Projectile>();
        upgrades = new ArrayList<Upgrade>();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    // Main method that runs periodically
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
        doProjectileInput();
    }



    public void logic(float deltaTime){
        float playerXPos = player.getCenterX();
        float playerYPos = player.getCenterY();
        updateProjectiles(deltaTime);
        doEnemyTimer(deltaTime);
        moveEnemiesTowardPlayer(deltaTime, playerXPos, playerYPos);
        doUpgrade(deltaTime);
        updateCamera();

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
        drawUpgrades(); // draws upgrades

        spriteBatch.end(); // ends sprite batch
    }

    // Counts down an enemy timer with delta time
    // Spawns an enemy every 5 seconds
    public void doEnemyTimer(float dt){
        if(timer > 0) timer -= dt;
        else{
            Enemy enemy = new Enemy(1.0f, 1.0f, enemySpeed, 3, 1);
            enemies.add(enemy);
            timer = 0.5f;
        }
    }

    // This method moves the enemies toward the player
    public void moveEnemiesTowardPlayer(float dt, float playerX, float playerY){
        // Iterates through every enemy
        for(Enemy e : enemies){
            // gets the cordinates
            float enemyCenterX = e.getCenterXPos();
            float enemyCenterY = e.getCenterYPos();

            // Checks if there are nearby obstacles
            // If there are the enemy will use node based tracking otherwise it just uses direct movement
            if (!e.areWallsNearby(enemyCenterX, enemyCenterY, 0.4f, gridManager)) {
                // Direct tracking logic
                e.moveTowardsPoint(dt, playerX, playerY);
                // Clears the current node path
                if (e.getCurrentPath() != null) e.getCurrentPath().clear();
                continue;
            }
            // Otherwise use the navigation method
            e.navigateTowardsPlayer(dt, playerX, playerY, pathfinder);
        }
    }

    // Updates all projectiles and checks for collision
    public void updateProjectiles(float deltaTime){
        Random rand = new Random(); // Creates new Random object

        // Iterates through every projectile
        for (int i = projectiles.size() - 1; i >= 0; i--) {
            // Gets the projectile and it's hitbox
            Projectile currentProjectile = projectiles.get(i);
            Rectangle projectileHitbox = currentProjectile.getHitBox();

            // Moves projectiles, then checks for collisions and updates accordingly
            currentProjectile.update(deltaTime, walls); // Moves projectiles and handles wall collision

            // Iterates through every enemy to check for collisions
            for (int j = enemies.size() - 1; j >= 0; j--) {
                Enemy currentEnemy = enemies.get(j);
                Rectangle curEnemyHitBox = currentEnemy.getHitBox();

                // Gets the position of the enemy that is used for upgrade spawning
                float deathX = currentEnemy.getCenterXPos();
                float deathY = currentEnemy.getCenterYPos();

                // Checks collision
                if(projectileHitbox.overlaps(curEnemyHitBox)){
                    boolean isCrit = (rand.nextDouble() <= currentProjectile.getCritChance()); // Rolls for a crit
                    float damage = currentProjectile.getDamage() * (isCrit ? critMultiplier : 1.0f); // Gets the damage delt
                    currentEnemy.takeDamage(damage); // makes the enemy take damage
                    float amtHealed = damage * currentProjectile.getLifeSteal(); // calcs the amt healed

                    currentProjectile.setPierce(currentProjectile.getPierce() - 1); // subtracts pierce

                    // If the pierce reaches 0, despawn the projectile
                    if(currentProjectile.getPierce() <= 0){
                        System.out.println("No Pierce");
                        currentProjectile.setActive(false);
                        currentProjectile.setPosition(-55, 55);
                    }
                }

                if (!currentEnemy.isAlive()) {
                    enemies.remove(currentEnemy);
                    rollUpgradeSpawn(deathX, deathY);
                }
                if (!currentProjectile.getActive()) {
                    projectiles.remove(currentProjectile);
                    System.out.println("REMOVED DUE TO UNACTIVE");
                    break;
                }
            }
        }
    }


    public void doPlayerMovement(float deltaTime){
        // Gets the old player cordinates
        float playerX = player.getX();
        float playerY = player.getY();


        // Gets the number of keys pressed
        int numOfKeysPressed = 0;
        player.setSpeedMult(1);
        if(Gdx.input.isKeyPressed(Input.Keys.W)) numOfKeysPressed += 1;
        if(Gdx.input.isKeyPressed(Input.Keys.A)) numOfKeysPressed += 1;
        if(Gdx.input.isKeyPressed(Input.Keys.S)) numOfKeysPressed += 1;
        if(Gdx.input.isKeyPressed(Input.Keys.D)) numOfKeysPressed += 1;

        // Slightly reduce speed so going diagonal isn't crazy fast
        if(numOfKeysPressed > 1) player.setSpeedMult(0.85f);
        else player.setSpeedMult(1.0f);


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

        // Updates the hit box at the end
        player.setTotalHitBox(player.getX(), player.getY());
    }

    public void doProjectileInput(){
        // Triggers on left click
        if (com.badlogic.gdx.Gdx.input.isButtonJustPressed(com.badlogic.gdx.Input.Buttons.LEFT)) {

            // gets the x and y positions in a vector container
            com.badlogic.gdx.math.Vector3 mousePos = new com.badlogic.gdx.math.Vector3(
                com.badlogic.gdx.Gdx.input.getX(), com.badlogic.gdx.Gdx.input.getY(), 0);

            // converts the pixel inputs into game units (8x5)
            viewport.unproject(mousePos);

            // Spawns the bullet at the middle of the player
            float spawnX = player.getCenterX();
            float spawnY = player.getCenterY();

            // Creates new bullet object
            Projectile newBullet = new Projectile(player.getBulletSpeed(), player.getBulletSize(),
                player.getCritChance(), player.getLifeSteal(), player.getDamage(), player.getPierce(),
                player.getBulletBounces(),spawnX, spawnY, mousePos.x, mousePos.y);

            // Adds it to the list
            projectiles.add(newBullet);
        }
    }

    // On enemy death an upgrade has a chance to be spawned
    public void rollUpgradeSpawn(float spawnX, float spawnY){
        Random rand = new Random();
        if(rand.nextDouble() <= 1){
            Upgrade newUpgrade = new Upgrade(spawnX, spawnY, 5f);
            upgrades.add(newUpgrade);
        }

    }

    public void doUpgrade(float deltaTime){
        for(int i = upgrades.size() - 1; i >= 0; i--){
            Upgrade u = upgrades.get(i);
            if(u.tick(deltaTime)){
                upgrades.remove(u);
                return;
            }
            if(player.getHitBox().overlaps(u.getHitBox())){
                u.collect(player);
                upgrades.remove(u);
            }
        }
    }

    // Updates the camera so it follows the player
    public void updateCamera(){
        com.badlogic.gdx.graphics.Camera cam = viewport.getCamera(); // Gets camera object
        cam.position.set(player.getCenterX(), player.getCenterY(), 0); // Moves the camera
        cam.update(); // Updates cam math and logic
    }

    // DRAW METHODS:
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

    public void drawUpgrades(){
        for(Upgrade up : upgrades){
            up.draw(spriteBatch);
        }
    }
}
