package tomer.spivak.androidstudio2dgame.model;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tomer.spivak.androidstudio2dgame.modelEnums.AttackType;
import tomer.spivak.androidstudio2dgame.modelEnums.CellState;
import tomer.spivak.androidstudio2dgame.modelObjects.Building;
import tomer.spivak.androidstudio2dgame.modelAnimations.CellAnimationManager;
import tomer.spivak.androidstudio2dgame.modelObjects.Enemy;
import tomer.spivak.androidstudio2dgame.modelObjects.ModelObject;

public class Cell {
    private final Position position; // Grid position (fixed)
    private ModelObject object;
    private CellState cellState;

    public Cell(Position position) {
        this.position = position;
        cellState = CellState.NORMAL;
    }

    public Cell(Position position, ModelObject object) {
        this.position = position;
        this.object = object;
    }
    public Cell(Position position, ModelObject object, CellState cellState) {
        this.position = position;
        this.object = object;
        this.cellState = cellState;
    }

    public Cell(Position position, CellState cellState) {
        this.position = position;
        this.cellState = cellState;
    }

    public void placeBuilding(Building building) {
        this.object = building;
        building.setPosition(position); // Use grid position, not screen pixels
    }

    public void spawnEnemy(Enemy enemy) {
        this.object = enemy;
        enemy.setPosition(position); // Use grid position
    }

    public List<Cell> getNeighbors(GameState current) {
        List<Cell> neighbors = new ArrayList<>();
        for (Position neighborPos : position.getNeighbors()) {
            try {
                neighbors.add(current.getCellAt(neighborPos));
            } catch (Exception ignored) {
            }
        }
        return neighbors;
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

    public CellState getCellState() {
        return cellState;
    }

    public void setState(CellState cellState) {
        this.cellState = cellState;
    }

    // Method to convert the Cell to a Map for Firestore
    public Map<String, Object> toMap() {
        Map<String, Object> cellData = new HashMap<>();
        cellData.put("position", position.toMap()); // Convert Position to Map
        cellData.put("occupied", object != null); // Boolean if occupied
        cellData.put("object", object != null ? object.toMap() : null); // Convert ModelObject to Map if not null
        return cellData;
    }

    @NonNull
    @Override
    public String toString() {
        String str = "Cell{" +
                "position=" + position;
        if (object != null)
            str += ", object=" + object;
        return str + '}';
    }

    public void removeObject() {
        this.object = null;
    }

    public void cellAttacked(AttackType attackType) {
        CellAnimationManager.executeAttackedAnimation(this, attackType);
        // Schedule a task to reset the state after 200ms
    }
}

