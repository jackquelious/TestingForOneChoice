package com.runnymede.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class DroneEnemy extends Enemy {

    public enum BomberState {
        FLEEING,
        TRACKING,
        LOCKED
    }

    private BomberState state;

    // Dynamic parameters configured through the constructor
    private float maxFleeTime;
    private float maxTrackTime;
    private float maxLockTime;
    private float blastRadius;

    // State and visual timers
    private float fleeTimer;
    private float trackTimer;
    private float lockTimer;
    private float explosionDisplayTimer;

    // Targeting coordinates
    private float crosshairX; // Stores the position the cross hair should be
    private float crosshairY;

    private float lastPlayerX;
    private float lastPlayerY;

    // coordinates to pin the explosion texture to the ground
    private float explosionX;
    private float explosionY;

    // Configurable sprites
    private Sprite warningIndicatorSprite;
    private Sprite explosionSprite;

    // Updated constructor accepting configuration settings and textures
    public DroneEnemy(float xPos, float yPos, float speed, float health, float damage,
                       Texture mainTexture, Texture circleTexture, Texture explosionTexture,
                       float maxFleeTime, float maxTrackTime, float maxLockTime, float blastRadius) {

        super(xPos, yPos, speed, health, damage, mainTexture);

        // Assigning the custom parameters
        this.maxFleeTime = maxFleeTime;
        this.maxTrackTime = maxTrackTime;
        this.maxLockTime = maxLockTime;
        this.blastRadius = blastRadius;

        this.state = BomberState.FLEEING;
        this.fleeTimer = maxFleeTime;
        this.explosionDisplayTimer = 0f;

        // Calculate the diameter to scale the textures to the exact blast range
        float indicatorSize = blastRadius * 2f;

        this.warningIndicatorSprite = new Sprite(circleTexture);
        this.warningIndicatorSprite.setSize(blastRadius,  blastRadius);

        this.explosionSprite = new Sprite(explosionTexture);
        this.explosionSprite.setSize(indicatorSize * 1.05f, indicatorSize * 1.05f);

        lastPlayerX = 0;
        lastPlayerY = 0;
    }

    @Override
    public void updateAi(float dt, float targetX, float targetY, float radius, GridManager gridManager, Pathfinder pathfinder, MainGame mainGame) {
        if (!isAlive()) return;

        float playerX = targetX;
        float playerY = targetY;

        if (explosionDisplayTimer > 0) {
            explosionDisplayTimer -= dt;
        }

        float indicatorHalfWidth = warningIndicatorSprite.getWidth() / 2f;
        float explosionHalfWidth = explosionSprite.getWidth() / 2f;

        switch (state) {
            case FLEEING:
                fleeTimer -= dt;

                float angleToPlayer = getAngleToPoint(playerX, playerY);
                float fleeAngle = (float) (angleToPlayer + Math.PI);

                float fleePointX = getCenterXPos() + (float) (Math.cos(fleeAngle) * 5f);
                float fleePointY = getCenterYPos() + (float) (Math.sin(fleeAngle) * 5f);

                moveTowardsPoint(dt, fleePointX, fleePointY, gridManager);

                if (fleeTimer <= 0) {
                    state = BomberState.TRACKING;
                    trackTimer = maxTrackTime;
                }
                break;

            case TRACKING:
                trackTimer -= dt;

                crosshairX = playerX;
                crosshairY = playerY;

                warningIndicatorSprite.setPosition(crosshairX - indicatorHalfWidth, crosshairY - indicatorHalfWidth);

                if (trackTimer <= 0) {
                    state = BomberState.LOCKED;
                    lockTimer = maxLockTime;

                    // YOUR FIX: Calculate the predicted offset ONCE and apply it immediately.
                    // We find the player's velocity, and multiply it by a prediction window (e.g., predicting 0.3 seconds ahead).
                    float predictedOffsetX = ((playerX - lastPlayerX) / dt) * 0.2f;
                    float predictedOffsetY = ((playerY - lastPlayerY) / dt) * 0.2f;

                    // Snap the crosshair forward to the predicted spot
                    crosshairX += predictedOffsetX;
                    crosshairY += predictedOffsetY;
                }
                break;

            case LOCKED:
                lockTimer -= dt;

                // The crosshair coordinates are now COMPLETELY untouched here!
                // We just keep updating the sprite to sit exactly where we snapped it.
                warningIndicatorSprite.setPosition(crosshairX - indicatorHalfWidth, crosshairY - indicatorHalfWidth);

                if (lockTimer <= 0) {
                    explosionX = crosshairX;
                    explosionY = crosshairY;

                    explosionSprite.setPosition(explosionX - explosionHalfWidth, explosionY - explosionHalfWidth);

                    executeExplosion(playerX, playerY, mainGame);

                    explosionDisplayTimer = 0.3f;

                    state = BomberState.FLEEING;
                    fleeTimer = maxFleeTime;
                }
                break;
        }

        // Cache the player's position at the very end of the loop for the next frame's math
        lastPlayerX = playerX;
        lastPlayerY = playerY;
    }

    private void executeExplosion(float playerX, float playerY, MainGame mainGame) {
        float distance = getDistanceToPoint(crosshairX, crosshairY, playerX, playerY);

        if (distance <= blastRadius) {
            // mainGame.getPlayer().takeDamage(this.damage);
            System.out.println("BOOM Player hit");
        } else {
            System.out.println("BOOM Player dodged");
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        // 1. Draw the warning ring with the correct phase color filters
        if (state == BomberState.TRACKING) {
            warningIndicatorSprite.setColor(Color.CYAN); // Fixed from Blue to Cyan to prevent black textures
            warningIndicatorSprite.draw(batch);
        } else if (state == BomberState.LOCKED) {
            warningIndicatorSprite.setColor(Color.RED);
            warningIndicatorSprite.draw(batch);
        }

        // 2. Draw the blast texture if the explosion visual timer is currently running
        if (explosionDisplayTimer > 0) {
            explosionSprite.draw(batch);
        }

        // 3. Reset the color tint back to normal and draw the actual drone body
        warningIndicatorSprite.setColor(Color.WHITE);
        super.draw(batch);
    }
}
