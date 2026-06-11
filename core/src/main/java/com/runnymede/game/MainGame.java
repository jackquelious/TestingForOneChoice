package com.runnymede.game;

import java.util.ArrayList;
import java.util.Random;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.*;
import com.badlogic.gdx.utils.viewport.Viewport;

public class MainGame implements ApplicationListener {
    /// CONSTANTS:
    private static final float CRIT_MULTIPLIER = 2.5f;

    //Player constants
    private static final float PLAYER_BASE_SPEED = 2.1f;
    private static final int PLAYER_BASE_HEALTH = 12;
    private static final float PLAYER_DASH_DURACTION = 0.18f;
    private static final float PLAYER_DASH_COOLDOWN = 1.2f;
    private static final float PLAYER_DASH_SPEED_MULTIPLIER = 3.5f;

    private static final float PERIODIC_DIFF_INCREMENT = 0.02f;
    private static final float LEVEL_UP_DIFF_MULT = 1.2f;
    private static final float DIFF_TIMER_REFRESH = 1.2f;

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

    // Drone Constants
    private static final float DRONE_BASE_DAMAGE = 1.0f;
    private static final float DRONE_BASE_HEALTH = 3.0f;
    private static final float DRONE_BASE_SPEED = 0.8f;
    private static final float DRONE_FLEE_TIME = 1.2f;
    private static final float DRONE_TRACKING_TIME = 2.8f;
    private static final float DRONE_LOCKING_TIME = 0.8f;
    private static final float DRONE_EXPLOSION_TIME = 0.8f;
    private static final float DRONE_EXPLOSION_RADIUS = 0.8f;

    // Textures
    Texture bulletTexture;
    Texture enemyTexture;
    Texture sentryTexture;
    Texture laserTexture;
    Texture droneTexture;
    Texture crosshairTexture;
    Texture explosionTexture;
    Texture portalTexture;
    Texture bossTexture;
    Texture bossTurretTexture;
    Texture skullTexture;
    private Texture wallTexture;

    public enum GameState {PLAYING, UPGRADE_MENU, GAME_OVER}
    private GameState gameState = GameState.PLAYING;

    private SpriteBatch spriteBatch;
    private Viewport viewport;



    private BitmapFont font;
    private BitmapFont menuFont;

    private float timer;
    private float deltaTime;
    private Player player;
    private GridManager gridManager;
    private Pathfinder pathfinder;
    private Room currentRoom;
    private Portal portal;
    private ArrayList<Room> rooms;

    private ArrayList<Enemy> enemies;
    private ArrayList<Enemy> enemiesToSpawn;
    private ArrayList<DamageText> damageTexts;
    private ArrayList<Projectile> projectiles;
    private ArrayList<Upgrade> upgrades;
    private ArrayList<Rectangle> walls;


    private float difficultyScale;
    private float difficultyTimer;

    private int[][] originalLayout;
    private DungeonGenerator currentGenerator;

    public enum LoopUpgrade { LIFESTEAL, DASH, UPGRADED_DASH, SHIELD, BOUNCES }
    private LoopUpgrade[] currentChoices = new LoopUpgrade[3];

    // UI VARIABLES:
    private FitViewport uiViewport;
    private Texture healthBarFrameTexture;
    private Texture solidColorTexture;

    // SOUND TRACKS:
    Music mainThemeSong;
    Music bossFightTheme;

    // Audio Manager Variables:
    private Music currentTrack;
    private Music incomingTrack;
    private boolean isFading = false;
    private float fadeSpeed = 0.5f;

    public void create() {
        spriteBatch = new SpriteBatch();
        enemies = new ArrayList<Enemy>();
        timer = 0.2f;
        player = new Player(10.0f, 10.0f, PLAYER_BASE_SPEED, PLAYER_BASE_HEALTH, PLAYER_DASH_DURACTION, PLAYER_DASH_COOLDOWN, PLAYER_DASH_SPEED_MULTIPLIER);
        viewport = new FitViewport(8, 5);

        walls = new ArrayList<Rectangle>();

        wallTexture = new Texture("wallTexture.jpg");
        enemyTexture = new Texture("enemySquare.png");
        bulletTexture = new Texture("bullet.png");
        sentryTexture = new Texture("sentrySquare.png");
        laserTexture = new Texture("laserTexture.png");
        droneTexture = new Texture("droneTexture.png");
        crosshairTexture = new Texture("crosshairTexture.png");
        explosionTexture = new Texture("explosionTexture.png");
        portalTexture = new Texture("portalTexture.png");
        bossTexture = new Texture("bossTexture.png");
        bossTurretTexture = new Texture("bossTurretTexture.png");
        skullTexture = new Texture("skullTexture.png"); // Boss room indicator

        walls.add(new Rectangle(0, 0, 20, 0.5f));
        walls.add(new Rectangle(0, 19.5f, 20, 0.5f));
        walls.add(new Rectangle(0, 0, 0.5f, 20));
        walls.add(new Rectangle(19.5f, 0, 0.5f, 20));

        walls.add(new Rectangle(3, 2, 1, 1));
        walls.add(new Rectangle(15, 3, 1, 1));
        walls.add(new Rectangle(17, 12, 1, 1));

        projectiles = new ArrayList<Projectile>();
        upgrades = new ArrayList<Upgrade>();
        damageTexts = new ArrayList<DamageText>();
        rooms = new ArrayList<Room>();
        enemiesToSpawn = new ArrayList<Enemy>();

        font = new BitmapFont();
        font.setUseIntegerPositions(false);

        menuFont = new BitmapFont();
        menuFont.setUseIntegerPositions(false);
        menuFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        menuFont.getData().setScale(0.024f);
        menuFont.setColor(com.badlogic.gdx.graphics.Color.WHITE);

        difficultyScale = 1.0f;
        difficultyTimer = DIFF_TIMER_REFRESH;

        // Ui elements:
        // Creates a high-resolution viewport for crisp UI elements
        uiViewport = new FitViewport(800, 480);

        healthBarFrameTexture = new Texture("emptyHealthBarTexture.png");

        // Generate a 1x1 white pixel texture for the green health bar
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        pixmap.fill();
        solidColorTexture = new Texture(pixmap);
        pixmap.dispose();

        buildFloorLayout();

        // Initialize and play music
        mainThemeSong = Gdx.audio.newMusic(Gdx.files.internal("normalSoundtrack.mp3"));
        bossFightTheme = Gdx.audio.newMusic(Gdx.files.internal("bossSoundTrack.mp3"));

        mainThemeSong.setLooping(true);
        bossFightTheme.setLooping(true);

        // Start the game with the main theme
        mainThemeSong.setVolume(1.0f);
        bossFightTheme.setVolume(0.0f);

        currentTrack = mainThemeSong;
        currentTrack.play();

    }

    private void buildFloorLayout() {
        rooms.clear();
        portal = null;

        currentGenerator = new DungeonGenerator(60, 60);
        originalLayout = currentGenerator.generateFloor();

        gridManager = new GridManager(originalLayout);
        pathfinder = new Pathfinder(gridManager);

        for (Room r : currentGenerator.getPlacedRooms()) {
            r.findDoors(originalLayout);
            r.createTriggerBox(gridManager.getTileSize());
            rooms.add(r);

            if (r.type == Room.RoomType.PORTAL) {
                float portalX = r.getCenterX() * gridManager.getTileSize();
                float portalY = r.getCenterY() * gridManager.getTileSize();
                portal = new Portal(portalX, portalY, portalTexture);
            }
        }

        Room start = currentGenerator.getStartRoom();
        currentRoom = start;
        float tileScale = gridManager.getTileSize();
        player.setPosition(start.getCenterX() * tileScale, start.getCenterY() * tileScale);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        uiViewport.update(width, height, true); // Keep UI scaled properly
    }

    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();

        if (gameState == GameState.PLAYING) {
            input(deltaTime);
            logic(deltaTime);
        } else if (gameState == GameState.UPGRADE_MENU) {
            doUpgradeMenuInput();
        } else if (gameState == GameState.GAME_OVER) {
            doGameOverInput();
        }

        draw();
    }

    public void pause() {}
    public void resume() {}

    public void dispose() {
        spriteBatch.dispose();
        font.dispose();
        menuFont.dispose();

        // Dispose UI Textures
        healthBarFrameTexture.dispose();
        solidColorTexture.dispose();

        //Dispose audio
        mainThemeSong.dispose();
        bossFightTheme.dispose();


    }

    public void input(float deltaTime) {
        doPlayerMovement(deltaTime, gridManager);
        doProjectileInput();
        doPortalInput();
    }

    public void logic(float deltaTime) {
        float playerXPos = player.getCenterX();
        float playerYPos = player.getCenterY();

        updatePlayerCurrentRoom(rooms);
        updateProjectiles(deltaTime);
        updateDamageTexts(deltaTime);
        updateMusic(deltaTime);
        handleRoomStateLogic();
        spawnEnemiesInQue();
        updateAllEnemies(deltaTime, playerXPos, playerYPos);

        doUpgrade(deltaTime);
        updateCamera();
        handleDifficultyScaling(deltaTime);

        if (!player.isAlive()) {
            gameState = GameState.GAME_OVER;
        }
    }

    public void draw() {
        // 1. WORLD DRAWING PHASE
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);

        ScreenUtils.clear(0, 0, 0, 1);
        spriteBatch.begin();

        drawDungeonMap();
        drawUpgrades();
        drawEnemies();
        drawPortal();

        if (player.isAlive()) {
            player.draw(spriteBatch);
        }

        drawProjectiles();
        drawDamageTexts();

        if (gameState == GameState.UPGRADE_MENU) {
            drawUpgradeMenu();
        } else if (gameState == GameState.GAME_OVER) {
            drawGameOverMenu();
        }

        spriteBatch.end();

        // 2. STATIC UI DRAWING PHASE
        uiViewport.apply();
        spriteBatch.setProjectionMatrix(uiViewport.getCamera().combined);
        spriteBatch.begin();

        // Only draw the HUD if we are actively playing
        if (gameState == GameState.PLAYING) {
            drawHUD();
        }

        spriteBatch.end();
    }

    // Spawns the enmies when the player enters a room
    private void spawnEnemiesForRoom(Room room) {
        float tileSize = gridManager.getTileSize(); // gets the tile size

        // Checks if it's the boss room
        if (room.type == Room.RoomType.BOSS) {
            // If so spawn a boss enemy in the center of the room

            // Gets the position that the enemy should be spawned (center of room)
            float worldX = (room.getCenterX() * tileSize) + (tileSize / 2f);
            float worldY = (room.getCenterY() * tileSize) + (tileSize / 2f);

            // adds the boss enemy to the spawn list
            enemiesToSpawn.add(new BossEnemy(worldX, worldY,
                bossTexture,         // Fixed: Actual boss texture
                bulletTexture,       // standard bullet
                bossTurretTexture,   // turret square
                droneTexture,        // drone body
                crosshairTexture,    // Fixed: Drone warning circle
                explosionTexture,    // drone blast
                sentryTexture,       // sentry base
                laserTexture));      // sentry laser

            // returns so nothing else spawns
            return;
        }

        // If the room is a portal, spawn nothing
        if(room.type == Room.RoomType.PORTAL){
            return;
        }

        // Normal room spawns
        java.util.Random rand = new java.util.Random(); // creates an instance of random class
        int enemyCount = rand.nextInt(3, 6); // Generates a number of enemies to spawn

        // Tracks the number of enemy spawns, and a safety check of how many times it attempts to spawn an enemy
        int spawned = 0;
        int safetyAttempts = 0;

        // Spawns enemies until either it has tried to many times, or has spawned the number of people
        while (spawned < enemyCount && safetyAttempts < 50) {
            safetyAttempts++; // increaes safety check

            // Gets a random tile in the room
            int randomTileX = rand.nextInt(room.x + 1, room.x + room.width - 1);
            int randomTileY = rand.nextInt(room.y + 1, room.y + room.height - 1);

            // If the tile is walkable spawn an enemy
            if (gridManager.getGrid()[randomTileX][randomTileY].isWalkable) {

                // Gets the spawn position
                float worldX = (randomTileX * tileSize) + (tileSize / 2f);
                float worldY = (randomTileY * tileSize) + (tileSize / 2f);

                // Rolls a number between 0 - 99
                int spawnRoll = rand.nextInt(0, 100);

                // If number is 0 - 29 add a sentry enemy
                if (spawnRoll < 32) {
                    enemiesToSpawn.add(new SentryEnemy(worldX, worldY, SENTRY_BASE_HEALTH, SENTRY_BASE_DAMAGE, SENTRY_FIRE_TIME,
                        SENTRY_COOLDOWN_TIME, SENTRY_LASER_DURATION, sentryTexture, laserTexture));
                // If number is 30 - 49 spawn a drone
                } else if (spawnRoll < 50) {
                    enemiesToSpawn.add(new DroneEnemy(worldX, worldY, DRONE_BASE_SPEED, DRONE_BASE_HEALTH,
                        DRONE_BASE_DAMAGE, droneTexture, crosshairTexture, explosionTexture,
                        DRONE_FLEE_TIME, DRONE_TRACKING_TIME, DRONE_LOCKING_TIME, DRONE_EXPLOSION_RADIUS));

                // If number is 50 - 99 spawn regular ranged enemy
                } else {
                    enemiesToSpawn.add(new RangedEnemy(worldX, worldY, RANGED_BASE_SPEED, RANGED_BASE_HEALTH * difficultyScale, RANGED_BASE_DAMAGE * difficultyScale,
                        enemyTexture, 3.5f, 1.5f, 1.5f, 5f, 0.2f, bulletTexture));
                }
                spawned++; // increase number of spawned enemies
            }
        }
    }

    // Uses the enemies ai method to update all of the enemies
    public void updateAllEnemies(float dt, float playerX, float playerY) {
        for (Enemy e : enemies) {
            e.updateAi(dt, playerX, playerY, ENEMY_BEHAVIOUR_RADIUS, gridManager, pathfinder, this);
        }
    }

    // Updates all projectiles
    public void updateProjectiles(float deltaTime) {
        Random rand = new Random();

        for (int i = projectiles.size() - 1; i >= 0; i--) {
            Projectile currentProjectile = projectiles.get(i);
            Rectangle projectileHitbox = currentProjectile.getHitBox();

            currentProjectile.update(deltaTime, gridManager);

            if (currentProjectile.getOwner()) {
                for (int j = enemies.size() - 1; j >= 0; j--) {
                    Enemy currentEnemy = enemies.get(j);
                    Rectangle curEnemyHitBox = currentEnemy.getHitBox();

                    float enemyX = currentEnemy.getCenterXPos();
                    float enemyY = currentEnemy.getCenterYPos();

                    if (projectileHitbox.overlaps(curEnemyHitBox)) {
                        boolean isCrit = (rand.nextDouble() <= currentProjectile.getCritChance());
                        float damage = currentProjectile.getDamage() * (isCrit ? CRIT_MULTIPLIER : 1.0f);

                        currentEnemy.takeDamage(damage);

                        boolean isLethal = !currentEnemy.isAlive();
                        DamageText text = new DamageText(damage, isCrit, isLethal, enemyX, enemyY);
                        damageTexts.add(text);

                        float amtHealed = damage * currentProjectile.getLifeSteal();
                        player.heal(amtHealed);

                        currentProjectile.setPierce(currentProjectile.getPierce() - 1);

                        if (currentProjectile.getPierce() <= 0) {
                            currentProjectile.setActive(false);
                            currentProjectile.setPosition(-55, 55);
                        }
                    }

                    if (!currentEnemy.isAlive()) {
                        enemies.remove(currentEnemy);
                        // REMOVED: Individual item spawn logic handled on clear frame
                    }
                }
            } else {
                if (!player.hasUpgradedDash || !player.isDashing()) {
                    if (player.getHitBox().overlaps(currentProjectile.getHitBox())) {

                        if (player.takeDamage(currentProjectile.getDamage())) {
                            DamageText text = new DamageText(currentProjectile.getDamage(), false, false, player.getX(), player.getY());
                            damageTexts.add(text);
                        }

                        currentProjectile.setPierce(0);
                        currentProjectile.setActive(false);
                        currentProjectile.setPosition(-55, 55);
                    }
                }
            }

            if (!currentProjectile.getActive()) {
                projectiles.remove(i);
            }
        }
    }

    private void generateUpgradeChoices() {
        ArrayList<LoopUpgrade> pool = new ArrayList<>();
        pool.add(LoopUpgrade.LIFESTEAL);
        pool.add(LoopUpgrade.BOUNCES);
        pool.add(LoopUpgrade.SHIELD);

        if (player.hasDash) {
            pool.add(LoopUpgrade.UPGRADED_DASH);
        } else {
            pool.add(LoopUpgrade.DASH);
        }

        java.util.Collections.shuffle(pool);
        currentChoices[0] = pool.get(0);
        currentChoices[1] = pool.get(1);
        currentChoices[2] = pool.get(2);
    }

    public void updateDamageTexts(float deltaTime) {
        for (int i = damageTexts.size() - 1; i >= 0; i--) {
            DamageText dt = damageTexts.get(i);
            dt.update(deltaTime);
            if (!dt.getActive()) {
                damageTexts.remove(i);
            }
        }
    }

    public void handleDifficultyScaling(float deltaTime) {
        difficultyTimer -= deltaTime;
        if (difficultyTimer <= 0) {
            difficultyScale += PERIODIC_DIFF_INCREMENT;
            difficultyTimer = DIFF_TIMER_REFRESH;
        }
    }

    public void progressLevel() {
        difficultyScale *= LEVEL_UP_DIFF_MULT;
    }

    public void doPlayerMovement(float deltaTime, GridManager gridManager) {
        player.updateDashSystem(deltaTime, gridManager);

        int numOfKeysPressed = 0;
        player.setSpeedMult(1);
        if (Gdx.input.isKeyPressed(Input.Keys.W)) numOfKeysPressed += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) numOfKeysPressed += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) numOfKeysPressed += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) numOfKeysPressed += 1;

        if (numOfKeysPressed > 1) player.setSpeedMult(0.85f);
        else player.setSpeedMult(1.0f);

        if (Gdx.input.isKeyPressed(Input.Keys.A)) player.moveLeft(deltaTime, gridManager);
        if (Gdx.input.isKeyPressed(Input.Keys.D)) player.moveRight(deltaTime, gridManager);
        if (Gdx.input.isKeyPressed(Input.Keys.W)) player.moveUp(deltaTime, gridManager);
        if (Gdx.input.isKeyPressed(Input.Keys.S)) player.moveDown(deltaTime, gridManager);

        player.setTotalHitBox(player.getX(), player.getY());
        player.updateShieldSystem(deltaTime);
    }

    public void doProjectileInput() {
        if (com.badlogic.gdx.Gdx.input.isButtonJustPressed(com.badlogic.gdx.Input.Buttons.LEFT)) {
            com.badlogic.gdx.math.Vector3 mousePos = new com.badlogic.gdx.math.Vector3(
                com.badlogic.gdx.Gdx.input.getX(), com.badlogic.gdx.Gdx.input.getY(), 0);

            viewport.unproject(mousePos);

            float spawnX = player.getCenterX();
            float spawnY = player.getCenterY();

            Projectile newBullet = new Projectile(true, player.getBulletSpeed(), player.getBulletSize(),
                player.getCritChance(), player.getLifeSteal(), player.getDamage(), player.getPierce(),
                player.getBulletBounces(), spawnX, spawnY, mousePos.x, mousePos.y, bulletTexture);

            projectiles.add(newBullet);
        }
    }

    public void doPortalInput() {
        if (portal != null && player.getHitBox().overlaps(portal.getHitBox())) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                gameState = GameState.UPGRADE_MENU;
                generateUpgradeChoices();
                System.out.println("Portal Interacted! Game Paused for Upgrade Menu.");
            }
        }
    }

    public void doUpgradeMenuInput(){
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            player.applyUpgrade(currentChoices[0]);
            advanceToNextFloor();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            player.applyUpgrade(currentChoices[1]);
            advanceToNextFloor();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
            player.applyUpgrade(currentChoices[2]);
            advanceToNextFloor();
        }
    }

    private void doGameOverInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            restartMatch();
        }
    }

    // Spawns two items side-by-side in the center of the room with a 20-second lifespan
    private void spawnRoomRewards(Room room) {
        float tileSize = gridManager.getTileSize();
        float centerWorldX = (room.getCenterX() * tileSize) + (tileSize / 2f);
        float centerWorldY = (room.getCenterY() * tileSize) + (tileSize / 2f);

        upgrades.add(new Upgrade(centerWorldX - 0.4f, centerWorldY, 20f));
        upgrades.add(new Upgrade(centerWorldX + 0.4f, centerWorldY, 20f));
    }

    // Checks how many upgrades are currently active inside the room geometry
    private int getActiveUpgradesInRoom(Room room) {
        float tileSize = gridManager.getTileSize();
        int count = 0;
        for (Upgrade u : upgrades) {
            int tileX = (int)(u.getCenterXPos() / tileSize);
            int tileY = (int)(u.getCenterYPos() / tileSize);
            if (tileX >= room.x && tileX < (room.x + room.width) &&
                tileY >= room.y && tileY < (room.y + room.height) &&
                !u.getType().equals("none")) {
                count++;
            }
        }
        return count;
    }

    // REFACTORED: Safe index-driven clearing to prevent concurrent loop crashes
    public void doUpgrade(float deltaTime){
        for (int i = upgrades.size() - 1; i >= 0; i--){
            Upgrade u = upgrades.get(i);
            if (u.tick(deltaTime)){
                upgrades.remove(i);
                continue;
            }
            if (player.getHitBox().overlaps(u.getHitBox())){
                u.collect(player);
                upgrades.remove(i);
            }
        }
    }

    private void updatePlayerCurrentRoom(ArrayList<Room> placedRooms) {
        float tileSize = gridManager.getTileSize();
        int playerTileX = (int) (player.getCenterX() / tileSize);
        int playerTileY = (int) (player.getCenterY() / tileSize);

        for (Room room : placedRooms) {
            if (playerTileX >= room.x && playerTileX < (room.x + room.width) &&
                playerTileY >= room.y && playerTileY < (room.y + room.height)) {

                // If the player steps into a DIFFERENT room than before
                if (currentRoom != room) {
                    currentRoom = room;

                    // MUSIC TRIGGER:
                    if (currentRoom.type == Room.RoomType.BOSS) {
                        switchTrack(bossFightTheme);
                    } else {
                        switchTrack(mainThemeSong);
                    }
                }
                return;
            }
        }
    }

    private void handleRoomStateLogic() {
        if (currentRoom == null) return;

        if (currentRoom.getState() == Room.RoomState.UNVISITED) {
            if (currentRoom.type == Room.RoomType.START) {
                currentRoom.setState(Room.RoomState.CLEARED);
            } else {
                // FIXED SAFETY LOCK: Only snaps closed if player is inside trigger box AND clear of doorway bounds
                if (player.getHitBox().overlaps(currentRoom.getTriggerBox()) &&
                    !currentRoom.isPlayerCollidingWithDoors(player.getHitBox(), gridManager.getTileSize())) {
                    currentRoom.lockDoors(gridManager);
                    spawnEnemiesForRoom(currentRoom);
                }
            }
        }
        // CHANGED TO ELSE IF, AND ADDED enemiesToSpawn CHECK
        else if (currentRoom.getState() == Room.RoomState.LOCKED && enemies.isEmpty() && enemiesToSpawn.isEmpty()) {
            currentRoom.setState(Room.RoomState.WAITING_FOR_REWARDS);
            spawnRoomRewards(currentRoom);
        }
        // CHANGED TO ELSE IF
        else if (currentRoom.getState() == Room.RoomState.WAITING_FOR_REWARDS) {
            if (getActiveUpgradesInRoom(currentRoom) == 0) {
                currentRoom.unlockDoors(gridManager);
            }
        }
    }

    public void updateCamera(){
        com.badlogic.gdx.graphics.Camera cam = viewport.getCamera();
        cam.position.set(player.getCenterX(), player.getCenterY(), 0);
        cam.update();
    }

    private void advanceToNextFloor() {
        progressLevel();
        projectiles.clear();
        upgrades.clear();
        damageTexts.clear();
        enemies.clear();

        buildFloorLayout();
        gameState = GameState.PLAYING;
        System.out.println("Floor Cleared! Entering the next level...");
    }

    private void restartMatch() {
        System.out.println("Reviving player and rebuilding current floor layout...");

        projectiles.clear();
        upgrades.clear();
        damageTexts.clear();
        enemies.clear();



        player.resetStats(PLAYER_BASE_SPEED, PLAYER_BASE_HEALTH);
        difficultyScale = 1.0f;

        buildFloorLayout();
        gameState = GameState.PLAYING;
    }

    private void spawnEnemiesInQue(){
        if(!enemiesToSpawn.isEmpty()){
            enemies.addAll(enemiesToSpawn);
            enemiesToSpawn.clear();
        }
    }

    // DRAW METHODS:
    public void drawEnemies(){
        for (Enemy e : enemies){
            e.draw(spriteBatch);
        }
    }

    private void drawDungeonMap() {
        Node[][] grid = gridManager.getGrid();
        float size = gridManager.getTileSize();

        // Draws the walls
        for (int x = 0; x < gridManager.getGridColumns(); x++) {
            for (int y = 0; y < gridManager.getGridRows(); y++) {
                if (!grid[x][y].isWalkable) {
                    spriteBatch.draw(wallTexture, x * size, y * size, size, size);
                }
            }
        }

        // Draws the skulls sprite in the boss room
        for (Room room : rooms) {
            if (room.type == Room.RoomType.BOSS) {
                float centerX = room.getCenterX() * size;
                float centerY = room.getCenterY() * size;
                spriteBatch.draw(skullTexture, centerX - 0.5f, centerY - 0.5f, 1f, 1f);
            }
        }
    }

    public void drawProjectiles(){
        for (Projectile p : projectiles){
            p.draw(spriteBatch);
        }
    }

    public void drawUpgrades(){
        for (Upgrade up : upgrades){
            up.draw(spriteBatch);
        }
    }

    public void drawDamageTexts() {
        for (DamageText dt : damageTexts) {
            dt.draw(spriteBatch, font);
        }
    }

    public void drawPortal() {
        if (portal != null) {
            portal.draw(spriteBatch);
        }
    }

    public void drawUpgradeMenu(){
        menuFont.draw(spriteBatch, "CHOOSE YOUR UPGRADE (Press 1, 2, or 3)", player.getCenterX() - 4.1f, player.getCenterY() + 2f);
        menuFont.draw(spriteBatch, "1: " + currentChoices[0].name(), player.getCenterX() - 2f, player.getCenterY() + 0.5f);
        menuFont.draw(spriteBatch, "2: " + currentChoices[1].name(), player.getCenterX() - 2f, player.getCenterY() - 0.5f);
        menuFont.draw(spriteBatch, "3: " + currentChoices[2].name(), player.getCenterX() - 2f, player.getCenterY() - 1.5f);
    }

    private void drawGameOverMenu() {
        menuFont.draw(spriteBatch, "GAME OVER", player.getCenterX() - 0.8f, player.getCenterY() + 0.8f);
        menuFont.draw(spriteBatch, "Press 'R' to Restart from This Floor Layout", player.getCenterX() - 2.6f, player.getCenterY() - 0.2f);
    }

    private void drawHUD() {
        // Base coordinates for the bottom-left corner of the HUD
        float hudX = 10;
        float hudY = 8;

        // The size we want to draw the empty frame
        float frameWidth = 160;
        float frameHeight = 90;

        // Calculate the player's health percentage
        float healthPercent = (float) player.getHealth() / player.getMaxHealth();
        healthPercent = Math.max(0, healthPercent); // Prevents drawing a negative width if health drops below 0

        // Variable offsets
        float greenOffsetX = 47; // Pushes the green bar right, past the red heart
        float greenOffsetY = 34; // Pushes the green bar up from the bottom edge
        float maxGreenWidth = 100; // The maximum width of the green bar when at 100% health
        float greenHeight = 26;  // The thickness of the green bar

        // Calculate the dynamic width based on current health
        float currentGreenWidth = maxGreenWidth * healthPercent;

        // Draws the health bar
        spriteBatch.setColor(com.badlogic.gdx.graphics.Color.GREEN); // Tint the white pixel green
        spriteBatch.draw(solidColorTexture, hudX + greenOffsetX, hudY + greenOffsetY, currentGreenWidth, greenHeight);

        // resets the sprite batch tint
        spriteBatch.setColor(com.badlogic.gdx.graphics.Color.WHITE);

        // Draws the health bar frame
        spriteBatch.draw(healthBarFrameTexture, hudX, hudY, frameWidth, frameHeight);
    }

    // Safely queues a track change if it isn't already playing
    private void switchTrack(Music newTrack) {
        if (currentTrack == newTrack || incomingTrack == newTrack) return;
        incomingTrack = newTrack;
        isFading = true;
    }

    // Handles the cross-fade logic every frame
    private void updateMusic(float deltaTime) {
        // Step 1: Fade out the old track
        if (isFading && incomingTrack != null) {
            float currentVol = currentTrack.getVolume();
            float newVol = currentVol - (fadeSpeed * deltaTime);

            if (newVol <= 0) {
                // Fade out complete -> Swap to the new track
                currentTrack.stop();
                currentTrack = incomingTrack;
                currentTrack.setVolume(0f);
                currentTrack.play();

                isFading = false;
                incomingTrack = null;
            } else {
                currentTrack.setVolume(newVol);
            }
        }
        // Step 2: Fade in the new track
        else if (!isFading && currentTrack.getVolume() < 1.0f) {
            float newVol = currentTrack.getVolume() + (fadeSpeed * deltaTime);
            if (newVol > 1.0f) newVol = 1.0f; // Cap volume at 1.0 (100%)
            currentTrack.setVolume(newVol);
        }
    }

    public Player getPlayer() {return player;}
    public void addProjectile(Projectile p){projectiles.add(p);}
    public void removeProjectile(Projectile p){projectiles.remove(p);}
    public void addUpgrade(Upgrade up){upgrades.add(up);}
    public void removeUpgrade(Upgrade up){upgrades.remove(up);}
    public void addEnemy(Enemy enemy){enemiesToSpawn.add(enemy);}
    public void removeEnemy(Enemy enemy){enemies.remove(enemy);}
    public void addDamageText(DamageText dt){damageTexts.add(dt);}
    public void removeDamageText(DamageText dt){damageTexts.remove(dt);}
}
