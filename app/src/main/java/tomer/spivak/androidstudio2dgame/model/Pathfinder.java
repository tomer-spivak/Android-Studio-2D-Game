package tomer.spivak.androidstudio2dgame.model;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class Pathfinder {
    private final GameState gameState;
    public Pathfinder(GameState gameState) {
        this.gameState = gameState;
    }

    // Returns the closest REACHABLE building to the enemy's position, or null if none exist
    public Position findClosestBuilding(Position enemyPos, List<Position> allBuildings) {
        // Step 1: Run BFS from the enemy's position to compute distances to all tiles
        Map<Position, Integer> bfsDistances = bfs(enemyPos);

        // Step 2: Iterate through all buildings to find the closest one
        Position closestBuilding = null;
        int minDistance = Integer.MAX_VALUE;

        Log.d("enemyspawn", "keyset: " + bfsDistances.keySet());

        for (Position entry : bfsDistances.keySet()){
            Log.d("enemyspawn", "key: " + entry);
        }

        for (Position building : allBuildings) {
            if (bfsDistances.containsKey(building)) {
                int distance = bfsDistances.get(building);
                if (distance < minDistance) {
                    minDistance = distance;
                    closestBuilding = building;
                }
            }
        }

        return closestBuilding;
    }

    // BFS to compute shortest distances from a starting position
    private Map<Position, Integer> bfs(Position start) {
        Map<Position, Integer> distances = new HashMap<>();
        Queue<Position> queue = new LinkedList<>();
        queue.add(start);
        distances.put(start, 0);

        while (!queue.isEmpty()) {
            Position current = queue.poll();
            int currentDist = distances.get(current);

            // Explore all 4-directional neighbors
            for (Position neighbor : current.getNeighbors()) {
                // Skip if neighbor is out of bounds, blocked, or already visited
                if (!gameState.isValidPosition(neighbor) || !isPassablePosition(neighbor) ||
                        distances.containsKey(neighbor)) {
                    continue;
                }

                distances.put(neighbor, currentDist + 1);
                queue.add(neighbor);
            }
        }

        return distances;
    }



    private boolean isPassablePosition(Position pos) {
        // Check if the position is valid AND not occupied
        return !gameState.getGrid()[pos.getX()][pos.getY()].isOccupied();
    }


    public List<Position> findPath(Position start, Position goal) {
        Map<Position, Position> cameFrom = new HashMap<>();
        Queue<Position> frontier = new LinkedList<>();
        frontier.add(start);
        cameFrom.put(start, null);

        while (!frontier.isEmpty()) {
            Position current = frontier.poll();

            // Stop if we've reached the goal
            if (current.equals(goal)) {
                break;
            }

            for (Position neighbor : current.getNeighbors()) {
                if (!gameState.isValidPosition(neighbor) || !isPassablePosition(neighbor)) {
                    continue;
                }
                if (!cameFrom.containsKey(neighbor)) {
                    frontier.add(neighbor);
                    cameFrom.put(neighbor, current);
                }
            }
        }

        // If the goal wasn't reached, return an empty path.
        if (!cameFrom.containsKey(goal)) {
            return new ArrayList<>();
        }

        // Reconstruct the path from goal to start.
        List<Position> path = new ArrayList<>();
        Position current = goal;
        while (current != null) {
            path.add(0, current);  // Insert at the beginning.
            current = cameFrom.get(current);
        }

        // Remove the starting position if present.
        if (!path.isEmpty() && path.get(0).equals(start)) {
            path.remove(0);
        }

        return path;
    }


}
