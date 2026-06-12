package com.runnymede.game;

import java.util.ArrayList;
import java.util.Random;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
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
    private static final float CRIT_MULTIPLIER = 3.2f;

    //Player constants
    private static final float PLAYER_BASE_SPEED = 2.4f;
    private static final int PLAYER_BASE_HEALTH = 30;
    private static final float PLAYER_DASH_DURACTION = 0.2f;
    private static final float PLAYER_DASH_COOLDOWN = 1.5f;
    private static final float PLAYER_DASH_SPEED_MULTIPLIER = 3.8f;

    private static final float PERIODIC_DIFF_INCREMENT = 0.02f;
    private static final float LEVEL_UP_DIFF_MULT = 1.35f;
    private static final float DIFF_TIMER_REFRESH = 1.5f;

    // Enemy constants
    private static final float ENEMY_TIMER_REFRESH = 0.8f;
    private static final float ENEMY_BEHAVIOUR_RADIUS = 0.4f;

    // Melee constants
    private static final float MELEE_BASE_SPEED = 1.6f;
    private static final float MELEE_BASE_DAMAGE = 1.0f;
    private static final float MELEE_BASE_HEALTH = 3.0f;

    // RANGED CONSTANTS
    private static final float RANGED_BASE_SPEED = 0.8f;
    private static final float RANGED_BASE_DAMAGE = 1.2f;
    private static final float RANGED_BASE_HEALTH = 3.0f;
    private static final float RANGED_BASE_RANGE = 2.5f;

    private static final float RANGED_BASE_BULLET_SPEED = 3.8f;
    private static final float RANGED_BASE_BULLET_SIZE = 0.2f;

    private static final float RANGED_BASE_RETREAT_TIME = 1.5f;
    private static final float RANGED_BASE_ATTACK_TIME = 1.2f;

    // SENTRY CONSTANTS
    private static final float SENTRY_BASE_DAMAGE = 2.6f;
    private static final float SENTRY_BASE_HEALTH = 5.0f;
    private static final float SENTRY_COOLDOWN_TIME = 1.2f;
    private static final float SENTRY_FIRE_TIME = 3.5f;
    private static final float SENTRY_LASER_DURATION = 0.3f;

    // Drone Constants
    private static final float DRONE_BASE_DAMAGE = 2f;
    private static final float DRONE_BASE_HEALTH = 2.0f;
    private static final float DRONE_BASE_SPEED = 1.2f;
    private static final float DRONE_FLEE_TIME = 1.2f;
    private static final float DRONE_TRACKING_TIME = 2.8f;
    private static final float DRONE_LOCKING_TIME = 0.8f;
    private static final float DRONE_EXPLOSION_TIME = 0.8f;
    private static final float DRONE_EXPLOSION_RADIUS = 0.8f;

    // TextConstants
    float regFontScale = 0.024f;
    float upgradeFontScale = 0.028f;



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
    private BitmapFont upgradeFont;

    private float runTime; // Tracks total time on current run
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
    private ArrayList<UpgradeText> upgradeTexts;


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

    // Tracks how many floors you cleared
    private int numberOfFloorsCleared;

    public void create() {
        spriteBatch = new SpriteBatch();
        enemies = new ArrayList<Enemy>();
        runTime = 0.0f;
        player = new Player(10.0f, 10.0f, PLAYER_BASE_SPEED, PLAYER_BASE_HEALTH, PLAYER_DASH_DURACTION,
            PLAYER_DASH_COOLDOWN, PLAYER_DASH_SPEED_MULTIPLIER);
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
        upgradeTexts = new ArrayList<UpgradeText>();

        font = new BitmapFont();
        font.setUseIntegerPositions(false);

        menuFont = new BitmapFont();
        menuFont.setUseIntegerPositions(false);
        menuFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        menuFont.getData().setScale(regFontScale);
        menuFont.setColor(com.badlogic.gdx.graphics.Color.WHITE);

        upgradeFont = new BitmapFont();
        upgradeFont.setUseIntegerPositions(false);

        upgradeFont = new BitmapFont();
        upgradeFont.setUseIntegerPositions(false);
        upgradeFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        upgradeFont.getData().setScale(upgradeFontScale);
        upgradeFont.setColor(com.badlogic.gdx.graphics.Color.WHITE);

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

        numberOfFloorsCleared = 0;

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

        // Only increase the timer if the game is actually being played
        if (gameState == GameState.PLAYING) {
            runTime += deltaTime;
        }

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
        upgradeFont.dispose();

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
        updateUpgradeTexts(deltaTime);
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
        drawUpgradeTexts();


        if (gameState == GameState.UPGRADE_MENU) {
            drawUpgradeMenu();
        } else if (gameState == GameState.GAME_OVER) {
            drawGameOverMenu();
        }

        spriteBatch.end();

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

                // If number is 0 - 14 (more common with scale) only available on floor 3+
                if (spawnRoll < (15 + (4.0f * difficultyScale)) && numberOfFloorsCleared > 1) {
                    float health = SENTRY_BASE_HEALTH + 5.0f * (difficultyScale - 1);
                    float dmg = Math.min(SENTRY_BASE_DAMAGE + 4.0f * (difficultyScale - 1), 12.0f);
                    float cd = Math.max(SENTRY_COOLDOWN_TIME - (difficultyScale - 1), 0.1f);
                    float fireTime = Math.max(2.6f, SENTRY_FIRE_TIME - 0.6f * (difficultyScale - 1));

                    enemiesToSpawn.add(new SentryEnemy(worldX, worldY, health, dmg, fireTime,
                        cd, SENTRY_LASER_DURATION, sentryTexture, laserTexture));

                // If number is 15 - 34 spawn a drone (more common with scale) Only available on level 2+
                } else if (spawnRoll < (35 + (4.0f * difficultyScale)) && numberOfFloorsCleared > 0) {
                    // Calculates attack Times:
                    float fleeTime = Math.max(DRONE_FLEE_TIME - 0.6f * (difficultyScale - 1), 0.8f);
                    float trackTime = Math.max(DRONE_TRACKING_TIME - 0.6f * (difficultyScale - 1), 0.8f);
                    float lockTime = Math.max(DRONE_LOCKING_TIME - 0.14f * (difficultyScale - 1), 0.5f);

                    // Calculates attack values
                    float boomRad = Math.min(DRONE_EXPLOSION_RADIUS + 0.1f * (difficultyScale-1), 0.95f);
                    float dmg = Math.min(DRONE_BASE_DAMAGE + 4.0f * (difficultyScale - 1), 10.0f);
                    float health = (DRONE_BASE_HEALTH * difficultyScale);

                    enemiesToSpawn.add(new DroneEnemy(worldX, worldY, DRONE_BASE_SPEED, health,
                        dmg, droneTexture, crosshairTexture, explosionTexture,
                        fleeTime, trackTime, lockTime, boomRad));

                // If number is 50 - 99 spawn regular ranged enemy
                } else {
                    float health = RANGED_BASE_HEALTH * difficultyScale;
                    float dmg = Math.min(RANGED_BASE_DAMAGE + 4.0f * (difficultyScale - 1), 8.0f);
                    float bulletSpeed = Math.min(RANGED_BASE_BULLET_SPEED + 1.2f * (difficultyScale - 1), 5.5f);
                    float attackTime = Math.max(RANGED_BASE_ATTACK_TIME - (difficultyScale - 1), 0.5f);
                    enemiesToSpawn.add(new RangedEnemy(worldX, worldY, RANGED_BASE_SPEED, health, dmg,
                        enemyTexture, RANGED_BASE_RANGE, attackTime, RANGED_BASE_RETREAT_TIME, bulletSpeed,
                        RANGED_BASE_BULLET_SIZE, bulletTexture));
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

                    // If they hit eachother deal damage and add them to the list of hit enemies
                    if (projectileHitbox.overlaps(curEnemyHitBox)) {

                        // Checks if the current projectile has already hit that enemy
                        if (!currentProjectile.hitEnemy(currentEnemy)) {

                            // Records the hit so they won't be damaged again
                            currentProjectile.recordEnemy(currentEnemy);

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

    public void updateUpgradeTexts(float deltaTime) {
        for(int i = upgradeTexts.size() - 1; i >= 0; i--){
            UpgradeText ut = upgradeTexts.get(i);
            ut.update(deltaTime, this);
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

    // Method that does upgradeLogic
    public void doUpgrade(float deltaTime){
        for (int i = upgrades.size() - 1; i >= 0; i--){
            Upgrade u = upgrades.get(i);
            if (u.tick(deltaTime)){
                upgrades.remove(i);
                continue;
            }
            if (player.getHitBox().overlaps(u.getHitBox())){
                String uType = u.getType();
                u.collect(player);
                upgrades.remove(i);


                System.out.println("type is " + uType);
                float spawnX = player.getX() - 0.5f;
                float spawnY = player.getY() + 0.8f;

                // Declare the variable ONCE out here so Java doesn't throw a scope error
                UpgradeText newText = null;

                switch(uType){
                    case "damage":
                        newText = new UpgradeText(spawnX, spawnY, "Increased Damage",
                            com.badlogic.gdx.graphics.Color.RED);
                        break;

                    case "health":
                        newText = new UpgradeText(spawnX, spawnY, "Increased Max Health",
                            com.badlogic.gdx.graphics.Color.GREEN);
                        break;

                    case "heal":
                        newText = new UpgradeText(spawnX, spawnY, "Healed HP",
                            com.badlogic.gdx.graphics.Color.PINK);
                        break;

                    case "bulletSize":
                        newText = new UpgradeText(spawnX, spawnY, "Increased Bullet Size",
                            com.badlogic.gdx.graphics.Color.BLUE);
                        break;

                    case "bulletSpeed":
                        newText = new UpgradeText(spawnX, spawnY, "Increased Bullet Speed",
                            com.badlogic.gdx.graphics.Color.CYAN);
                        break;

                    case "critChance":
                        newText = new UpgradeText(spawnX, spawnY, "Increased Crit Chance",
                            com.badlogic.gdx.graphics.Color.YELLOW);
                        break;

                    case "speed":
                        newText = new UpgradeText(spawnX, spawnY, "Increased Movement Speed",
                            com.badlogic.gdx.graphics.Color.ORANGE);
                        break;

                    case "pierce":
                        newText = new UpgradeText(spawnX, spawnY, "Increased Pierce",
                            com.badlogic.gdx.graphics.Color.MAGENTA);
                        break;

                    default:
                        break;
                }

                // Adds the text to your active list so it gets rendered to the screen
                if (newText != null) {
                    // Assuming UpgradeText extends DamageText, add it right into your existing manager
                    upgradeTexts.add(newText);
                }
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

    // After using a portal advance to the next level
    private void advanceToNextFloor() {
        progressLevel(); // updates diff scale

        // Clears all lists
        projectiles.clear();
        upgrades.clear();
        damageTexts.clear();
        upgradeTexts.clear();
        enemies.clear();

        buildFloorLayout(); // rebuilds a layout
        gameState = GameState.PLAYING; // resets game state
        numberOfFloorsCleared ++;
    }

    // Resets the game after dying
    private void restartMatch() {

        // Clears all lists so they can be redone
        projectiles.clear();
        upgrades.clear();
        damageTexts.clear();
        upgradeTexts.clear();
        enemies.clear();


        // Resets player stats and difficulty scale
        player.resetStats(PLAYER_BASE_SPEED, PLAYER_BASE_HEALTH);
        difficultyScale = 1.0f;
        runTime = 0;

        //Builds another dungeon, and resets the gameState
        buildFloorLayout();
        gameState = GameState.PLAYING;
        switchTrack(mainThemeSong); // Switches the track back to nomral one
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

    public void drawUpgradeTexts(){
        for(UpgradeText ut : upgradeTexts){
            ut.draw(spriteBatch, upgradeFont);
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
        // ==========================================
        // 1. HEALTH BAR RENDERING (Stays the same)
        // ==========================================
        float hudX = 10;
        float hudY = 8;
        float frameWidth = 160;
        float frameHeight = 90;

        float healthPercent = (float) player.getHealth() / player.getMaxHealth();
        healthPercent = Math.max(0, healthPercent);

        float greenOffsetX = 32;
        float greenOffsetY = 30;
        float maxGreenWidth = 119;
        float greenHeight = 30;

        float currentGreenWidth = maxGreenWidth * healthPercent;

        spriteBatch.setColor(com.badlogic.gdx.graphics.Color.GREEN);
        spriteBatch.draw(solidColorTexture, hudX + greenOffsetX, hudY + greenOffsetY, currentGreenWidth, greenHeight);

        spriteBatch.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        spriteBatch.draw(healthBarFrameTexture, hudX, hudY, frameWidth, frameHeight);


        // ==========================================
        // 2. DIFFICULTY TRACKER RENDERING
        // ==========================================

        // Calculate the floor and ceiling of the current tier so the bar fills up cleanly from 0%
        float minThreshold = 1.0f;
        if (difficultyScale >= 6.5f)       minThreshold = 6.5f;
        else if (difficultyScale >= 4.5f)  minThreshold = 4.5f;
        else if (difficultyScale >= 3.0f)  minThreshold = 3.0f;
        else if (difficultyScale >= 2.0f)  minThreshold = 2.0f;
        else if (difficultyScale >= 1.5f)  minThreshold = 1.5f;

        float currentThreshold = getNextDifficultyThreshold(difficultyScale);
        float fillPercentage = 1.0f;

        if (currentThreshold > minThreshold) {
            fillPercentage = (difficultyScale - minThreshold) / (currentThreshold - minThreshold);
        }

        // Positions for the top right corner of the 800x480 UI frame
        float barWidth = 200f;
        float barHeight = 22f;
        float barX = 800f - barWidth - 20f; // 20 pixels padding from the right edge

        // 🛠️ CHANGED: Increased padding from 20f to 50f to safely pull everything downward
        float barY = 480f - barHeight - 50f;

        // Draw the dark background tray
        spriteBatch.setColor(Color.DARK_GRAY);
        spriteBatch.draw(solidColorTexture, barX, barY, barWidth, barHeight);

        // Draw the Risk-style Firebrick fill bar
        spriteBatch.setColor(Color.FIREBRICK);
        spriteBatch.draw(solidColorTexture, barX, barY, barWidth * fillPercentage, barHeight);

        // Construct a crisp pixel outline border
        spriteBatch.setColor(Color.WHITE);
        float border = 2f; // Thickness of the outline frame
        spriteBatch.draw(solidColorTexture, barX - border, barY + barHeight, barWidth + (border * 2), border); // Top
        spriteBatch.draw(solidColorTexture, barX - border, barY - border, barWidth + (border * 2), border); // Bottom
        spriteBatch.draw(solidColorTexture, barX - border, barY, border, barHeight);                        // Left
        spriteBatch.draw(solidColorTexture, barX + barWidth, barY, border, barHeight);                      // Right

        // Standard font
        font.setColor(Color.WHITE);
        font.getData().setScale(1.0f); // Ensure it's crisp and at full pixel scale
        String diffText = "RISK: " + getDifficultyName(difficultyScale);
        font.draw(spriteBatch, diffText, barX + 10, barY + 17);

        // ==========================================
        // 3. SPEEDRUN TIMER RENDERING
        // ==========================================
        String timeText = getFormattedTime(runTime);

        // Scale the font up to make the clock prominent
        font.getData().setScale(1.6f);
        font.setColor(Color.WHITE);

        // Draw it aligned with the left edge of the difficulty bar, sitting just above it
        font.draw(spriteBatch, timeText, barX, barY + barHeight + 28);

        // Reset the font scale back to normal
        font.getData().setScale(1.0f);
    }

    // Safely queues a track change if it isn't already playing
    private void switchTrack(Music newTrack) {
        if (currentTrack == newTrack || incomingTrack == newTrack) return; // doesn't switch if it's already queing
        incomingTrack = newTrack;
        isFading = true;
    }

    // Handles the cross-fade logic every frame
    private void updateMusic(float deltaTime) {
        if (isFading && incomingTrack != null) {
            float currentVol = currentTrack.getVolume();
            float newVol = currentVol - (fadeSpeed * deltaTime);

            if (newVol <= 0) {
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
            if (newVol > 1.0f) newVol = 1.0f; // Cap volume at 100%
            currentTrack.setVolume(newVol);
        }
    }

    // FOR THE DIFF BAR:
    // Returns the name of the difficulty we are in based on the difficulty scaling
    private String getDifficultyName(float diff) {
        if (diff < 1.5f) return "EASY";
        if (diff < 2.2f) return "NORMAL";
        if (diff < 3.4f) return "HARD";
        if (diff < 4.5f) return "INSANE";
        if (diff < 6.5f) return "NIGHTMARE";
        return "HAHAHA"; // The classic RoR2 final tier!
    }

    private float getNextDifficultyThreshold(float diff) {
        if (diff < 1.5f) return 1.5f;
        if (diff < 2.0f) return 2.0f;
        if (diff < 3.0f) return 3.0f;
        if (diff < 4.5f) return 4.5f;
        if (diff < 6.5f) return 6.5f;
        return diff; // If maxed out, the bar just stays full
    }

    // Gets the string for the time in seconds
    private String getFormattedTime(float totalSeconds) {
        int minutes = (int) (totalSeconds / 60);
        int seconds = (int) (totalSeconds % 60);

        // Pads single digits with a leading zero
        String minStr = (minutes < 10) ? "0" + minutes : String.valueOf(minutes);
        String secStr = (seconds < 10) ? "0" + seconds : String.valueOf(seconds);

        return minStr + ":" + secStr;
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
    public void addUpgradeText(UpgradeText ut){upgradeTexts.add(ut);}
    public void removeUpgradeText(UpgradeText ut){upgradeTexts.remove(ut);}
}
