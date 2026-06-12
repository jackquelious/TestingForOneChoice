package com.runnymede.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;

public class TurretSquareEnemy extends Enemy {

    private static final float TURRET_SIZE = 1.0f;

    private float rotationTimer;
    private float shootTimer;
    private Texture bulletTexture;
    private float currentAngle;

    public TurretSquareEnemy(float x, float y, Texture texture, Texture bulletTexture) {
        super(x, y, 0f, 28.0f, 1.5f, texture);
        this.bulletTexture = bulletTexture;
        this.currentAngle = 0f;

        this.sprite.setSize(TURRET_SIZE, 1.5f * TURRET_SIZE);
        this.hitBox.setSize(TURRET_SIZE, 1.5f * TURRET_SIZE);

        // Sets the rotation anchor to the exact middle of the sprite
        this.sprite.setOriginCenter();
    }

    @Override
    public void updateAi(float dt, float targetX, float targetY, float radius, GridManager gridManager, Pathfinder pathfinder, MainGame game) {
        rotationTimer += dt;
        shootTimer += dt;

        // Slowly rotate the angle (in radians)
        currentAngle += dt * 1.5f;

        // Visually spin the sprite (converting radians to degrees for LibGDX)
        this.sprite.setRotation(currentAngle * MathUtils.radDeg);

        // Periodically fire 4 bullets in a cross pattern based on current rotation
        if (shootTimer >= 0.25f) {
            shootTimer = 0f;

            for (int i = 0; i < 4; i++) {
                float fireAngle = currentAngle + (i * MathUtils.PI / 2f);
                float tX = getCenterXPos() + MathUtils.cos(fireAngle) * 5f;
                float tY = getCenterYPos() + MathUtils.sin(fireAngle) * 5f;

                Projectile proj = new Projectile(false, 1.3f, 0.13f, 0f,
                    0f, 6.0f, 1, 1,
                    getCenterXPos(), getCenterYPos(), tX, tY, bulletTexture);
                game.addProjectile(proj);
            }
        }
    }
}
