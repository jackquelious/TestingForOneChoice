package com.runnymede.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

import java.util.List;

public class Enemy {
    // CLASS VARIABLES
    private float speed;
    private int health;
    private int damage;
    private Texture texture;
    private Sprite sprite;
    private boolean alive;
    private Rectangle hitBox;

    // For pathfinding
    private List<Node> currentPath;
    private float pathTimer = 0f;
    private final float pathRefreshRate = 0.25f; // Recalculates route 4 times a second
    private final float tileSize = 0.5f;

    public Enemy(float xPos, float yPos, float speed, int health, int damage) {
        this.speed = speed;
        this.health = health;
        this.damage = damage;

        this.texture = new Texture("enemySquare.png");
        this.sprite = new Sprite(texture);

        this.sprite.setSize(0.4f, 0.4f);
        this.sprite.setPosition(xPos, yPos);
        this.hitBox = new Rectangle(xPos, yPos, sprite.getWidth(), sprite.getHeight());
        this.alive = true;
    }

    // Getters
    public float getXPos() { return sprite.getX(); }
    public float getYPos() { return sprite.getY(); }
    public float getSpeed() { return speed; }
    public int getHealth() { return health; }
    public int getDamage() { return damage; }
    public Texture getTexture() { return texture; }
    public Sprite getSprite() { return sprite; }
    public boolean isAlive() { return alive; }
    public Rectangle getHitBox() { return hitBox; }

    public void takeDamage(int damage) {
        health -= damage;
        if (health <= 0) {
            remove();
        }
    }

    public void spawn(float xPos, float yPos) {
        sprite.setPosition(xPos, yPos);
        alive = true;
    }

    public void setHitBoxPos(float xPos, float yPos) {
        hitBox.setPosition(xPos, yPos);
    }

    public float getCenterXPos() {
        return  sprite.getX() + (sprite.getWidth() / 2);
    }

    public float getCenterYPos() {
        return  sprite.getY() + (sprite.getHeight() / 2);
    }

    public List<Node> getCurrentPath() { return currentPath; }

    public void remove() {
        sprite.setPosition(-100f, -100f);
        health = 0;
        alive = false;
    }

    public float getAngleToPoint(float targetX, float targetY) {
        float centerX = getCenterXPos();
        float centerY = getCenterYPos();

        float deltaX = targetX - centerX;
        float deltaY = targetY - centerY;
        return (float) Math.atan2(deltaY, deltaX);
    }

    public void moveTowardsPoint(float deltaTime, float targetX, float targetY) {
        float angle = getAngleToPoint(targetX, targetY);

        float xMoveAmount = (float) (speed * Math.cos(angle) * deltaTime);
        float yMoveAmount = (float) (speed * Math.sin(angle) * deltaTime);

        float newXPos = getXPos() + xMoveAmount;
        float newYPos = getYPos() + yMoveAmount;

        sprite.setPosition(newXPos, newYPos);
        setHitBoxPos(newXPos, newYPos);
    }

    // calculate the route to the player and moves through it
    public void navigateTowardsPlayer(float deltaTime, float targetX, float targetY, Pathfinder pathfinder) {
        // Ticks down the recalculation timer
        pathTimer -= deltaTime;

        // calculates a new route tp the player upon timer expriing
        if (pathTimer <= 0) {

            // gets the center cords of the enemy
            float centerX = getCenterXPos();
            float centerY = getCenterYPos();

            currentPath = pathfinder.findPath(centerX, centerY, targetX, targetY);
            pathTimer = pathRefreshRate; // Reset timer
        }

        // as long as the path is valud
        if (currentPath != null && !currentPath.isEmpty()) {
            // Get the next node in the list
            Node nextNode = currentPath.get(0);

            // converts grid tile integers back into real world coordinates
            float nodeTargetX = (nextNode.gridX * tileSize) + (tileSize / 2f);
            float nodeTargetY = (nextNode.gridY * tileSize) + (tileSize / 2f);

            //Gets the enemy's center cords
            float centerX = getCenterXPos();
            float centerY = getCenterYPos();

            // Calculate distance to this node
            float deltaX = nodeTargetX - centerX;
            float deltaY = nodeTargetY - centerY;
            float distanceToNode = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);

            // if the node is within 0.25 units of the enemy check it off the list
            if (distanceToNode < 0.25f) {
                currentPath.remove(0);
            } else {
                // Otherwise, walk straight toward that specific node
                moveTowardsPoint(deltaTime, nodeTargetX, nodeTargetY);
            }
        } else {
            // Fallback: If no path exists (or we reached the end), just walk straight at the player
            moveTowardsPoint(deltaTime, targetX, targetY);
        }
    }

    public void draw(SpriteBatch batch) {
        sprite.draw(batch);
    }

    public boolean areWallsNearby(float centerX, float centerY, float radius, GridManager gridManager) {
        float tileSize = 0.5f; // Matching your GridManager's tileSize

        // 1. Create a bounding box centered on the enemy, expanded by the radius
        float checkX = centerX - radius;
        float checkY = centerY - radius;
        float checkSize = radius * 2;
        Rectangle checkArea = new Rectangle(checkX, checkY, checkSize, checkSize);

        // 2. Retrieve the 2D node array from your GridManager
        Node[][] grid = gridManager.getGrid();
        int gridColumns = grid.length;
        int gridRows = grid[0].length;

        // 3. Convert world coordinates to grid bounds so we only check relevant tiles
        int startX = Math.max(0, (int) (checkX / tileSize));
        int startY = Math.max(0, (int) (checkY / tileSize));
        int endX = Math.min(gridColumns - 1, (int) ((checkX + checkSize) / tileSize));
        int endY = Math.min(gridRows - 1, (int) ((checkY + checkSize) / tileSize));

        // 4. Create a temporary rectangle for wall testing
        Rectangle wallBox = new Rectangle(0, 0, tileSize, tileSize);

        // 5. Scan ONLY the tiles inside our checkArea
        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {

                // If the node is NOT walkable, it is a wall!
                if (!grid[x][y].isWalkable) {
                    // Position our temporary wall box at this tile's world position
                    wallBox.setPosition(x * tileSize, y * tileSize);

                    // If our expanded enemy box hits this wall tile, a wall is nearby!
                    if (checkArea.overlaps(wallBox)) {
                        return true;
                    }
                }
            }
        }

        return false; // Coast is clear!
    }
}
