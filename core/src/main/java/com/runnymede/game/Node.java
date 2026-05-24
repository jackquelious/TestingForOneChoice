package com.runnymede.game;

// This is the node class for the pathfinding
public class Node {
    // CLASS VARIABLES:

    //Positions in the grid of nodes
    public int gridX;
    public int gridY;

    // It is walkable if no walls overlap (so enemies can't go through obsticles)
    public boolean isWalkable;

    // Pathfinding Costs
    public int gCost; // The distance from the starting node
    public int hCost; // The estimated distance to the player/target

    // This tracks what node comes before it in the pathfinding list
    public Node parent;

    // Constructor
    public Node(int gridX, int gridY, boolean isWalkable) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.isWalkable = isWalkable;
    }

    // Returns the total cose
    // *This is what the AI will compute to look at which nodes are best*//
    // The lowest FCost is allways the selected node in the chain

    public int getFCost() {
        return gCost + hCost; // Is equal to the sum of the two costs
    }
}
