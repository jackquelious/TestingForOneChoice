package com.runnymede.game;

import java.util.ArrayList;
import java.util.Random;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.*;
import com.badlogic.gdx.utils.viewport.Viewport;

public class MainGame implements ApplicationListener{
    // CONSTANTS:
    private static final float CRIT_MULTIPLIER = 2.5f;
    private static final float ENEMY_SPEED = 1.3f;
    private static final float PLAYER_SPEED = 1.8f;
    private static final float BULLET_SPEED = 3.0f;
    private static final float WORLD_SIZE = 20.0f;
    private static final float PERIODIC_DIFF_INCREMENT = 0.05f;
    private static final float LEVEL_UP_DIFF_MULT = 1.2f;
    private static final float DIFF_TIMER_REFRESH = 1.0f;

    // Tetures
    Texture bulletTexture;
    Texture enemyTexture;


    private SpriteBatch spriteBatch;
    private Viewport viewport;

    private ArrayList<Rectangle> walls;
    private Texture wallTexture;

    private BitmapFont font;

    private ArrayList<Enemy> enemies;
    private float timer;
    private float deltaTime;
    private Player player;
    private GridManager gridManager;
    private Pathfinder pathfinder;

    private ArrayList<DamageText> damageTexts;
    private ArrayList<Projectile> projectiles;
    private ArrayList<Upgrade> upgrades;

    private float totalTime;
    private float difficultyTimer;
    private float difficultyScale;

    public void create() {
        spriteBatch = new SpriteBatch();
        enemies = new ArrayList<Enemy>();
        timer = 0.2f; // for enemy spawns
        player = new Player(10.0f, 10.0f, PLAYER_SPEED, 3);
        viewport = new FitViewport(8, 5);

        walls = new ArrayList<Rectangle>();

        //Initalizes the textures
        wallTexture = new Texture("wallTexture.jpg");
        enemyTexture = new Texture("enemySquare.png");
        bulletTexture = new Texture("bullet.png");


        // Creates the room boundaries
        walls.add(new Rectangle(0, 0, 20, 0.5f));       // Bottom Wall
        walls.add(new Rectangle(0, 19.5f, 20, 0.5f));   // Top Wall
        walls.add(new Rectangle(0, 0, 0.5f, 20));       // Left Wall
        walls.add(new Rectangle(19.5f, 0, 0.5f, 20));   // Right Wall

        // The pillars
        walls.add(new Rectangle(3, 2, 1, 1));
        walls.add(new Rectangle(15, 3, 1, 1));
        walls.add(new Rectangle(17, 12, 1, 1));

        // Initializes the lists of game objects
        projectiles = new ArrayList<Projectile>();
        upgrades = new ArrayList<Upgrade>();
        damageTexts = new ArrayList<DamageText>();

        // Set up the font to work in world units
        font = new BitmapFont();
        font.setUseIntegerPositions(false);

        // Initializes the global difficulty timers
        totalTime = 0;
        difficultyScale = 1.0f;
        difficultyTimer = DIFF_TIMER_REFRESH;

        // Initializes the generator and creates a clean, structured dungeon layout
        DungeonGenerator generator = new DungeonGenerator(60, 60);
        int[][] freshLayout = generator.generateFloor(); // This single call handles everything!

        // Your GridManager instantly maps pathfinding nodes straight to the layout floors!
        gridManager = new GridManager(freshLayout);
        pathfinder = new Pathfinder(gridManager);

        Room start = generator.getStartRoom(); // Drops your player perfectly in the middle of safety
        float tileScale = gridManager.getTileSize();

        // Scale the grid coordinates up to matching world positioning coordinates
        float spawnX = start.getCenterX() * tileScale;
        float spawnY = start.getCenterY() * tileScale;
        player.setPosition(spawnX, spawnY);
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
        font.dispose();
    }

    public void input(float deltaTime){
        doPlayerMovement(deltaTime, gridManager);
        doProjectileInput();
    }



    public void logic(float deltaTime){
        float playerXPos = player.getCenterX();
        float playerYPos = player.getCenterY();
        updateProjectiles(deltaTime);
        updateDamageTexts(deltaTime);
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
        drawDungeonMap(); // Draws the dungeon
        drawProjectiles(); // draws projectiles
        drawUpgrades(); // draws upgrades
        drawDamageTexts(); // draws damageTexts

        spriteBatch.end(); // ends sprite batch
    }

    // Counts down an enemy timer with delta time
    // Spawns an enemy every 5 seconds
    public void doEnemyTimer(float dt){
        if(timer > 0) timer -= dt;
        else{
            Enemy enemy = new Enemy(1.0f, 1.0f, ENEMY_SPEED, 3 * difficultyScale, 1 * difficultyScale, enemyTexture);
            enemies.add(enemy);
            timer = 0.8f;
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

    // Updates all projectiles, handles collisions, and spawns damage texts
    public void updateProjectiles(float deltaTime){
        Random rand = new Random(); // Creates new Random object

        // Iterates through every projectile
        for (int i = projectiles.size() - 1; i >= 0; i--) {
            // Gets the projectile and its hitbox
            Projectile currentProjectile = projectiles.get(i);
            Rectangle projectileHitbox = currentProjectile.getHitBox();

            // Moves projectiles, then checks for wall collisions
            currentProjectile.update(deltaTime, gridManager);

            // Iterates through every enemy to check for collisions
            for (int j = enemies.size() - 1; j >= 0; j--) {
                Enemy currentEnemy = enemies.get(j);
                Rectangle curEnemyHitBox = currentEnemy.getHitBox();

                // Gets the position of the enemy that is used for upgrade and text spawning
                float enemyX = currentEnemy.getCenterXPos();
                float enemyY = currentEnemy.getCenterYPos();

                // Checks collision
                if(projectileHitbox.overlaps(curEnemyHitBox)){
                    // 1. Calculate Damage
                    boolean isCrit = (rand.nextDouble() <= currentProjectile.getCritChance());
                    float damage = currentProjectile.getDamage() * (isCrit ? CRIT_MULTIPLIER : 1.0f);

                    // Applies damage
                    currentEnemy.takeDamage(damage);

                    // Spawns the damage text
                    boolean isLethal = !currentEnemy.isAlive(); // Creates a new boolean that detects if the damage was lethal
                    DamageText text = new DamageText(damage, isCrit, isLethal, enemyX, enemyY);
                    damageTexts.add(text);

                    // Heals the player by calculated amount
                    float amtHealed = damage * currentProjectile.getLifeSteal();
                    player.heal(amtHealed);

                    // Decreases bullet pierce by 1
                    currentProjectile.setPierce(currentProjectile.getPierce() - 1);

                    // If the pierce reaches 0, despawn the projectile
                    if(currentProjectile.getPierce() <= 0){
                        currentProjectile.setActive(false);
                        currentProjectile.setPosition(-55, 55);
                    }
                }

                // Checks if enemy died during this collision
                if (!currentEnemy.isAlive()) {
                    enemies.remove(currentEnemy);
                    rollUpgradeSpawn(enemyX, enemyY);
                }

                // Checks if projectile was deactivated during this collision
                if (!currentProjectile.getActive()) {
                    projectiles.remove(currentProjectile);
                    break;
                }
            }
        }
    }

    public void updateDamageTexts(float deltaTime){
        for(int i = damageTexts.size() - 1; i >= 0; i--){
            DamageText dt = damageTexts.get(i);
            dt.update(deltaTime);
            if(!dt.getActive()){
                damageTexts.remove(i);
            }
        }
    }

    public void handleDifficultyScaling(float deltaTime){
        totalTime += deltaTime;
        difficultyTimer -= deltaTime;
        if(difficultyTimer <= 0){
            difficultyScale += PERIODIC_DIFF_INCREMENT;
            difficultyTimer = DIFF_TIMER_REFRESH;
        }
    }

    public void progressLevel(){
        difficultyScale *= LEVEL_UP_DIFF_MULT;
    }


    public void doPlayerMovement(float deltaTime, GridManager gridManager){
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
        if(Gdx.input.isKeyPressed(Input.Keys.A)) player.moveLeft(deltaTime, gridManager);
        if(Gdx.input.isKeyPressed(Input.Keys.D)) player.moveRight(deltaTime, gridManager);

        // Then doing Y to check for the y-axis walls
        if(Gdx.input.isKeyPressed(Input.Keys.W)) player.moveUp(deltaTime, gridManager);
        if(Gdx.input.isKeyPressed(Input.Keys.S)) player.moveDown(deltaTime, gridManager);

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
                player.getBulletBounces(),spawnX, spawnY, mousePos.x, mousePos.y, bulletTexture);

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

    private void drawDungeonMap() {
        Node[][] grid = gridManager.getGrid();
        float size = gridManager.getTileSize();

        // Scan through the entire grid width and height to render tiles
        for (int x = 0; x < gridManager.getGridColumns(); x++) {
            for (int y = 0; y < gridManager.getGridRows(); y++) {
                // If the pathfinder node is not walkable, draw a solid wall texture
                if (!grid[x][y].isWalkable) {
                    spriteBatch.draw(wallTexture, x * size, y * size, size, size);
                }
            }
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
    public void drawDamageTexts() {
        for (DamageText dt : damageTexts) {
            dt.draw(spriteBatch, font);
        }
    }
}
