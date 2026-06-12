package com.runnymede.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;

import java.util.*;

public class BossEnemy extends Enemy {
    // Phase and Queue Management
    private List<Integer> attackQueue;
    private int currentPhase;
    private float phaseTimer;
    private float phaseDuration = 5.0f; // Each phase lasts 6 seconds

    // Attack specific trackers
    private float shootTimer;
    private int volleyCount;
    private boolean hasSpawnedMinions;
    private boolean hasSpawnedSquare;

    // Textures needed for the boss and its summons
    private Texture bulletTexture;
    private Texture squareTexture;

    // Minion Textures
    private Texture droneTexture;
    private Texture circleTexture;
    private Texture explosionTexture;
    private Texture sentryTexture;
    private Texture laserTexture;

    public BossEnemy(float x, float y, Texture bossTexture, Texture bulletTexture, Texture squareTexture,
                     Texture droneTexture, Texture circleTexture, Texture explosionTexture,
                     Texture sentryTexture, Texture laserTexture) {
        super(x, y, 0.5f, 5.0f, 1.5f, bossTexture); // 5 max health, speed 0.5, damage 1.5

        this.bulletTexture = bulletTexture;
        this.squareTexture = squareTexture;
        this.droneTexture = droneTexture;
        this.circleTexture = circleTexture;
        this.explosionTexture = explosionTexture;
        this.sentryTexture = sentryTexture;
        this.laserTexture = laserTexture;

        // Make the boss larger
        this.sprite.setSize(0.8f, 0.8f);
        this.hitBox.setSize(0.8f, 0.8f);

        attackQueue = new ArrayList<>();
        loadNextPhase();
    }

    // Fills the queue with 1-4 shuffled, then appends 5 (Vulnerable)
    private void fillQueue() {
        List<Integer> pool = new ArrayList<>(Arrays.asList(1, 2, 3, 4));
        Collections.shuffle(pool);
        attackQueue.addAll(pool);
        attackQueue.add(5);
    }

    // Pulls the next attack from the queue
    private void loadNextPhase() {
        if (attackQueue.isEmpty()) {
            fillQueue();
        }
        currentPhase = attackQueue.remove(0);
        phaseTimer = 0f;
        shootTimer = 0f;
        volleyCount = 0;
        hasSpawnedMinions = false;
        hasSpawnedSquare = false;
    }

    // OVERRIDE: Custom Damage Logic
    @Override
    public void takeDamage(float damage) {
        // Only take damage if in Phase 5 (Vulnerable)
        if (currentPhase == 5) {
            super.takeDamage(1.0f); // Forces exactly 1 damage

            // Immediately load next phase to prevent multi-hit one-shots
            if (this.health > 0) {
                loadNextPhase();
            }
        }
    }

    @Override
    public void updateAi(float dt, float targetX, float targetY, float radius, GridManager gridManager, Pathfinder pathfinder, MainGame game) {
        Random rand = new Random();
        phaseTimer += dt;
        shootTimer += dt;

        // Automatically progress to the next phase when the timer runs out
        if (phaseTimer >= phaseDuration && currentPhase != 5) {
            loadNextPhase();
            return;
        }

        switch (currentPhase) {
            case 1: // ROTATING SQUARE
                if (!hasSpawnedSquare) {
                    // Removed the offset so it spawns directly on the boss's center
                    game.addEnemy(new TurretSquareEnemy(getCenterXPos(), getCenterYPos(), squareTexture, bulletTexture));
                    hasSpawnedSquare = true;
                }
                navigateTowardsPlayer(dt, targetX, targetY, pathfinder, gridManager);
                break;

            case 2: // MINION SPAWN
                if (!hasSpawnedMinions) {
                    // REDUCED OFFSET: Spawn Drone slightly to the left (-0.75f)
                    game.addEnemy(new DroneEnemy(getCenterXPos() - 0.5f, getCenterYPos(), 1.5f,
                        6.5f, 5.0f,
                        droneTexture, circleTexture, explosionTexture,
                        0.6f, 1.5f, 0.42f, 0.8f));

                    // REDUCED OFFSET: Spawn Sentry slightly to the right (+0.75f)
                    game.addEnemy(new SentryEnemy(getCenterXPos() + 0.5f, getCenterYPos(), 15.0f,
                        6.0f, 2.5f, 2.0f, 0.4f,
                        sentryTexture, laserTexture));

                    hasSpawnedMinions = true;
                }
                navigateTowardsPlayer(dt, targetX, targetY, pathfinder, gridManager);
                break;

            case 3: // RAPID FIRE MACHINE GUN
                navigateTowardsPlayer(dt * 0.5f, targetX, targetY, pathfinder, gridManager);
                if (shootTimer >= 0.17f) {
                    shootTimer = 0f;
                    float randomAngleOffset = (rand.nextBoolean()) ? rand.nextFloat() * 0.6f : -rand.nextFloat() * 0.6f;
                    fireBullet(targetX, targetY, game, randomAngleOffset);
                }
                break;

            case 4: // SHOTGUN VOLLEYS
                if (shootTimer >= 0.6f && volleyCount < 8) {
                    shootTimer = 0f;
                    volleyCount++;
                    // Fire 5 bullets in a spread
                    fireBullet(targetX, targetY, game, -0.6f);
                    fireBullet(targetX, targetY, game, -0.3f);
                    fireBullet(targetX, targetY, game, 0f);
                    fireBullet(targetX, targetY, game, 0.3f);
                    fireBullet(targetX, targetY, game, 0.6f);
                }
                break;

            case 5: // VULNERABLE PHASE
                // Boss STOPS moving completely.
                if (phaseTimer >= phaseDuration) {
                    loadNextPhase();
                }
                break;
        }
    }

    private void fireBullet(float targetX, float targetY, MainGame game, float angleOffset) {
        float angle = getAngleToPoint(targetX, targetY) + angleOffset;
        float actualTargetX = getCenterXPos() + MathUtils.cos(angle) * 5f;
        float actualTargetY = getCenterYPos() + MathUtils.sin(angle) * 5f;

        Projectile proj = new Projectile(false, 2.5f, 0.18f, 0f, 0f,
            8.2f, 1, 1, getCenterXPos(), getCenterYPos(), actualTargetX,
            actualTargetY, bulletTexture);
        game.addProjectile(proj);
    }
}
