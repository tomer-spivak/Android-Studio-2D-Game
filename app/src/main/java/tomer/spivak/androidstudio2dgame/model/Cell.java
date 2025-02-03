package tomer.spivak.androidstudio2dgame.model;

import java.util.HashMap;
import java.util.Map;

public class Cell {
    private final Position position; // Grid position (fixed)
    private ModelObject object;

    public Cell(Position position) {
        this.position = position;
    }

    public Cell(Position position, ModelObject object) {
        this.position = position;
        this.object = object;
    }

    public void placeBuilding(Building building) {
        this.object = building;
        building.setPosition(position); // Use grid position, not screen pixels
    }

    public void spawnEnemy(Enemy enemy) {
        this.object = enemy;
        enemy.setPosition(position); // Use grid position
    }

    // Getters
    public Position getPosition() {
        return position;
    }

    public ModelObject getObject() {
        return object;
    }

    public boolean isOccupied() {
        return object != null;
    }

    // Method to convert the Cell to a Map for Firestore
    public Map<String, Object> toMap() {
        Map<String, Object> cellData = new HashMap<>();
        cellData.put("position", position.toMap()); // Convert Position to Map
        cellData.put("occupied", object != null); // Boolean if occupied
        cellData.put("object", object != null ? object.toMap() : null); // Convert ModelObject to Map if not null
        return cellData;
    }
}

