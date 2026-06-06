package com.runnymede.game;

import com.badlogic.gdx.math.MathUtils;
import java.util.ArrayList;
import java.util.Random;

public class DungeonGenerator {

    // Tile definitions matching your GridManager
    private static final int TILE_WALL = 0;
    private static final int TILE_FLOOR = 1;

    // Fixed dimensions for every individual room template
    private static final int ROOM_SIZE = 10;

    // Dimensions for our invisible room slot macro-grid
    private static final int GRID_WIDTH = 6;
    private static final int GRID_HEIGHT = 6;

    // Visual text blueprints for room variety
    private static final String[] EMPTY_ROOM = {
        "##########",
        "#........#",
        "#........#",
        "#........#",
        "#........#",
        "#........#",
        "#........#",
        "#........#",
        "#........#",
        "##########"
    };

    private static final String[] CENTER_PILLAR_ROOM = {
        "##########",
        "#........#",
        "#........#",
        "#...##...#",
        "#...##...#",
        "#...##...#",
        "#...##...#",
        "#........#",
        "#........#",
        "##########"
    };

    private static final String[] FOUR_CORNERS_ROOM = {
        "##########",
        "#..#..#..#",
        "#........#",
        "##......##",
        "#........#",
        "#........#",
        "##......##",
        "#........#",
        "#..#..#..#",
        "##########"
    };

    private int[][] map;
    private int mapWidth;
    private int mapHeight;

    // Our structured slot tracking structures
    private Room[][] roomGrid;
    private ArrayList<Room> placedRooms;

    public DungeonGenerator(int mapWidth, int mapHeight) {
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.map = new int[mapWidth][mapHeight];
        this.placedRooms = new ArrayList<>();
        this.roomGrid = new Room[GRID_WIDTH][GRID_HEIGHT];
    }

    public int[][] generateFloor() {
        Random rand = new Random();
        // Phase 1: Preparing the Canvas (Fill the entire world with solid stone walls)
        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                map[x][y] = TILE_WALL;
            }
        }

        // Clear out our data arrays to ensure a totally fresh floor state
        placedRooms.clear();
        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                roomGrid[x][y] = null;
            }
        }

        // Phase 2: Setting up the Starting Anchor right in the middle slot
        int currentGridX = 3;
        int currentGridY = 3;
        placeRoomInSlot(currentGridX, currentGridY, Room.RoomType.START);

        int roomsToPlace = 10;
        int roomsPlaced = 0;

        // Phase 3 & 4: The Drunkard's Walk Selection and Placement Loop
        while (roomsPlaced < roomsToPlace) {
            // Pick a random direction: 0=Up, 1=Down, 2=Left, 3=Right
            int direction = rand.nextInt(0, 4);

            if (direction == 0) currentGridY += 1;
            else if (direction == 1) currentGridY -= 1;
            else if (direction == 2) currentGridX -= 1;
            else if (direction == 3) currentGridX += 1;

            // Guardrails: stop the walker from sliding off the edge indices
            currentGridX = MathUtils.clamp(currentGridX, 0, GRID_WIDTH - 1);
            currentGridY = MathUtils.clamp(currentGridY, 0, GRID_HEIGHT - 1);

            // Validation check: only place a room if the chosen slot is empty
            if (roomGrid[currentGridX][currentGridY] == null) {
                // If this is the last room needed, designate it as the Boss room
                Room.RoomType type = (roomsPlaced == roomsToPlace - 1) ? Room.RoomType.BOSS : Room.RoomType.NORMAL;

                placeRoomInSlot(currentGridX, currentGridY, type);
                roomsPlaced++;
            }
        }

        // Phase 5: Linking the Finished Layout with straight corridors
        connectGridRooms();

        return map;
    }

    private void placeRoomInSlot(int gridX, int gridY, Room.RoomType type) {
        // Map grid slot indices back into matching raw tile coordinates
        int pixelX = gridX * ROOM_SIZE;
        int pixelY = gridY * ROOM_SIZE;

        Room newRoom = new Room(pixelX, pixelY, ROOM_SIZE, ROOM_SIZE, type);

        // Register the new room in both tracking configurations
        roomGrid[gridX][gridY] = newRoom;
        placedRooms.add(newRoom);

        // Fetch a prefab and carve its visual footprint into the layout matrix
        String[] template = getRandomTemplate(type);
        carvePrefab(newRoom, template);
    }

    private void carvePrefab(Room room, String[] template) {
        for (int prefabY = 0; prefabY < ROOM_SIZE; prefabY++) {
            String row = template[prefabY];
            for (int prefabX = 0; prefabX < ROOM_SIZE; prefabX++) {
                char tileChar = row.charAt(prefabX);
                int worldX = room.x + prefabX;
                // Invert Y reading because arrays read top-down while rendering engines read bottom-up
                int worldY = room.y + (ROOM_SIZE - 1 - prefabY);

                if (tileChar == '.') {
                    map[worldX][worldY] = TILE_FLOOR;
                } else {
                    map[worldX][worldY] = TILE_WALL;
                }
            }
        }
    }

    private String[] getRandomTemplate(Room.RoomType type) {
        if (type == Room.RoomType.START || type == Room.RoomType.BOSS || type == Room.RoomType.PORTAL) {
            return EMPTY_ROOM;
        }

        int roll = MathUtils.random(0, 2);
        if (roll == 1) return CENTER_PILLAR_ROOM;
        if (roll == 2) return FOUR_CORNERS_ROOM;
        return EMPTY_ROOM;
    }

    private void connectGridRooms() {
        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                Room currentRoom = roomGrid[x][y];
                if (currentRoom == null) continue;

                // Connect to a neighboring room immediately to the right
                if (x + 1 < GRID_WIDTH && roomGrid[x + 1][y] != null) {
                    carveHorizontalHallway(currentRoom, roomGrid[x + 1][y]);
                }

                // Connect to a neighboring room immediately above
                if (y + 1 < GRID_HEIGHT && roomGrid[x][y + 1] != null) {
                    carveVerticalHallway(currentRoom, roomGrid[x][y + 1]);
                }
            }
        }
    }

    private void carveHorizontalHallway(Room leftRoom, Room rightRoom) {
        int yLevel = leftRoom.getCenterY();
        for (int x = leftRoom.getCenterX(); x <= rightRoom.getCenterX(); x++) {
            map[x][yLevel] = TILE_FLOOR;
            if (yLevel + 1 < mapHeight) map[x][yLevel + 1] = TILE_FLOOR;
            if (yLevel - 1 >= 0) map[x][yLevel - 1] = TILE_FLOOR;
        }
    }

    private void carveVerticalHallway(Room bottomRoom, Room topRoom) {
        int xLevel = bottomRoom.getCenterX();
        for (int y = bottomRoom.getCenterY(); y <= topRoom.getCenterY(); y++) {
            map[xLevel][y] = TILE_FLOOR;
            if (xLevel + 1 < mapWidth) map[xLevel + 1][y] = TILE_FLOOR;
            if (xLevel - 1 >= 0) map[xLevel - 1][y] = TILE_FLOOR;
        }
    }

    public Room getStartRoom() {
        return placedRooms.get(0);
    }

    public ArrayList<Room> getPlacedRooms() {
        return placedRooms;
    }
}
