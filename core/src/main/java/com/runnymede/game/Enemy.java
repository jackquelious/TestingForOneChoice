package com.runnymede.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

import java.util.List;

public class Enemy {
    // CLASS VARIABLES
    private float speed;
    private float health;
    private float damage;
    private Texture texture;
    private Sprite sprite;
    private boolean alive;
    private Rectangle hitBox;

    // For pathfinding
    private List<Node> currentPath; // Tracks the calculated path of nodes that lead to the target

    // PATHFINDING CONSTANTS
    private float PATH_TIMER = 0f; // A timer that triggers a re-calculation of the path
    private final float PATH_REFRESH_RATE = 0.25f; // Recalculates route 4 times a second
    private final float TILE_SIZE = 0.5f; // the size of tiles

    // Constructor
    // Excepts start point, speed, health, and damage arguments
    public Enemy(float xPos, float yPos, float speed, float health, float damage) {
        // Initializes and instantiates all variables
        this.speed = speed;
        this.health = health;
        this.damage = damage;

        this.texture = new Texture("enemySquare.png");
        this.sprite = new Sprite(texture);

        // Sets up the bullet sprite
        this.sprite.setSize(0.4f, 0.4f);
        this.sprite.setPosition(xPos, yPos);

        this.hitBox = new Rectangle(xPos, yPos, sprite.getWidth(), sprite.getHeight()); // Creates the hitbox
        this.alive = true; // Starts alive
    }

    // Getters
    public float getXPos() { return sprite.getX(); }
    public float getYPos() { return sprite.getY(); }
    public float getSpeed() { return speed; }
    public float getHealth() { return health; }
    public float getDamage() { return damage; }
    public Texture getTexture() { return texture; }
    public Sprite getSprite() { return sprite; }
    public boolean isAlive() { return alive; }
    public Rectangle getHitBox() { return hitBox; }

    // Gets the center cordinates of the enemy
    public float getCenterXPos() {
        return  sprite.getX() + (sprite.getWidth() / 2);
    }

    public float getCenterYPos() {
        return  sprite.getY() + (sprite.getHeight() / 2);
    }

    // Method that makes the enemy take damage
    // it takes a damage argument that controls how much damage the enemy should take
    public void takeDamage(float damage) {
        health -= damage; // Removes health
        if (health <= 0) {
            remove(); // Destroys the enemy if the health is reduced below 0
        }
    }

    // A spawn method if I want to later implement respawning enemies
    public void spawn(float xPos, float yPos) {
        // Sets the position and makes it alive again
        sprite.setPosition(xPos, yPos);
        alive = true;
    }

    // Changes the position of the hitBox
    public void setHitBoxPos(float xPos, float yPos) {
        hitBox.setPosition(xPos, yPos);
    }



    public List<Node> getCurrentPath() { return currentPath; }

    public void remove() {
        sprite.setPosition(-100f, -100f);
        health = 0;
        alive = false;
    }

    // Gets the angle from the enemy to the point
    public float getAngleToPoint(float targetX, float targetY) {
        float centerX = getCenterXPos();
        float centerY = getCenterYPos();

        float deltaX = targetX - centerX;
        float deltaY = targetY - centerY;
        return (float) Math.atan2(deltaY, deltaX);
    }

    // A method that can be periodically run to move the enemy to a point
    public void moveTowardsPoint(float deltaTime, float targetX, float targetY) {
        // Gets the angle to the target
        float angle = getAngleToPoint(targetX, targetY);

        // Gets the amount the enemy should move with trig
        float xMoveAmount = (float) (speed * Math.cos(angle) * deltaTime);
        float yMoveAmount = (float) (speed * Math.sin(angle) * deltaTime);

        // Moves the sprite and updates the hit box
        sprite.translate(xMoveAmount, yMoveAmount);
        setHitBoxPos(sprite.getX(), sprite.getY());
    }

    // calculate the route to the player and moves through it
    public void navigateTowardsPlayer(float deltaTime, float targetX, float targetY, Pathfinder pathfinder) {
        // Ticks down the recalculation timer
        PATH_TIMER -= deltaTime;

        // calculates a new route tp the player upon timer expriing
        if (PATH_TIMER <= 0) {

            // gets the center cords of the enemy
            float centerX = getCenterXPos();
            float centerY = getCenterYPos();

            // Sets the current path
            currentPath = pathfinder.findPath(centerX, centerY, targetX, targetY);
            PATH_TIMER = PATH_REFRESH_RATE; // Reset timer
        }

        // as long as the path is valud
        if (currentPath != null && !currentPath.isEmpty()) {
            // Get the next node in the list
            Node nextNode = currentPath.get(0);

            // converts grid tile integers back into real world coordinates
            float nodeTargetX = (nextNode.gridX * TILE_SIZE) + (TILE_SIZE / 2f);
            float nodeTargetY = (nextNode.gridY * TILE_SIZE) + (TILE_SIZE / 2f);

            //Gets the enemy's center cords
            float centerX = getCenterXPos();
            float centerY = getCenterYPos();

            // Calculate distance to this node
            float deltaX = nodeTargetX - centerX;
            float deltaY = nodeTargetY - centerY;
            float distanceToNode = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);

            // if the node is within 0.15 units of the enemy check it off the list
            if (distanceToNode < 0.15f) {
                currentPath.remove(0);
            } else {
                // Otherwise, walk straight toward the node
                moveTowardsPoint(deltaTime, nodeTargetX, nodeTargetY);
            }
        } else {
            // If no path exists just walk toward the player
            moveTowardsPoint(deltaTime, targetX, targetY);
        }
    }

    // This method checks if there are enemies nearby
    public boolean areWallsNearby(float centerX, float centerY, float radius, GridManager gridManager) {
        // Creates a hit box around the enemy

        // Calculates the lower left position of the hit Box
        float checkX = centerX - radius;
        float checkY = centerY - radius;

        // The size should be double the radius
        float checkSize = radius * 2;
        Rectangle checkArea = new Rectangle(checkX, checkY, checkSize, checkSize); // Creats the rectangle

        // Gets the node grid from grid manager
        Node[][] grid = gridManager.getGrid();
        int gridColumns = grid.length;
        int gridRows = grid[0].length;

        // converts the world cordinates to grid format
        int startX = Math.max(0, (int) (checkX / TILE_SIZE));
        int startY = Math.max(0, (int) (checkY / TILE_SIZE));
        int endX = Math.min(gridColumns - 1, (int) ((checkX + checkSize) / TILE_SIZE));
        int endY = Math.min(gridRows - 1, (int) ((checkY + checkSize) / TILE_SIZE));

        // Creates a temporary Rectangle
        Rectangle wallBox = new Rectangle(0, 0, TILE_SIZE, TILE_SIZE);

        // Checks the nodes inside range
        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {

                // checks whether the node is walkable (is it a wall)
                if (!grid[x][y].isWalkable) {
                    // Changes the position of the temp rectangle
                    wallBox.setPosition(x * TILE_SIZE, y * TILE_SIZE);

                    // If the temp box overlaps the nearby wall node, it will return true
                    // there are walls nearb y
                    if (checkArea.overlaps(wallBox)) {
                        return true;
                    }
                }
            }
        }

        return false; // If it doesn't detect anything there are no walls nearby
    }

    // Draw method
    public void draw(SpriteBatch batch) {
        sprite.draw(batch);
    }
}
