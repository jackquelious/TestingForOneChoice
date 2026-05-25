package com.runnymede.game;

import com.badlogic.gdx.math.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class GridManager {
    // class variable
    // This is the grid of nodes on the map
    private Node[][] grid;

    // Constants
    // Since each node is 0.5 x 0.5, and the window is 8 x 5 the nodes will be 16 x 10
    private int gridColumns = 16;
    private int gridRows = 10;
    private float tileSize = 0.5f;

    // Constructor
    public GridManager(ArrayList<Rectangle> walls) {
        // initializes the gird and builds it
        grid = new Node[gridColumns][gridRows];
        buildGrid(walls);
    }

    // The important method
    // it builds the grid by splitting the map into nodes and recordes whether they are walkable or not
    private void buildGrid(ArrayList<Rectangle> walls) {

        // Uses two for loops to iterate through the 2d grid
        for (int x = 0; x < gridColumns; x++) {
            for (int y = 0; y < gridRows; y++) {
                boolean walkable = true;

                // Makes a temporary box for each node
                Rectangle nodeRect = new Rectangle(x * tileSize, y * tileSize, tileSize, tileSize);

                // Check if this square overlaps with any wall in the game
                for (Rectangle w : walls) {
                    if (nodeRect.overlaps(w)) {
                        walkable = false;
                        break;
                    }
                }

                // Save the new node in the array
                grid[x][y] = new Node(x, y, walkable);
            }
        }
    }

    // this finds which node a point is on
    // (used to know which node a character is on)
    public Node getNodeFromWorldPosition(float worldX, float worldY) {
        // Calculates where the point is on the grid
        int gridX = (int) (worldX / tileSize);
        int gridY = (int) (worldY / tileSize);

        // Safety checks to prevent errors
        if (gridX < 0) gridX = 0;
        if (gridX >= gridColumns) gridX = gridColumns - 1;
        if (gridY < 0) gridY = 0;
        if (gridY >= gridRows) gridY = gridRows - 1;

        // Returns the node
        return grid[gridX][gridY];
    }

    // Returns the nodes around a given node
    public List<Node> getNeighbors(Node node) {
        // Declares a new list of available nodes
        List<Node> neighbors = new ArrayList<>();

        // If the nodes are in bounds they are added to the list of adjacent nodes
        if (node.gridX + 1 < gridColumns) neighbors.add(grid[node.gridX + 1][node.gridY]); // Right
        if (node.gridX - 1 >= 0) neighbors.add(grid[node.gridX - 1][node.gridY]); // Left
        if (node.gridY + 1 < gridRows) neighbors.add(grid[node.gridX][node.gridY + 1]); // Up
        if (node.gridY - 1 >= 0) neighbors.add(grid[node.gridX][node.gridY - 1]); // Down

        return neighbors;
    }

    public Node getNode(int x, int y) {
        return grid[x][y];
    }

    public Node[][] getGrid(){
        return grid;
    }
}
