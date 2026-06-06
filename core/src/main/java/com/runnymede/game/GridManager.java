package com.runnymede.game;

import java.util.ArrayList;
import java.util.List;
import com.badlogic.gdx.math.Rectangle;

public class GridManager {

    private Node[][] grid;
    private int gridColumns;
    private int gridRows;


    // Constants
    private float TILE_SIZE = 0.5f; // The size of each tile

    // Values that represent a wall or a floor
    public static final int TILE_WALL = 0;
    public static final int TILE_FLOOR = 1;

    // Constructor
    // Generates grid mased on map of tiles
    public GridManager(int[][] dungeonMap) {
        // Gets the size of the dungeon
        this.gridColumns = dungeonMap.length;
        this.gridRows = dungeonMap[0].length;

        // Makes the grid of nodes
        grid = new Node[gridColumns][gridRows];

        // Runs the build method
        buildGridFromMap(dungeonMap);
    }

    // Builds the grid
    // A floor is walkable, everything else is not
    private void buildGridFromMap(int[][] dungeonMap) {
        for (int x = 0; x < gridColumns; x++) {
            for (int y = 0; y < gridRows; y++) {
                // Node is walkable if the generator marked it as a floor
                boolean walkable = (dungeonMap[x][y] == TILE_FLOOR);
                grid[x][y] = new Node(x, y, walkable);
            }
        }
    }

    public Node getNodeFromWorldPosition(float worldX, float worldY) {
        int gridX = (int) (worldX / TILE_SIZE);
        int gridY = (int) (worldY / TILE_SIZE);

        if (gridX < 0) gridX = 0;
        if (gridX >= gridColumns) gridX = gridColumns - 1;
        if (gridY < 0) gridY = 0;
        if (gridY >= gridRows) gridY = gridRows - 1;

        return grid[gridX][gridY];
    }

    public List<Node> getNeighbors(Node node) {
        List<Node> neighbors = new ArrayList<>();

        if (node.gridX + 1 < gridColumns) neighbors.add(grid[node.gridX + 1][node.gridY]);
        if (node.gridX - 1 >= 0) neighbors.add(grid[node.gridX - 1][node.gridY]);
        if (node.gridY + 1 < gridRows) neighbors.add(grid[node.gridX][node.gridY + 1]);
        if (node.gridY - 1 >= 0) neighbors.add(grid[node.gridX][node.gridY - 1]);

        return neighbors;
    }

    // Determines if a moving entity's hitbox is overlapping an unwalkable grid tile
    public boolean checkWallCollision(Rectangle hitBox) {
        // Find the bounding grid coordinates that the hitbox covers
        int startX = (int) (hitBox.getX() / TILE_SIZE);
        int endX = (int) ((hitBox.getX() + hitBox.getWidth()) / TILE_SIZE);
        int startY = (int) (hitBox.getY() / TILE_SIZE);
        int endY = (int) ((hitBox.getY() + hitBox.getHeight()) / TILE_SIZE);

        // Bound checks to completely prevent array index out of bounds crashes
        startX = Math.max(0, startX);
        endX = Math.min(gridColumns - 1, endX);
        startY = Math.max(0, startY);
        endY = Math.min(gridRows - 1, endY);

        Rectangle tileBox = new Rectangle();

        // Scan only the localized tiles the entity is physically touching
        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                // If the tile is solid stone, construct a temporary box to check intersection
                if (!grid[x][y].isWalkable) {
                    tileBox.set(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                    if (hitBox.overlaps(tileBox)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public Node getNode(int x, int y) { return grid[x][y]; }
    public Node[][] getGrid() { return grid; }
    public int getGridColumns() { return gridColumns; }
    public int getGridRows() { return gridRows; }
    public float getTileSize() { return TILE_SIZE; }
}
