package com.runnymede.game;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Pathfinder {
    private GridManager gridManager;

    public Pathfinder(GridManager gridManager) {
        this.gridManager = gridManager;
    }

    public List<Node> findPath(float startWorldX, float startWorldY, float targetWorldX, float targetWorldY) {
        // 1. Translate the floating world coordinates into our grid squares
        Node startNode = gridManager.getNodeFromWorldPosition(startWorldX, startWorldY);
        Node targetNode = gridManager.getNodeFromWorldPosition(targetWorldX, targetWorldY);

        // 2. Create our two notepads
        List<Node> openList = new ArrayList<>(); // "To-Do" List
        HashSet<Node> closedSet = new HashSet<>(); // "Done" List (HashSet is super fast for lookups)

        openList.add(startNode);

        // 3. The Explorer's Loop
        while (openList.size() > 0) {
            // Find the node in the open list with the lowest F-Cost
            Node currentNode = openList.get(0);
            for (int i = 1; i < openList.size(); i++) {
                if (openList.get(i).getFCost() < currentNode.getFCost() ||
                    (openList.get(i).getFCost() == currentNode.getFCost() && openList.get(i).hCost < currentNode.hCost)) {
                    currentNode = openList.get(i);
                }
            }

            // Move the current node from To-Do to Done
            openList.remove(currentNode);
            closedSet.add(currentNode);

            // Did we find the player?
            if (currentNode == targetNode) {
                return retracePath(startNode, targetNode);
            }

            // Check the neighbors!
            for (Node neighbor : gridManager.getNeighbors(currentNode)) {
                // If it's a wall or we already checked it, ignore it
                if (!neighbor.isWalkable || closedSet.contains(neighbor)) {
                    continue;
                }

                // Calculate the G-Cost (how far it is from the start)
                int newCostToNeighbor = currentNode.gCost + getDistance(currentNode, neighbor);

                // If this is a shorter path, or the neighbor isn't on the To-Do list yet
                if (newCostToNeighbor < neighbor.gCost || !openList.contains(neighbor)) {
                    neighbor.gCost = newCostToNeighbor;
                    neighbor.hCost = getDistance(neighbor, targetNode);
                    neighbor.parent = currentNode; // Leave a breadcrumb!

                    if (!openList.contains(neighbor)) {
                        openList.add(neighbor);
                    }
                }
            }
        }

        // If the loop finishes and we never found the target, no path exists
        return new ArrayList<>();
    }

    // Helper Method 1: Follow the breadcrumbs backward
    private List<Node> retracePath(Node startNode, Node endNode) {
        List<Node> path = new ArrayList<>();
        Node currentNode = endNode;

        while (currentNode != startNode) {
            path.add(currentNode);
            currentNode = currentNode.parent;
        }

        // The path is currently backwards (Target to Start), so we reverse it!
        java.util.Collections.reverse(path);
        return path;
    }

    // Helper Method 2: Calculate grid distance (Manhattan Distance for 4-way movement)
    private int getDistance(Node nodeA, Node nodeB) {
        int distanceX = Math.abs(nodeA.gridX - nodeB.gridX);
        int distanceY = Math.abs(nodeA.gridY - nodeB.gridY);
        return distanceX + distanceY;
    }
}
