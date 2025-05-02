package tomer.spivak.androidstudio2dgame.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import tomer.spivak.androidstudio2dgame.modelEnums.EnemyState;
import tomer.spivak.androidstudio2dgame.modelEnums.GameStatus;
import tomer.spivak.androidstudio2dgame.modelObjects.Building;
import tomer.spivak.androidstudio2dgame.modelObjects.Enemy;
import tomer.spivak.androidstudio2dgame.modelObjects.ModelObject;
import tomer.spivak.androidstudio2dgame.modelObjects.ModelObjectFactory;
import tomer.spivak.androidstudio2dgame.music.SoundEffects;

public class EnemyManager {
    SoundEffects soundEffects;


    public void spawnEnemies(GameState gameState, int amount) {
        String enemyType = "MONSTER";
            for (int i = 0; i < amount; i++) {
                spawnEnemy(gameState, enemyType);
            }
    }

    private void spawnEnemy(GameState gameState, String enemyType) {
        Cell[][] grid = gameState.getGrid();
        int rows = grid.length;
        int cols = grid[0].length;
        int maxLevel = Math.min(rows, cols) / 2;  // number of concentric frames

        Random rnd = new Random();
        for (int level = 0; level < maxLevel; level++) {
            List<Cell> freeCells = new ArrayList<>();

            // collect border cells at this level
            int r2 = rows - 1 - level;
            int c2 = cols - 1 - level;

            // top & bottom edges
            for (int c = level; c <= c2; c++) {
                if (!grid[level][c].isOccupied()) freeCells.add(grid[level][c]);
                if (r2 != level && !grid[r2][c].isOccupied()) freeCells.add(grid[r2][c]);
            }
            // left & right edges (excluding corners already done)
            for (int r = level + 1; r <= r2 - 1; r++) {
                if (!grid[r][level].isOccupied()) freeCells.add(grid[r][level]);
                if (c2 != level && !grid[r][c2].isOccupied()) freeCells.add(grid[r][c2]);
            }

            if (!freeCells.isEmpty()) {
                Cell spawnCell = freeCells.get(rnd.nextInt(freeCells.size()));
                Enemy enemy = (Enemy) ModelObjectFactory.create(
                        enemyType, spawnCell.getPosition(),
                        gameState.getDifficulty()
                );
                enemy.setSoundEffects(soundEffects);
                spawnCell.spawnEnemy(enemy);
                createPathForEnemy(gameState, enemy);
                return;
            }
        }

        // if we get here, *all* frames were full
        gameState.setGameStatus(GameStatus.WON);
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
            enemy.update(deltaTime);
            if (enemy.getEnemyState() == EnemyState.IDLE) {
                updateEnemyMovement(enemy, current, deltaTime);
            }
        }

    }


    private void updateEnemyMovement(Enemy enemy, GameState current, long deltaTime) {
        List<Position> path = createPathForEnemy(current, enemy);
        int targetIndex = enemy.getCurrentTargetIndex();

        if (path == null || path.isEmpty() || targetIndex >= path.size()){
            attemptAttack(current, enemy, deltaTime);
            return;
        }

        enemy.updateDirection(enemy.getPosition(), path.get(0));
        enemy.accumulateTime(deltaTime);

        float timePerStep = 1000 / enemy.getMovementSpeed();

        while (enemy.getAccumulatedTime() >= timePerStep && targetIndex < path.size()) {
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
    }

    private void attemptAttack(GameState current, Enemy enemy, long deltaTime) {
        Building buildingToAttack = pivotToAdjacentBuildings(enemy, current);
        enemy.accumulateAttackTime(deltaTime);
        if (enemy.canAttack()) {
            enemy.attack(buildingToAttack);
        }
    }

    private Building pivotToAdjacentBuildings(Enemy enemy, GameState current) {
        Cell enemyCell = current.getCellAt(enemy.getPosition());
        List<Cell> neighbors = enemyCell.getNeighbors(current);

        Cell targetCell = null;
        float minHealth = Integer.MAX_VALUE;

        // find the building with least health
        for (Cell cell : neighbors) {
            if (cell.getObject() instanceof Building) {
                Building b = (Building) cell.getObject();
                if (b.getHealth() < minHealth) {
                    minHealth = b.getHealth();
                    targetCell = cell;
                }
            }
        }

        if (targetCell != null) {
            // turn toward that building
            enemy.updateDirection(enemy.getPosition(), targetCell.getPosition());
            return (Building) targetCell.getObject();
        }

        return null;
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
        List<Position> path = new Pathfinder(current).findPathToClosestBuildingNeighbor(enemy.getPosition());
        enemy.setPath(path);
        return path;
    }

    public void setSoundEffects(SoundEffects soundEffects) {
        this.soundEffects = soundEffects;
    }
}