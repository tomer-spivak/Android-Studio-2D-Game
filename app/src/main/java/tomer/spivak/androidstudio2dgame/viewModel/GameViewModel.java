package tomer.spivak.androidstudio2dgame.viewModel;


import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import tomer.spivak.androidstudio2dgame.modelEnums.GameStatus;
import tomer.spivak.androidstudio2dgame.modelObjects.Building;
import tomer.spivak.androidstudio2dgame.model.Cell;
import tomer.spivak.androidstudio2dgame.modelObjects.Enemy;
import tomer.spivak.androidstudio2dgame.model.GameState;
import tomer.spivak.androidstudio2dgame.modelEnums.EnemyState;
import tomer.spivak.androidstudio2dgame.modelObjects.ModelObject;
import tomer.spivak.androidstudio2dgame.modelObjects.ModelObjectFactory;
import tomer.spivak.androidstudio2dgame.model.Pathfinder;
import tomer.spivak.androidstudio2dgame.model.Position;
import tomer.spivak.androidstudio2dgame.modelObjects.Turret;

public class GameViewModel extends ViewModel {
    private final MutableLiveData<GameState> gameState = new MutableLiveData<>();
    private String selectedBuildingType;
    private long accumulatedDayTime = 0;
    // Define the threshold for night start (5000 ms in this case).
    private static final long NIGHT_THRESHOLD = 5000;

    public void initBoardFromCloud(Cell[][] board) {
        gameState.setValue(new GameState(board));
    }

    public void selectBuilding(String buildingType) {
        selectedBuildingType = buildingType;
    }

    public void placeBuilding(int row, int col) {
        GameState current = gameState.getValue();
        if (current != null) {
            Cell cell = current.getGrid()[row][col];
            if (!cell.isOccupied() && selectedBuildingType != null) {
                Log.d("debug", selectedBuildingType);
                cell.placeBuilding((Building)ModelObjectFactory.create(selectedBuildingType,
                        new Position(row, col)));
                Log.d("debug", String.valueOf(cell.getObject().getClass()));
                gameState.postValue(current);
            }
        }
    }

    public void updateGameState(long deltaTime) {
        accumulatedDayTime += deltaTime;
        GameState current = gameState.getValue();

        if (current != null) {

            if (accumulatedDayTime > NIGHT_THRESHOLD) {
                //night
                if (current.getTimeOfDay()) {
                    //init night and raid
                    startNight(current);
                }


                checkDeath(current);

                if (!getBuildingPositions(current.getGrid()).isEmpty()){
                    //raid is still in progress
                    List<Enemy> enemies = getEnemies(current);
                    updateTurrets(current, enemies, deltaTime);

                    updateEnemies(current, deltaTime);
                } else {
                    //raid ended with all buildings destroyed
                    Lose(current);
                }
                gameState.postValue(current); // Use postValue for background thread
            }
        }
    }

    private void checkDeath(GameState current ) {
        Cell[][] grid = current.getGrid();
        for (Cell[] cellRow : grid){
            for (Cell cell: cellRow){
                if (cell.getObject() != null){
                    ModelObject object = cell.getObject();
                    if (object.getHealth() <= 0){
                        cell.removeObject();
                    }
                }
            }
        }

    }

    private List<Turret> getTurrets(GameState current) {
        List<Turret> turrets = new ArrayList<>();
        Cell[][] grid = current.getGrid();
        for (Cell[] row : grid) {
            for (Cell cell : row) {
                ModelObject obj = cell.getObject();
                if (obj instanceof Turret) {
                    turrets.add((Turret) obj);
                }
            }
        }
        return turrets;
    }

    private void updateTurrets(GameState current, List<Enemy> enemies, long deltaTime) {
        List<Turret> turrets = getTurrets(current);
        for (Turret turret : turrets) {
            turret.update(enemies, deltaTime);
        }
    }
    private List<Enemy> getEnemies(GameState current) {
        List<Enemy> enemies = new ArrayList<>();
        Cell[][] grid = current.getGrid();
        for (Cell[] row : grid) {
            for (Cell cell : row) {
                ModelObject obj = cell.getObject();
                if (obj instanceof Enemy) {
                    enemies.add((Enemy) obj);
                }
            }
        }
        return enemies;
    }

    private void Lose(GameState current) {
        current.setGameStatus(GameStatus.LOST);
    }

    private void startNight(GameState current) {
        current.setTimeOfDay(false);

        //place holder amount
        int amount = 3;
        spawnEnemies(current, amount);
    }

    private void spawnEnemies(GameState current, int amount){
        for (int i = 0; i < amount; i++){
            spawnEnemy(current, "MONSTER");
        }
    }

    private void spawnEnemy(GameState current, String enemyType) {
        Cell cellToSpawnEnemyAt = getRandomFramePointIndex(current.getGrid());
        while (cellToSpawnEnemyAt.isOccupied()){
            cellToSpawnEnemyAt = getRandomFramePointIndex(current.getGrid());
        }
        Enemy enemy = (Enemy) ModelObjectFactory.create(enemyType,
                new Position(0,0));
        cellToSpawnEnemyAt.spawnEnemy(enemy);

        createPathForEnemy(current, enemy);
    }

    public Cell getRandomFramePointIndex(Cell[][] centerCells) {
        int rows = centerCells.length;
        int cols = centerCells[0].length;
        Random rand = new Random();

        // Total frame positions
        int leftColumnCount = rows - 2;
        int rightColumnCount = rows - 2;
        int totalFramePositions = cols + cols + leftColumnCount + rightColumnCount;

        // Generate random index from the frame positions
        int randChoice = rand.nextInt(totalFramePositions);

        if (randChoice < cols) {
            // First row
            return centerCells[0][randChoice];
        } else if (randChoice < cols + cols) {
            // Last row
            return centerCells[rows - 1][randChoice - cols];
        } else if (randChoice < cols + cols + leftColumnCount) {
            // First column (excluding first/last row)
            return centerCells[randChoice - cols - cols + 1][0];
        } else {
            // Last column (excluding first/last row)
            return centerCells[randChoice - cols - cols - leftColumnCount + 1][cols - 1];
        }
    }

    private void updateEnemies(GameState current, long deltaTime) {
        List<Enemy> enemies = new ArrayList<>();
        Cell[][] grid = current.getGrid();
        for (Cell[] row : grid) {
            for (Cell cell : row) {
                ModelObject modelObject = cell.getObject();
                if (modelObject instanceof Enemy) {
                    enemies.add((Enemy) modelObject);
                }
            }
        }
        for (Enemy enemy : enemies) {
            if (enemy.getEnemyState() != EnemyState.HURT && enemy.getEnemyState() != EnemyState.ATTACKING)
                updateEnemyMovement(enemy, current, deltaTime);
        }
    }

    private void updateEnemyMovement(Enemy enemy, GameState current, long deltaTime) {
        List<Position> path = createPathForEnemy(current, enemy);
        int targetIndex = enemy.getCurrentTargetIndex();

        if (path == null || path.isEmpty() || targetIndex >= path.size()){
            finishedPath(current, enemy, deltaTime);
            return;
        }

        enemy.updateDirection(enemy.getPosition(), path.get(0));
        enemy.accumulateTime(deltaTime);

        float timePerStep = 1000 / enemy.getMovementSpeed(); // ms per cell
        boolean hasMoved = false;

        while (enemy.getAccumulatedTime() >= timePerStep && targetIndex < path.size()) {
            hasMoved = true;
            Position nextPos = path.get(targetIndex);
            Cell nextCell = current.getCellAt(nextPos);
            if (nextCell.getObject() != null) {
                path = createPathForEnemy(current, enemy);
                nextPos = path.get(targetIndex);
                nextCell = current.getCellAt(nextPos);
            }
            Cell currentCell = current.getCellAt(enemy.getPosition());
            targetIndex = moveEnemy(currentCell, nextCell, enemy, timePerStep);
            enemy.setState(EnemyState.IDLE);
        }
        if (path.isEmpty() || targetIndex >= path.size()){
            fixDirectionToBuilding(enemy, current);
        }
        if (!hasMoved)
            enemy.setState(EnemyState.MOVING);
    }

    private void finishedPath(GameState current, Enemy enemy, long deltaTime) {
        // After movement logic, check for adjacent buildings and attack

        Cell currentEnemyCell = current.getCellAt(enemy.getPosition());
        List<Cell> neighbors = currentEnemyCell.getNeighbors(current);

        Set<Building> adjacentBuildings = new HashSet<>();

        for (Cell neighbor : neighbors) {
            ModelObject obj = neighbor.getObject();
            if (obj instanceof Building) {
                adjacentBuildings.add((Building) obj);
            }
        }

        if (!adjacentBuildings.isEmpty()) {
            enemy.accumulateAttackTime(deltaTime);

            for (Building building : adjacentBuildings) {
                if (enemy.canAttack()) {
                    // Attack immediately without delay
                    enemy.attack(building);
                }
            }
        }
    }

    private void fixDirectionToBuilding(Enemy enemy, GameState current) {
        Cell enemyCell = current.getCellAt(enemy.getPosition());
        List<Cell> buildingPotential = enemyCell.getNeighbors(current);
        for (Cell cell : buildingPotential) {
            if (cell.getObject() instanceof Building) {
                enemy.updateDirection(enemy.getPosition(), cell.getPosition());
            }
        }
    }

    private int moveEnemy(Cell currentCell, Cell nextCell, Enemy enemy, float timePerStep){
        currentCell.removeObject();
        Position prevPos = enemy.getPosition();
        nextCell.spawnEnemy(enemy);
        enemy.updateDirection(prevPos);
        enemy.incrementTargetIndex();
        enemy.decreaseAccumulatedTime(timePerStep);
        return enemy.getCurrentTargetIndex();

    }

    private List<Position> createPathForEnemy(GameState current, Enemy enemy) {
        Pathfinder pathfinder = new Pathfinder(current);
        Position closetBuilding = getClosetBuildingToEnemy(current, enemy, pathfinder);

        List<Position> path = pathfinder.findPath(enemy.getPosition(), closetBuilding);

        enemy.setPath(path);
        return enemy.getPath();
    }

    private Position getClosetBuildingToEnemy(GameState current, Enemy enemy,
                                              Pathfinder pathfinder) {

        List<Position> allBuildings = getBuildingPositions(current.getGrid());
        if (allBuildings.isEmpty()){
            return enemy.getPosition();
        }
        List<Position> buildingNeighbors = getNeighborPositions(allBuildings, current);

        Position closest = pathfinder.findClosestBuilding(enemy.getPosition(), buildingNeighbors);

        while (closest == null){
            buildingNeighbors = getNeighborPositions(buildingNeighbors, current);
            closest = pathfinder.findClosestBuilding(enemy.getPosition(), buildingNeighbors);
        }
        return closest;
    }

    private List<Position> getNeighborPositions(List<Position> positions, GameState current){
        List<Position> buildingPositions = new ArrayList<>();
        for (Position pos : positions) {
            List<Position> neighbors = pos.getNeighbors();
            for (Position neighbour : neighbors){
                if (current.isValidPosition(neighbour))
                    buildingPositions.add(neighbour);
            }
        }
        return buildingPositions;
    }

    private List<Position> getBuildingPositions(Cell[][] grid) {
        List<Position> buildingPositions = new ArrayList<>();
        for (Cell[] cells : grid) {
            for (Cell cell : cells) {
                if (cell.isOccupied() && cell.getObject() instanceof Building) {
                    buildingPositions.add(cell.getObject().getPosition());
                }
            }
        }
        return buildingPositions;
    }

    public LiveData<GameState> getGameState() {
        return gameState;
    }
}