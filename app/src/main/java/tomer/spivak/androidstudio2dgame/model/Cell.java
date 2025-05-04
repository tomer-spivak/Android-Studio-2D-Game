package tomer.spivak.androidstudio2dgame.model;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import tomer.spivak.androidstudio2dgame.modelEnums.CellState;
import tomer.spivak.androidstudio2dgame.modelObjects.Building;
import tomer.spivak.androidstudio2dgame.modelObjects.Enemy;
import tomer.spivak.androidstudio2dgame.modelObjects.ModelObject;

public class Cell {
    private final Position position;
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
        building.setPosition(position);
    }

    public void spawnEnemy(Enemy enemy) {
        this.object = enemy;
        enemy.setPosition(position);
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

    public Map<String, Object> toMap() {
        Map<String, Object> cellData = new HashMap<>();
        cellData.put("position", position.toMap());
        cellData.put("occupied", object != null);
        cellData.put("object", object != null ? object.toMap() : null);
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

    public void executeBurntAnimation() {
        setState(CellState.BURNT);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                setState(CellState.NORMAL);
            }
        }, 200);
    }

    public void executeEnemyDeathAnimation() {
        final CellState[] states = {
                CellState.ENEMYDEATH1,
                CellState.ENEMYDEATH2,
                CellState.ENEMYDEATH3,
        };

        final int cyclesToRun = 3;
        final int stepsPerCycle = states.length;
        final int totalSteps = cyclesToRun * stepsPerCycle;
        final AtomicInteger stepCounter = new AtomicInteger(0);

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                int step = stepCounter.getAndIncrement();
                if (step < totalSteps) {
                    int stateIndex = step % stepsPerCycle;
                    setState(states[stateIndex]);
                } else {
                    setState(CellState.NORMAL);
                    scheduler.shutdown();
                }
            }
        }, 0, 300, TimeUnit.MILLISECONDS);
    }

}

