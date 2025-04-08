package tomer.spivak.androidstudio2dgame.model;


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

    public Position findClosestBuilding(Position enemyPos, List<Position> allBuildings) {
        Map<Position, Integer> bfsDistances = bfs(enemyPos);

        Position closestBuilding = null;
        int minDistance = Integer.MAX_VALUE;

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

    private Map<Position, Integer> bfs(Position start) {
        Map<Position, Integer> distances = new HashMap<>();
        Queue<Position> queue = new LinkedList<>();
        queue.add(start);
        distances.put(start, 0);

        while (!queue.isEmpty()) {
            Position current = queue.poll();
            int currentDist = distances.get(current);

            for (Position neighbor : current.getNeighbors()) {
                if (!gameState.isValidPosition(neighbor) || isPassablePosition(neighbor) ||
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
        return gameState.getGrid()[pos.getX()][pos.getY()].isOccupied();
    }

    public List<Position> findPath(Position start, Position goal) {
        Map<Position, Position> cameFrom = new HashMap<>();
        Queue<Position> frontier = new LinkedList<>();
        frontier.add(start);
        cameFrom.put(start, null);

        while (!frontier.isEmpty()) {
            Position current = frontier.poll();

            if (current.equals(goal)) {
                break;
            }

            for (Position neighbor : current.getNeighbors()) {
                if (!gameState.isValidPosition(neighbor) || isPassablePosition(neighbor)) {
                    continue;
                }
                if (!cameFrom.containsKey(neighbor)) {
                    frontier.add(neighbor);
                    cameFrom.put(neighbor, current);
                }
            }
        }

        if (!cameFrom.containsKey(goal)) {
            return new ArrayList<>();
        }

        List<Position> path = new ArrayList<>();
        Position current = goal;
        while (current != null) {
            path.add(0, current);
            current = cameFrom.get(current);
        }

        if (!path.isEmpty() && path.get(0).equals(start)) {
            path.remove(0);
        }

        return path;
    }

}
