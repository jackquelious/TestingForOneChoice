package com.runnymede.game;

import java.util.ArrayList;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Rectangle;

public class Room {
    // Constants
    private static float TILE_SIZE = 0.5f;

    // CLASS VARIABLES
    public int x, y; // position
    public int width, height; // size

    // Tracks configuration and operational states
    public RoomType type;
    private RoomState state;
    private Rectangle triggerBox;  // When player hits the trigger box the enemies spawn

    // Holds the exact coordinates where hallways connect to this room
    private ArrayList<GridPoint2> doors;

    public enum RoomType {
        START, NORMAL, BOSS, PORTAL
    }

    public enum RoomState {
        UNVISITED, LOCKED, CLEARED
    }

    public Room(int x, int y, int width, int height, RoomType type) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.type = type;
        this.state = RoomState.UNVISITED;
        this.doors = new ArrayList<>();
        createTriggerBox(TILE_SIZE);
    }

    // Checks if this room overlaps another room to prevent clipping
    public boolean overlaps(Room other) {
        return x < other.x + other.width &&
            x + width > other.x &&
            y < other.y + other.height &&
            y + height > other.y;
    }

    // Finds the horizontal center tile for hallway connections
    public int getCenterX() {
        return x + (width / 2);
    }

    // Finds the vertical center tile for hallway connections
    public int getCenterY() {
        return y + (height / 2);
    }

    /**
     * Scans the 10x10 outer frame of the room against the generated layout map.
     * Any floor tile found on the border is logged as a doorway threshold.
     */
    public void findDoors(int[][] map) {
        doors.clear();

        // Scan bottom and top perimeter edges
        for (int col = x; col < x + width; col++) {
            if (map[col][y] == 1) doors.add(new GridPoint2(col, y));
            if (map[col][y + height - 1] == 1) doors.add(new GridPoint2(col, y + height - 1));
        }

        // Scan left and right perimeter edges
        for (int row = y; row < y + height; row++) {
            if (map[x][row] == 1) doors.add(new GridPoint2(x, row));
            if (map[x + width - 1][row] == 1) doors.add(new GridPoint2(x + width - 1, row));
        }
    }

    /**
     * Seals all registered doorways by converting grid pathfinding nodes to unwalkable.
     */
    public void lockDoors(GridManager gridManager) {
        Node[][] grid = gridManager.getGrid();
        for (GridPoint2 door : doors) {
            if (door.x >= 0 && door.x < grid.length && door.y >= 0 && door.y < grid[0].length) {
                grid[door.x][door.y].isWalkable = false;
            }
        }
        this.state = RoomState.LOCKED;
    }

    /**
     * Re-opens all registered doorways by restoring grid pathfinding nodes to walkable.
     */
    public void unlockDoors(GridManager gridManager) {
        Node[][] grid = gridManager.getGrid();
        for (GridPoint2 door : doors) {
            if (door.x >= 0 && door.x < grid.length && door.y >= 0 && door.y < grid[0].length) {
                grid[door.x][door.y].isWalkable = true;
            }
        }
        this.state = RoomState.CLEARED;
    }

    public void createTriggerBox(float tileSize) {
        // We position the hitbox 1 tile inward from the bottom-left corner
        float boxX = (this.x + 2) * tileSize;
        float boxY = (this.y + 2) * tileSize;

        // We make the box 2 tiles skinnier and shorter than the room so it doesn't touch the doors
        float boxWidth = (this.width - 4) * tileSize;
        float boxHeight = (this.height - 4) * tileSize;

        this.triggerBox = new Rectangle(boxX, boxY, boxWidth, boxHeight);
    }



    // Getters and Setters for state integration
    public RoomState getState() { return state; }
    public void setState(RoomState state) { this.state = state; }
    public ArrayList<GridPoint2> getDoors() { return doors; }
    public Rectangle getTriggerBox() { return triggerBox; }
}
