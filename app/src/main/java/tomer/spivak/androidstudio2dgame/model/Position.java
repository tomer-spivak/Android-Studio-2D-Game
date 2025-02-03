package tomer.spivak.androidstudio2dgame.model;

import java.util.HashMap;
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
        if (obj == null || getClass() != obj.getClass()) return false;
        Position other = (Position) obj;
        return x == other.x && y == other.y;
    }
    public Map<String, Object> toMap() {
        Map<String, Object> positionData = new HashMap<>();
        positionData.put("x", x);
        positionData.put("y", y);
        return positionData;
    }
}