package tomer.spivak.androidstudio2dgame.logic;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Position {
    private final int x;
    private final int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public List<Position> getNeighbors() {
        List<Position> neighbors = new ArrayList<>();

        neighbors.add(new Position(x + 1, y));
        if (x - 1 >= 0)
            neighbors.add(new Position(x - 1, y));
        neighbors.add(new Position(x, y + 1));
        if (y - 1 >= 0)
            neighbors.add(new Position(x, y - 1));
        return neighbors;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Position))
            return false;
        Position other = (Position) obj;
        return this.x == other.x && this.y == other.y;
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

    @NonNull
    @Override
    public String toString() {
        return "Position{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}