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

public class MainGame implements ApplicationListener {
    /// CONSTANTS:
    private static final float CRIT_MULTIPLIER = 2.5f;

    //Player constants
    private static final float PLAYER_BASE_SPEED = 2.1f;
    private static final int PLAYER_BASE_HEALTH = 5;
    private static final float PLAYER_DASH_DURACTION = 0.18f;
    private static final float PLAYER_DASH_COOLDOWN = 1.2f;
    private static final float PLAYER_DASH_SPEED_MULTIPLIER = 3.5f;

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

    // Controls game state (FIX: Added GAME_OVER state)
    public enum GameState {PLAYING, UPGRADE_MENU, GAME_OVER}
    private GameState gameState = GameState.PLAYING;

    private SpriteBatch spriteBatch;
    private Viewport viewport;

    private ArrayList<Rectangle> walls;
    private Texture wallTexture;

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
    private ArrayList<DamageText> damageTexts;
    private ArrayList<Projectile> projectiles;
    private ArrayList<Upgrade> upgrades;

    private float difficultyScale;
    private float difficultyTimer;

    // Tracking spawn anchoring metrics for clean system reloads
    private int[][] originalLayout;
    private DungeonGenerator currentGenerator;

    public enum LoopUpgrade { LIFESTEAL, DASH, UPGRADED_DASH, SHIELD, BOUNCES }
    private LoopUpgrade[] currentChoices = new LoopUpgrade[3];

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
        explosionTexture = new Texture("explosionTexture.jpg");
        portalTexture = new Texture("portalTexture.png");

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

        font = new BitmapFont();
        font.setUseIntegerPositions(false);

        menuFont = new BitmapFont();
        menuFont.setUseIntegerPositions(false);
        menuFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        menuFont.getData().setScale(0.024f);
        menuFont.setColor(com.badlogic.gdx.graphics.Color.WHITE);

        difficultyScale = 1.0f;
        difficultyTimer = DIFF_TIMER_REFRESH;

        buildFloorLayout();
    }

    /**
     * Helper to group dungeon setup logic so it can be re-called easily upon restart.
     */
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
    }

    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();

        if (gameState == GameState.PLAYING) {
            input(deltaTime);
            logic(deltaTime);
        } else if (gameState == GameState.UPGRADE_MENU) {
            doUpgradeMenuInput();
        } else if (gameState == GameState.GAME_OVER) {
            doGameOverInput(); // FIX: Redirect inputs to clean layout reloads
        }

        draw();
    }

    public void pause() {}
    public void resume() {}

    public void dispose() {
        spriteBatch.dispose();
        font.dispose();
        menuFont.dispose();
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
        handleRoomStateLogic();
        updateAllEnemies(deltaTime, playerXPos, playerYPos);
        doUpgrade(deltaTime);
        updateCamera();
        handleDifficultyScaling(deltaTime);

        // FIX: Trap execution frame early if health drops below zero
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

        if (gameState == GameState.UPGRADE_MENU) {
            drawUpgradeMenu();
        } else if (gameState == GameState.GAME_OVER) {
            drawGameOverMenu(); // FIX: Draw Game Over Screen overlay
        }

        spriteBatch.end();
    }

    private void spawnEnemiesForRoom(Room room) {
        java.util.Random rand = new java.util.Random();
        float tileSize = gridManager.getTileSize();

        int enemyCount = rand.nextInt(3, 6);
        int spawned = 0;
        int safetyAttempts = 0;

        while (spawned < enemyCount && safetyAttempts < 50) {
            safetyAttempts++;

            int randomTileX = rand.nextInt(room.x + 1, room.x + room.width - 1);
            int randomTileY = rand.nextInt(room.y + 1, room.y + room.height - 1);

            if (gridManager.getGrid()[randomTileX][randomTileY].isWalkable) {
                float worldX = (randomTileX * tileSize) + (tileSize / 2f);
                float worldY = (randomTileY * tileSize) + (tileSize / 2f);

                if (room.type == Room.RoomType.BOSS) {
                    enemies.add(new RangedEnemy(worldX, worldY, RANGED_BASE_SPEED, 50f, 10f,
                        enemyTexture, 3.5f, 1.5f, 1.5f, 5f, 0.2f, bulletTexture));
                } else {
                    int spawnRoll = rand.nextInt(0, 100);
                    if (spawnRoll < 30) {
                        enemies.add(new SentryEnemy(worldX, worldY, SENTRY_BASE_HEALTH, SENTRY_BASE_DAMAGE, SENTRY_FIRE_TIME,
                            SENTRY_COOLDOWN_TIME, SENTRY_LASER_DURATION, sentryTexture, laserTexture));
                    } else if (spawnRoll < 80) {
                        enemies.add(new DroneEnemy(worldX, worldY, DRONE_BASE_SPEED, DRONE_BASE_HEALTH,
                            DRONE_BASE_DAMAGE, droneTexture, crosshairTexture, explosionTexture,
                            DRONE_FLEE_TIME, DRONE_TRACKING_TIME, DRONE_LOCKING_TIME, DRONE_EXPLOSION_RADIUS));
                    } else {
                        enemies.add(new RangedEnemy(worldX, worldY, RANGED_BASE_SPEED, RANGED_BASE_HEALTH * difficultyScale, RANGED_BASE_DAMAGE * difficultyScale,
                            enemyTexture, 3.5f, 1.5f, 1.5f, 5f, 0.2f, bulletTexture));
                    }
                }
                spawned++;
            }
        }
    }

    public void updateAllEnemies(float dt, float playerX, float playerY) {
        for (Enemy e : enemies) {
            e.updateAi(dt, playerX, playerY, ENEMY_BEHAVIOUR_RADIUS, gridManager, pathfinder, this);
        }
    }

    public void updateProjectiles(float deltaTime) {
        Random rand = new Random();

        // FIX: Standardized cleaner step-down to prevent clearing out concurrent arrays via index references
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
                        rollUpgradeSpawn(enemyX, enemyY);
                    }
                }
            } else {
                // ENEMY PROJECTILE VS PLAYER
                if (!player.hasUpgradedDash || !player.isDashing()) {
                    if (player.getHitBox().overlaps(currentProjectile.getHitBox())) {

                        // FIX: Only spawn floating numbers IF damage successfully punctures shield matrices
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

            // FIX: Removed the toxic 'break;' statement that was skipping other projectiles!
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

    // FIX: Process Game Over Input
    private void doGameOverInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            restartMatch();
        }
    }

    public void rollUpgradeSpawn(float spawnX, float spawnY){
        Random rand = new Random();
        if (rand.nextDouble() <= 1){
            Upgrade newUpgrade = new Upgrade(spawnX, spawnY, 5f);
            upgrades.add(newUpgrade);
        }
    }

    public void doUpgrade(float deltaTime){
        for (int i = upgrades.size() - 1; i >= 0; i--){
            Upgrade u = upgrades.get(i);
            if (u.tick(deltaTime)){
                upgrades.remove(u);
                return;
            }
            if (player.getHitBox().overlaps(u.getHitBox())){
                u.collect(player);
                upgrades.remove(u);
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
                currentRoom = room;
                return;
            }
        }
    }

    private void handleRoomStateLogic(){
        if (currentRoom == null) return;

        if (currentRoom.getState() == Room.RoomState.UNVISITED) {
            if (currentRoom.type == Room.RoomType.START) {
                currentRoom.setState(Room.RoomState.CLEARED);
            } else {
                if (player.getHitBox().overlaps(currentRoom.getTriggerBox())) {
                    currentRoom.lockDoors(gridManager);
                    spawnEnemiesForRoom(currentRoom);
                }
            }
        }

        if (currentRoom.getState() == Room.RoomState.LOCKED && enemies.isEmpty()) {
            currentRoom.unlockDoors(gridManager);
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

    // FIX: Wipes and restarts the structural game cycle cleanly
    private void restartMatch() {
        System.out.println("Reviving player and rebuilding current floor layout...");

        projectiles.clear();
        upgrades.clear();
        damageTexts.clear();
        enemies.clear();

        player.setHealth(player.getMaxHealth());
        if (player.hasShield) {
            player.shieldActive = true;
        }

        buildFloorLayout();
        gameState = GameState.PLAYING;
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

        for (int x = 0; x < gridManager.getGridColumns(); x++) {
            for (int y = 0; y < gridManager.getGridRows(); y++) {
                if (!grid[x][y].isWalkable) {
                    spriteBatch.draw(wallTexture, x * size, y * size, size, size);
                }
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

    // FIX: Renders clean layout prompt text overlaying center stage camera position metrics
    private void drawGameOverMenu() {
        menuFont.draw(spriteBatch, "GAME OVER", player.getCenterX() - 0.8f, player.getCenterY() + 0.8f);
        menuFont.draw(spriteBatch, "Press 'R' to Restart from This Floor Layout", player.getCenterX() - 2.6f, player.getCenterY() - 0.2f);
    }

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
