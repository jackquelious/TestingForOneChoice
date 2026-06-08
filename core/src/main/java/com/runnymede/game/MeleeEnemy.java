package com.runnymede.game;

import com.badlogic.gdx.graphics.Texture;

public class MeleeEnemy extends Enemy{
    public MeleeEnemy(float xPos, float yPos, float speed, float health, float damage, Texture texture){
        super(xPos, yPos, speed, health, damage, texture);
    }

    public void updateAi(float dt, float targetX, float targetY ,float radius, GridManager gridManager, Pathfinder pathfinder, MainGame mainGame){
        // gets the cordinates
        float enemyCenterX = getCenterXPos();
        float enemyCenterY = getCenterYPos();

        // Checks if there are nearby obstacles
        // If there are the enemy will use node based tracking otherwise it just uses direct movement
        if (!areWallsNearby(enemyCenterX, enemyCenterY, radius, gridManager)) {
            // Direct tracking logic
            moveTowardsPoint(dt, targetX, targetY, gridManager);
            // Clears the current node path
            if (getCurrentPath() != null) getCurrentPath().clear();
        } else {
        // Otherwise use the navigation method
        navigateTowardsPlayer(dt, targetX, targetY, pathfinder, gridManager);
        }
    }
}
