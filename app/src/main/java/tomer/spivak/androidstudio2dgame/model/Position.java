package tomer.spivak.androidstudio2dgame.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Position {
    private final int x; // Grid row index (e.g., 0 to 9 for a 10x10 grid)
    private final int y; // Grid column index

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // Calculate distance between two grid positions (for attack range checks)
    public float distanceTo(Position other) {
        // Use Manhattan distance for grid-based movement
        return Math.abs(x - other.x) + Math.abs(y - other.y);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Position)) return false;
        Position other = (Position) obj;
        return this.x == other.x && this.y == other.y;
    }

    public List<Position> getNeighbors() {
        List<Position> neighbors = new ArrayList<>();
        neighbors.add(new Position(x + 1, y)); // Right
        neighbors.add(new Position(x - 1, y)); // Left
        neighbors.add(new Position(x, y + 1)); // Down
        neighbors.add(new Position(x, y - 1)); // Up
        return neighbors;
    }

    @Override
    public int hashCode() {
        return 31 * x + y;
    }
    public Map<String, Object> toMap() {
        Map<String, Object> positionData = new HashMap<>();
        positionData.put("x", x);
        positionData.put("y", y);
        return positionData;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public String toString() {
        return "Position{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}