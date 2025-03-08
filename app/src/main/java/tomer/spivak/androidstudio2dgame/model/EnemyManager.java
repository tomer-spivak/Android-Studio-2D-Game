package tomer.spivak.androidstudio2dgame.model;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import tomer.spivak.androidstudio2dgame.modelEnums.EnemyState;
import tomer.spivak.androidstudio2dgame.modelObjects.Building;
import tomer.spivak.androidstudio2dgame.modelObjects.Enemy;
import tomer.spivak.androidstudio2dgame.modelObjects.ModelObject;
import tomer.spivak.androidstudio2dgame.modelObjects.ModelObjectFactory;

public class EnemyManager {
    // File: EnemyManager.java


    public void spawnEnemies(GameState gameState, int amount) {
        String enemyType = "MONSTER";
            for (int i = 0; i < amount; i++) {
                spawnEnemy(gameState, enemyType);
            }
    }
    private void spawnEnemy(GameState gameState, String enemyType) {
            Cell cellToSpawn = getRandomFramePointIndex(gameState.getGrid());
            while(cellToSpawn.isOccupied()){
                cellToSpawn = getRandomFramePointIndex(gameState.getGrid());
            }
            Enemy enemy = (Enemy) ModelObjectFactory.create(enemyType, new Position(0, 0),
                    gameState.getDifficulty());
            cellToSpawn.spawnEnemy(enemy);
            createPathForEnemy(gameState, enemy);
    }
    public Cell getRandomFramePointIndex(Cell[][] centerCells) {
            int rows = centerCells.length;
            int cols = centerCells[0].length;
            Log.d("debug", String.valueOf(rows));
            Log.d("debug", String.valueOf(cols));
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


    public void updateEnemies(GameState current, long deltaTime) {
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
            if (enemy.getEnemyState() != EnemyState.HURT && enemy.getEnemyState() !=
                    EnemyState.ATTACKING1 && enemy.getEnemyState() != EnemyState.ATTACKING2 &&
                    enemy.getEnemyState() != EnemyState.ATTACKING3 && enemy.getEnemyState() != EnemyState.ATTACKING4)
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



    public List<Enemy> getEnemies(GameState gameState) {
            List<Enemy> enemies = new ArrayList<>();
            Cell[][] grid = gameState.getGrid();
            for (Cell[] row : grid) {
                for (Cell cell : row) {
                    if (cell.getObject() instanceof Enemy) {
                        enemies.add((Enemy) cell.getObject());
                    }
                }
            }
            return enemies;
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





}
