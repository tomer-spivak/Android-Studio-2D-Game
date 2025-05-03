package tomer.spivak.androidstudio2dgame.model;


import java.util.*;

import tomer.spivak.androidstudio2dgame.modelObjects.Building;

public class Pathfinder {
    private final GameState gameState;

    public Pathfinder(GameState gameState) {
        this.gameState = gameState;
    }

    /**
     * Finds the shortest path from `start` to the closest neighboring cell of any Building.
     * Expands outwards ring by ring: first building neighbors, then their neighbors, etc.
     * Returns an empty list if no building or no reachable neighbor exists.
     */
    public List<Position> findPathToClosestBuildingNeighbor(Position start) {
        Map<Position, Position> cameFrom = new HashMap<>();
        Queue<Position> frontier = new LinkedList<>();

        // ✅ NEW: Check if already adjacent to a building
        if (hasAdjacentBuilding(start)) {
            return Collections.emptyList(); // No movement needed; enemy is adjacent already
        }

        // Existing: Check if any neighbor is adjacent to a building
        for (Position nb : start.getNeighbors()) {
            if (!gameState.isValidPosition(nb) || isOccupied(nb)) {
                continue;
            }

            if (hasAdjacentBuilding(nb)) {
                return Collections.singletonList(nb);
            }

            cameFrom.put(nb, start);
            frontier.add(nb);
        }

        // Standard BFS...
        while (!frontier.isEmpty()) {
            Position cur = frontier.poll();
            if (cur == null)
                continue;
            for (Position nb : cur.getNeighbors()) {
                if (!gameState.isValidPosition(nb) || isOccupied(nb) || cameFrom.containsKey(nb)) {
                    continue;
                }

                if (hasAdjacentBuilding(nb)) {
                    cameFrom.put(nb, cur);
                    return reconstructPath(start, nb, cameFrom);
                }

                cameFrom.put(nb, cur);
                frontier.add(nb);
            }
        }

        return null;
    }

    /** Returns true if any of pos’s 4-neighbours has a Building in it. */
    private boolean hasAdjacentBuilding(Position pos) {
        for (Position nb : pos.getNeighbors()) {
            if (!gameState.isValidPosition(nb))
                continue;
            Cell c = gameState.getCellAt(nb);
            if (c.getObject() instanceof Building) {
                return true;
            }
        }
        return false;
    }

    /**
     * Standard BFS that stops at the first reached target and reconstructs the path.
     */

    private List<Position> reconstructPath(Position start, Position goal, Map<Position, Position> cameFrom) {
        List<Position> path = new ArrayList<>();
        for (Position p = goal; p != null; p = cameFrom.get(p)) {
            path.add(0, p);
        }
        if (!path.isEmpty() && path.get(0).equals(start)) {
            path.remove(0);
        }

        return path;
    }

    private boolean isOccupied(Position pos) {
        return gameState.getGrid()[pos.getX()][pos.getY()].isOccupied();
    }
}
