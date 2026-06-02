package com.runnymede.game;

public class Room {
    // CLASS VARIABLES
    public int x, y; // position
    public int width, height; // size

    // Tracks the type of room
    public RoomType type;

    public enum RoomType {
        START, NORMAL, BOSS, PORTAL
    }

    public Room(int x, int y, int width, int height, RoomType type) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.type = type;
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
}
