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
    /// CONSTANTS:
    private static final float CRIT_MULTIPLIER = 2.5f;

    private static final float PLAYER_SPEED = 2.1f;
    private static final float PERIODIC_DIFF_INCREMENT = 0.03f;
    private static final float LEVEL_UP_DIFF_MULT = 1.2f;
    private static final float DIFF_TIMER_REFRESH = 0.8f;

    // Enemy constants
    private static final float ENEMY_TIMER_REFRESH = 0.8f;
    private static final float ENEMY_BEHAVIOUR_RADIUS = 0.4f;

    // Melee constants
    private static final float MELEE_BASE_SPEED = 1.6f;
    private static final float MELEE_BASE_DAMAGE = 1.0f;
    private static final float MELEE_BASE_HEALTH = 3.0f;

    // RANGED CONSTANTS
    private static final float RANGED_BASE_SPEED = 0.8f;
    private static final float RANGED_BASE_DAMAGE = 1.0f;
    private static final float RANGED_BASE_HEALTH = 3.0f;

    private static final float RANGED_BASE_BULLET_SPEED = 4.5f;
    private static final float RANGED_BASE_BULLET_SIZE = 0.2f;
    private static final float RANGED_BASE_SHOOT_RANGE = 3.2f;

    private static final float RANGED_BASE_RETREAT_TIME = 1.5f;
    private static final float RANGED_BASE_ATTACK_TIME = 0.8f;

    // SENTRY CONSTANTS
    private static final float SENTRY_BASE_DAMAGE = 1.0f;
    private static final float SENTRY_BASE_HEALTH = 3.0f;
    private static final float SENTRY_COOLDOWN_TIME = 1.2f;
    private static final float SENTRY_FIRE_TIME = 3.5f;
    private static final float SENTRY_LASER_DURATION = 0.3f;



    // Tetures
    Texture bulletTexture;
    Texture enemyTexture;
    Texture sentryTexture;
    Texture laserTexture;


    private SpriteBatch spriteBatch;
    private Viewport viewport;

    private ArrayList<Rectangle> walls;
    private Texture wallTexture;

    private BitmapFont font;


    private float timer;
    private float deltaTime;
    private Player player;
    private GridManager gridManager;
    private Pathfinder pathfinder;
    private Room currentRoom;
    private ArrayList<Room> rooms;

    private ArrayList<Enemy> enemies;
    private ArrayList<DamageText> damageTexts;
    private ArrayList<Projectile> projectiles;
    private ArrayList<Upgrade> upgrades;

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
        sentryTexture = new Texture("sentrySquare.png");
        laserTexture = new Texture("laserTexture.png");


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
        rooms = new ArrayList<Room>();

        // Set up the font to work in world units
        font = new BitmapFont();
        font.setUseIntegerPositions(false);

        difficultyScale = 1.0f;
        difficultyTimer = DIFF_TIMER_REFRESH;

        // Initializes the generator and creates a clean, structured dungeon layout
        DungeonGenerator generator = new DungeonGenerator(60, 60);
        int[][] freshLayout = generator.generateFloor(); // This single call handles everything!

        // Your GridManager instantly maps pathfinding nodes straight to the layout floors!
        gridManager = new GridManager(freshLayout);
        pathfinder = new Pathfinder(gridManager);

        for(Room r : generator.getPlacedRooms()){
            r.findDoors(freshLayout);
            r.createTriggerBox(gridManager.getTileSize());
            rooms.add(r);
        } // Finds each door and adds it to the list

        Room start = generator.getStartRoom(); // Drops your player perfectly in the middle of safety
        currentRoom = start; // Initializes the current room

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
        updatePlayerCurrentRoom(rooms);
        updateProjectiles(deltaTime);
        updateDamageTexts(deltaTime);
        handleRoomStateLogic();
        updateAllEnemies(deltaTime, playerXPos, playerYPos);
        doUpgrade(deltaTime);
        updateCamera();
        handleDifficultyScaling(deltaTime);

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

    private void spawnEnemiesForRoom(Room room) {
        java.util.Random rand = new java.util.Random();
        float tileSize = gridManager.getTileSize();

        // Determine population scale based on room difficulty/type
        int enemyCount = rand.nextInt(3, 6);

        int spawned = 0;
        int safetyAttempts = 0;

        // Keep trying until we successfully place our quota, or hit a safety limit
        while (spawned < enemyCount && safetyAttempts < 50) {
            safetyAttempts++;

            // Pick a random tile coordinate inside the inner floor space (avoiding the outer walls)
            int randomTileX = rand.nextInt(room.x + 1, room.x + room.width - 1);
            int randomTileY = rand.nextInt(room.y + 1, room.y + room.height - 1);

            // Verify the selected node isn't a central obstacle pillar
            if (gridManager.getGrid()[randomTileX][randomTileY].isWalkable) {
                // Convert tile indices back into world floats centered on the tile
                float worldX = (randomTileX * tileSize) + (tileSize / 2f);
                float worldY = (randomTileY * tileSize) + (tileSize / 2f);

                if (room.type == Room.RoomType.BOSS) {
                    // Future home of our Ultrakill Virtue!
                    // For now, let's spawn a super-powered Ranged Enemy as a placeholder
                    enemies.add(new RangedEnemy(worldX, worldY, RANGED_BASE_SPEED, 50f, 10f,
                        enemyTexture, 3.5f, 1.5f, 1.5f, 5f, 0.2f, bulletTexture));
                } else {
                    int spawnRoll = rand.nextInt(0, 100);
                    if(spawnRoll < 30) {
                        enemies.add(new SentryEnemy(worldX, worldY,SENTRY_BASE_HEALTH, SENTRY_BASE_DAMAGE, SENTRY_FIRE_TIME,
                            SENTRY_COOLDOWN_TIME, SENTRY_LASER_DURATION, sentryTexture, laserTexture));
                    } else {
                        // Spawn normal kiting grunts
                        enemies.add(new RangedEnemy(worldX, worldY, RANGED_BASE_SPEED, RANGED_BASE_HEALTH * difficultyScale, RANGED_BASE_DAMAGE * difficultyScale,
                            enemyTexture, 3.5f, 1.5f, 1.5f, 5f, 0.2f, bulletTexture));
                    }
                }
                spawned++;
            }
        }
    }

    // This method moves the enemies toward the player
    public void updateAllEnemies(float dt, float playerX, float playerY){
        // Iterates through every enemy
        for(Enemy e : enemies){
            e.updateAi(dt, playerX, playerY, ENEMY_BEHAVIOUR_RADIUS, gridManager, pathfinder, this);
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

            // If the projectile is owned by player, detect enemy collisions
            if(currentProjectile.getOwner()){
                // Iterates through every enemy to check for collisions
                for (int j = enemies.size() - 1; j >= 0; j--) {
                    Enemy currentEnemy = enemies.get(j);
                    Rectangle curEnemyHitBox = currentEnemy.getHitBox();

                    // Gets the position of the enemy that is used for upgrade and text spawning
                    float enemyX = currentEnemy.getCenterXPos();
                    float enemyY = currentEnemy.getCenterYPos();

                    // Checks collision
                    if (projectileHitbox.overlaps(curEnemyHitBox)) {
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
                        if (currentProjectile.getPierce() <= 0) {
                            currentProjectile.setActive(false);
                            currentProjectile.setPosition(-55, 55);
                        }
                    }

                    // Checks if enemy died during this collision
                    if (!currentEnemy.isAlive()) {
                        enemies.remove(currentEnemy);
                        rollUpgradeSpawn(enemyX, enemyY);
                    }
                }
            } else { // otherwise detect player collisions
                if (player.getHitBox().overlaps(currentProjectile.getHitBox())) {
                    player.takeDamage(currentProjectile.getDamage());
                    currentProjectile.setPierce(0);

                    // Adds a damage text
                    // FIXME: change lethal stuff after adding player death
                    DamageText text = new DamageText(currentProjectile.getDamage(), false, false, player.getX(), player.getY());
                    damageTexts.add(text);

                    // If the pierce reaches 0, despawn the projectile
                    if (currentProjectile.getPierce() <= 0) {
                        currentProjectile.setActive(false);
                        currentProjectile.setPosition(-55, 55);
                    }

                }

            }


            // Checks if projectile was deactivated during this collision
            if (!currentProjectile.getActive()) {
                projectiles.remove(currentProjectile);
                break;
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
            Projectile newBullet = new Projectile(true, player.getBulletSpeed(), player.getBulletSize(),
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

    /**
     * Examines the player's current tile position and updates the active room reference.
     * @param placedRooms The list of rooms generated by the DungeonGenerator.
     */
    private void updatePlayerCurrentRoom(ArrayList<Room> placedRooms) {
        // 1. Get the tile size from your grid manager
        float tileSize = gridManager.getTileSize();

        // 2. Convert the player's center world coordinates into grid tile integers
        int playerTileX = (int) (player.getCenterX() / tileSize);
        int playerTileY = (int) (player.getCenterY() / tileSize);

        // 3. Loop through your rooms to find a structural boundary match
        for (Room room : placedRooms) {
            if (playerTileX >= room.x && playerTileX < (room.x + room.width) &&
                playerTileY >= room.y && playerTileY < (room.y + room.height)) {

                // Player is inside this room!
                currentRoom = room;
                return; // Exit early once found
            }
        }
    }

    private void handleRoomStateLogic(){
        if (currentRoom == null) return;

        // Trigger encounter if the player steps into the room's inner hitbox
        if (currentRoom.getState() == Room.RoomState.UNVISITED) {
            if (currentRoom.type == Room.RoomType.START) {
                currentRoom.setState(Room.RoomState.CLEARED);
            } else {
                // THE NEW FIX: Simple bounding box collision!
                if (player.getHitBox().overlaps(currentRoom.getTriggerBox())) {
                    currentRoom.lockDoors(gridManager);
                    spawnEnemiesForRoom(currentRoom);
                }
            }
        }

        // Check if a locked room has been cleared of all threats
        if (currentRoom.getState() == Room.RoomState.LOCKED && enemies.isEmpty()) {
            currentRoom.unlockDoors(gridManager);
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

    // Methods that update the private objects so other classes can edit them
    public Player getPlayer() {return player;}
    public void addProjectile(Projectile p){projectiles.add(p);}
    public void removeProjectile(Projectile p){projectiles.remove(p);}
    public void addUpgrade(Upgrade up){upgrades.add(up);}
    public void removeUpgrade(Upgrade up){upgrades.remove(up);}
    public void addEnemy(Enemy enemy){enemies.add(enemy);}
    public void removeEnemy(Enemy enemy){enemies.remove(enemy);}
    public void addDamageText(DamageText dt){damageTexts.add(dt);}
    public void removeDamageText(DamageText dt){damageTexts.remove(dt);}
}
