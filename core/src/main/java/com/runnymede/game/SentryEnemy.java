package com.runnymede.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class SentryEnemy extends Enemy {

    public enum SentryState { TRACKING, FIRING, COOLDOWN }

    private SentryState state;

    // Timers
    private float fireTimer;
    private float cooldownTimer;
    private float laserVisibleTimer;

    // Timers
    private float maxFireTime;      // How long it takes to lock on
    private float maxCooldownTime;  // Time between shots
    private float laserDuration;     // How long the beam stays on screen

    // Target tracking for rendering the laser
    private float targetX;
    private float targetY;
    private Texture laserTexture;

    public SentryEnemy(float x, float y, float health, float damage, float maxFireTime, float maxCoolDown, float laserDuration, Texture texture, Texture laserTexture) {
        // We pass 0.0f for speed so your physical movement code completely ignores it!
        super(x, y, 0.0f, health, damage, texture);

        this.maxFireTime = maxFireTime;
        this.maxCooldownTime = maxCoolDown;
        this.laserDuration = laserDuration;

        this.laserTexture = laserTexture;
        this.state = SentryState.TRACKING;
        this.fireTimer = maxFireTime;
        this.cooldownTimer = maxCoolDown;
        this.laserVisibleTimer = 0.0f;

    }

    @Override
    public void updateAi(float dt, float playerX, float playerY, float radius, GridManager gridManager, Pathfinder pathfinder, MainGame game) {
        // Always keep hitbox securely wrapped around the stationary model
        this.setHitBoxPos(getXPos(), getYPos());

        switch (state) {
            case TRACKING:
                fireTimer -= dt;

                // Trigger the Hitscan!
                if (fireTimer <= 0) {
                    state = SentryState.FIRING;
                    laserVisibleTimer = laserDuration;

                    // Lock coordinates for visual drawing
                    this.targetX = playerX;
                    this.targetY = playerY;

                    if(!hasLineOfSight(playerX, playerY, gridManager)) {
                        // Apply instant, unavoidable damage and spawn visual text
                        game.getPlayer().takeDamage(this.damage);
                        game.addDamageText(new DamageText(this.damage, false, false, playerX, playerY));
                    }
                }
                break;

            case FIRING:
                laserVisibleTimer -= dt;
                if (laserVisibleTimer <= 0) {
                    state = SentryState.COOLDOWN;
                    cooldownTimer = maxCooldownTime;
                }
                break;

            case COOLDOWN:
                cooldownTimer -= dt;
                if (cooldownTimer <= 0) {
                    state = SentryState.TRACKING;
                    fireTimer = maxFireTime;
                }
                break;
        }
    }

    /**
     * Raycasts a line from the Sentry to the Player.
     * Returns false the exact moment the line touches a solid grid wall.
     */
    private boolean hasLineOfSight(float targetX, float targetY, GridManager gridManager) {
        float startX = this.getCenterXPos();
        float startY = this.getCenterYPos();

        float dx = targetX - startX;
        float dy = targetY - startY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        // Normalize direction
        float dirX = dx / distance;
        float dirY = dy / distance;

        float tileSize = gridManager.getTileSize();
        // Step forward in tiny increments (a quarter of a tile) to ensure we never jump over a wall
        float stepSize = tileSize / 4f;

        for (float currentDist = 0; currentDist < distance; currentDist += stepSize) {
            float checkX = startX + (dirX * currentDist);
            float checkY = startY + (dirY * currentDist);

            int tileX = (int) (checkX / tileSize);
            int tileY = (int) (checkY / tileSize);

            // Ensure we stay inside array bounds
            if (tileX >= 0 && tileX < gridManager.getGridColumns() && tileY >= 0 && tileY < gridManager.getGridRows()) {
                if (!gridManager.getGrid()[tileX][tileY].isWalkable) {
                    return false; // Line hit a solid wall!
                }
            }
        }
        return true; // Line reached the player unobstructed!
    }

    @Override
    public void draw(SpriteBatch batch) {
        // Draw the Sentry base body
        super.draw(batch);

        // Render the hitscan laser if firing
        if (state == SentryState.FIRING || laserVisibleTimer > 0) {
            float startX = this.getCenterXPos();
            float startY = this.getCenterYPos();

            float dx = targetX - startX;
            float dy = targetY - startY;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            // Calculate angle for the LibGDX draw rotation
            float angle = (float) Math.toDegrees(Math.atan2(dy, dx));

            // Stretches your bullet/laser texture to perfectly bridge the gap between Sentry and Player
            batch.draw(laserTexture, startX, startY - 0.1f, 0, 0.1f, distance, 0.2f, 1f, 1f, angle, 0, 0, laserTexture.getWidth(), laserTexture.getHeight(), false, false);
        }
    }
}
