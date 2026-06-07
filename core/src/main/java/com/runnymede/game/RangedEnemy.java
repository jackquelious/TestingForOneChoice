package com.runnymede.game;

import com.badlogic.gdx.graphics.Texture;
import org.apache.tools.ant.Main;

public class RangedEnemy extends Enemy{
    // Tracks the enmies game state
    private enum State {Approaching, Attacking, Retreating}

    // CLASS VARIABLES:
    private State state;
    private float shootRange;

    private float bulletSpeed;
    private float bulletSize;

    private float retreatTime;
    private float attackTime;

    private float maxRetreatTime;
    private float maxAttackTime;

    // Constructor
    public RangedEnemy(float xPos, float yPos, float speed, float health, float damage, Texture texture, float shootRange,
                       float attackTime, float retreatTime, float bulletSpeed, float bulletSize, Texture bulletTexture) {
        // Calls the inherited enemy constructor
        super(xPos, yPos, speed, health, damage, texture);

        // Initialises  other class variables
        this.shootRange = shootRange;
        this.maxAttackTime = attackTime;
        this.bulletSpeed = bulletSpeed;
        this.bulletSize = bulletSize;
        this.maxRetreatTime = retreatTime;
        this.state = State.Approaching;

        this.retreatTime = retreatTime;
        this.attackTime = attackTime;
    }
    // Important method that handles enemy ai
    public void updateAi(float dt, float targetX, float targetY , float radius, GridManager gridManager, Pathfinder pathfinder, MainGame mainGame){
        // Gets the position of the enemy
        float centerX = getXPos();
        float centerY = getYPos();

        // Makes a switch that does different things depending on state
        switch (state){
            // If the state is approaching, approach player until within firing range
            case Approaching:
                // Uses basic movement from to move toward player
                if(getDistanceToPoint(centerX, centerY, targetX, targetY) >= shootRange){
                    // if there are no walls nearby use direct tracking logic
                    if (!areWallsNearby(centerX, centerY, radius, gridManager)) {
                        moveTowardsPoint(dt, targetX, targetY); // Direct tracking logic
                        if (getCurrentPath() != null) getCurrentPath().clear(); // clears the current path

                    } else {
                        // Otherwise use the navigation method
                        navigateTowardsPlayer(dt, targetX, targetY, pathfinder);
                    }
                } else {
                    // Switch to attacking phase
                    state = State.Attacking;
                }
                break;

            case Attacking:
                // If attack timer has ticked
                if(attackTime <= 0){
                    // Create new projectile
                    Projectile newProjectile = new Projectile(false, bulletSpeed, bulletSize, 0,
                        0, damage, 1, 1, centerX, centerY, targetX, targetY, texture);

                    // Add the projectile to the list
                    mainGame.addProjectile(newProjectile);
                    state = State.Retreating; // change state
                    attackTime = maxAttackTime;
                } else{attackTime -= dt;} // lower attack timer
                break;

            case Retreating:
                if(retreatTime <= 0){
                    retreatTime = maxRetreatTime;
                    state = State.Approaching;
                } else {
                    retreatTime -= dt;

                    // Calculate the push vector away from the player
                    float deltaX = centerX - targetX;
                    float deltaY = centerY - targetY;

                    float retreatX = centerX + deltaX;
                    float retreatY = centerY + deltaY;

                    // Retreats away from playerww
                    if (!areWallsNearby(centerX, centerY, radius, gridManager)) {
                        moveTowardsPoint(dt, retreatX, retreatY);
                        if (getCurrentPath() != null) getCurrentPath().clear();
                    } else {navigateTowardsPlayer(dt, retreatX, retreatY, pathfinder);}
                }
                break;
                }

        }
    }

