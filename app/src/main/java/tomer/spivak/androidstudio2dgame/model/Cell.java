package tomer.spivak.androidstudio2dgame.model;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import tomer.spivak.androidstudio2dgame.modelEnums.CellState;
import tomer.spivak.androidstudio2dgame.modelObjects.Building;
import tomer.spivak.androidstudio2dgame.modelObjects.Enemy;
import tomer.spivak.androidstudio2dgame.modelObjects.ModelObject;

public class Cell {
    private final Position position;
    private ModelObject object;
    private CellState cellState;
    private final CellState defaultState;
    private long animationTime = 0;

    public Cell(Position position, CellState defaultState) {
        this.position = position;
        this.cellState = defaultState;
        this.defaultState = defaultState;
    }

    public void placeBuilding(Building building) {
        this.object = building;
    }

    public void spawnEnemy(Enemy enemy) {
        this.object = enemy;
        enemy.setPosition(position);
    }

    public void updateAnimation(long deltaTime){
        animationTime += deltaTime;
        if(cellState == CellState.BURNT) {
            if (animationTime > 500) {
                setState(defaultState);
            }
        }
        if(cellState.name().contains("DEATH")) {
            final CellState[] states = {CellState.ENEMYDEATH1, CellState.ENEMYDEATH2, CellState.ENEMYDEATH3};
            final int cycles = 3;
            final float frameDur = 300;
            final int totalFrames = states.length * cycles;
            final float totalTime = totalFrames * frameDur;
            if (animationTime >= totalTime) {
                setState(defaultState);
            } else {
                int step = (int)(animationTime / frameDur);
                int frameIndex = step % states.length;
                setState(states[frameIndex]);
            }
        }
        if(cellState == CellState.EXPLODE){
            if (animationTime > 800) {
                setState(defaultState);
            }
        }
    }

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

    public void removeObject() {
        this.object = null;
    }

    public void executeBurntAnimation() {
        setState(CellState.BURNT);
        animationTime = 0;
    }

    public void executeEnemyDeathAnimation() {
        setState(CellState.ENEMYDEATH1);
        animationTime = 0L;
    }

    public void executeExplosion() {
        setState(CellState.EXPLODE);
        animationTime = 0;
    }

    public void resetAnimation() {
        setState(defaultState);
        animationTime = 0;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> cellData = new HashMap<>();
        cellData.put("position", position.toMap());
        cellData.put("occupied", object != null);
        cellData.put("object", object != null ? object.toMap() : null);
        Log.d("state", cellState.name());
        cellData.put("state", cellState.name());
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
}