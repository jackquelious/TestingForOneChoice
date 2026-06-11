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

    // Attributes
    private float maxFireTime;      // How long it takes to lock on
    private float maxCooldownTime;  // Time between shots
    private float laserDuration;    // How long the beam stays on screen

    // Target tracking for rendering the laser
    private float targetX;
    private float targetY;
    private Texture laserTexture;

    public SentryEnemy(float x, float y, float health, float damage, float maxFireTime, float maxCoolDown, float laserDuration, Texture texture, Texture laserTexture) {
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
        this.setHitBoxPos(getXPos(), getYPos());

        switch (state) {
            case TRACKING:
                fireTimer -= dt;

                if (fireTimer <= 0) {
                    state = SentryState.FIRING;
                    laserVisibleTimer = laserDuration;

                    // CHANGED: We now let calculateLaserPath determine targetX, targetY, AND if we hit the player!
                    // REMOVED the "!" that was reversing your logic
                    if (calculateLaserPath(playerX, playerY, gridManager)) {
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
     * Updates targetX and targetY to either the Player OR the obstructing Wall.
     * @return true if the laser hit the player, false if it hit a wall.
     */
    private boolean calculateLaserPath(float playerX, float playerY, GridManager gridManager) {
        float startX = this.getCenterXPos();
        float startY = this.getCenterYPos();

        float dx = playerX - startX;
        float dy = playerY - startY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        float dirX = dx / distance;
        float dirY = dy / distance;

        float tileSize = gridManager.getTileSize();
        float stepSize = tileSize / 4f;

        for (float currentDist = 0; currentDist < distance; currentDist += stepSize) {
            float checkX = startX + (dirX * currentDist);
            float checkY = startY + (dirY * currentDist);

            int tileX = (int) (checkX / tileSize);
            int tileY = (int) (checkY / tileSize);

            if (tileX >= 0 && tileX < gridManager.getGridColumns() && tileY >= 0 && tileY < gridManager.getGridRows()) {
                if (!gridManager.getGrid()[tileX][tileY].isWalkable) {
                    // HIT A WALL!
                    // Truncate the visual laser coordinates exactly at the wall surface
                    this.targetX = checkX;
                    this.targetY = checkY;
                    return false;
                }
            }
        }

        // HIT THE PLAYER!
        this.targetX = playerX;
        this.targetY = playerY;
        return true;
    }

    @Override
    public void draw(SpriteBatch batch) {
        super.draw(batch);

        if (state == SentryState.FIRING || laserVisibleTimer > 0) {
            float startX = this.getCenterXPos();
            float startY = this.getCenterYPos();

            float dx = targetX - startX;
            float dy = targetY - startY;

            // Your draw loop will automatically shrink the texture based on the distance to the wall!
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            float angle = (float) Math.toDegrees(Math.atan2(dy, dx));

            batch.draw(laserTexture, startX, startY - 0.1f, 0, 0.1f, distance, 0.2f, 1f, 1f, angle, 0, 0, laserTexture.getWidth(), laserTexture.getHeight(), false, false);
        }
    }
}
