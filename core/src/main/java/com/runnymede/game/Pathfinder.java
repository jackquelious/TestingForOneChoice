package com.runnymede.game;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Pathfinder {
    private GridManager gridManager; // Uses a grid manager

    private static float TILE_SIZE = 0.5f;

    public Pathfinder(GridManager gridManager) {
        this.gridManager = gridManager; //  initializes grid manager
    }

    // IMPORTANT METHOD
    // returns the list of nodes that leads to the player
    public List<Node> findPath(float startWorldX, float startWorldY, float targetWorldX, float targetWorldY) {
        // 1. Convert real world floats into grid integers
        int startGridX = (int)(startWorldX / TILE_SIZE);
        int startGridY = (int)(startWorldY / TILE_SIZE);
        int targetGridX = (int)(targetWorldX / TILE_SIZE);
        int targetGridY = (int)(targetWorldY / TILE_SIZE);

        // Guardrails: Keep the indices safely within your 60x60 grid arrays
        targetGridX = Math.max(0, Math.min(targetGridX, gridManager.getGridColumns() - 1));
        targetGridY = Math.max(0, Math.min(targetGridY, gridManager.getGridRows() - 1));

        Node[][] grid = gridManager.getGrid();

        // 2. THE FIX: If the retreat point is a wall, find a nearby floor tile!
        if (!grid[targetGridX][targetGridY].isWalkable) {
            // Look at the 4 adjacent tiles (Left, Right, Down, Up) to find a floor
            if (targetGridX - 1 >= 0 && grid[targetGridX - 1][targetGridY].isWalkable) {
                targetGridX -= 1;
            } else if (targetGridX + 1 < gridManager.getGridColumns() && grid[targetGridX + 1][targetGridY].isWalkable) {
                targetGridX += 1;
            } else if (targetGridY - 1 >= 0 && grid[targetGridX][targetGridY - 1].isWalkable) {
                targetGridY -= 1;
            } else if (targetGridY + 1 < gridManager.getGridRows() && grid[targetGridX][targetGridY + 1].isWalkable) {
                targetGridY += 1;
            }
        }

        // gets the nodes of the start and end points
        Node startNode = gridManager.getNodeFromWorldPosition(startWorldX, startWorldY);
        Node targetNode = gridManager.getNodeFromWorldPosition(targetWorldX, targetWorldY);


        // Creates two lists that track which nodes need to be explored and which nodes are already explored
        List<Node> openList = new ArrayList<>(); // tracks the to-be explored nodes
        HashSet<Node> closedSet = new HashSet<>(); // list of already checked off nodes

        // Starts by adding the start node to the to-do list
        openList.add(startNode);

        // looks at the to-do list and picks the node with the lowest f-cost (distance to player, and distance from start)
        while (openList.size() > 0) {
            // Find the node in the open list with the lowest F-Cost
            Node currentNode = openList.get(0);
            for (int i = 1; i < openList.size(); i++) {
                if (openList.get(i).getFCost() < currentNode.getFCost() ||
                    (openList.get(i).getFCost() == currentNode.getFCost() && openList.get(i).hCost < currentNode.hCost)) {
                    currentNode = openList.get(i);
                }
            }

            // checks off the selected node from the to-do list
            openList.remove(currentNode);
            closedSet.add(currentNode);

            // checks if the selected node is the target
            if (currentNode == targetNode) {
                return retracePath(startNode, targetNode); // returns the path of nodes that lead to it
            }

            // otherwise it looks for any neighbor nodes that are walkable and not already checked off
            for (Node neighbor : gridManager.getNeighbors(currentNode)) {
                if (!neighbor.isWalkable || closedSet.contains(neighbor)) {
                    continue;
                }

                // Calculates the G-Cost (how far it is from the start)
                int newCostToNeighbor = currentNode.gCost + getDistance(currentNode, neighbor);

                // If this is a shorter path, or the neighbor isn't on the To-Do list yet
                // this will add it to the to-do list
                if (newCostToNeighbor < neighbor.gCost || !openList.contains(neighbor)) {
                    // sets all the distance variables
                    neighbor.gCost = newCostToNeighbor;
                    neighbor.hCost = getDistance(neighbor, targetNode);
                    neighbor.parent = currentNode; // tracks wihch node lead to this node so it can be retraced later

                    // if the to-do list does not contain it, add it
                    if (!openList.contains(neighbor)) {
                        openList.add(neighbor);
                    }
                }
            }
        }

        // If the loop finishe and we never found the target return an empty path
        return new ArrayList<>();
    }

    // Returns the path to the end node
    private List<Node> retracePath(Node startNode, Node endNode) {
        // makes the list
        List<Node> path = new ArrayList<>();

        // Starts by selecting the last node
        Node currentNode = endNode;

        // Traces the path backwards untilll it reaches the start
        while (currentNode != startNode) {
            // adds the current node to the list and gets the parent of the current node
            path.add(currentNode);
            currentNode = currentNode.parent;
        }

        // Reverses the path so it starts at the start, not the end
        java.util.Collections.reverse(path);

        // returns the path
        return path;
    }

    // gets the distance to two nodes (the sum of the x and y parts)
    private int getDistance(Node nodeA, Node nodeB) {
        int distanceX = Math.abs(nodeA.gridX - nodeB.gridX);
        int distanceY = Math.abs(nodeA.gridY - nodeB.gridY);
        return distanceX + distanceY;
    }
}
