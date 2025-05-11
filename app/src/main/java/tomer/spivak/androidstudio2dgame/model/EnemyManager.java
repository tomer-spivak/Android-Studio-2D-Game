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
import tomer.spivak.androidstudio2dgame.music.SoundEffectManager;

public class EnemyManager {
    SoundEffectManager soundEffects;

    public void spawnEnemies(GameState gameState, int amount) {
        String enemyType = "monster";
            for (int i = 0; i < amount; i++) {
                spawnEnemy(gameState, enemyType);
            }
    }

    private void spawnEnemy(GameState gameState, String enemyType) {
        Cell[][] grid = gameState.getGrid();
        int rows = grid.length;
        int cols = grid[0].length;
        int maxLevel = Math.min(rows, cols) / 2;

        Random rnd = new Random();
        for (int level = 0; level < maxLevel; level++) {
            List<Cell> freeCells = new ArrayList<>();

            int r2 = rows - 1 - level;
            int c2 = cols - 1 - level;

            for (int c = level; c <= c2; c++) {
                if (!grid[level][c].isOccupied()) freeCells.add(grid[level][c]);
                if (r2 != level && !grid[r2][c].isOccupied()) freeCells.add(grid[r2][c]);
            }
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

        // if we get here, all cells were full
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


    private void attemptAttackIfAdjacent(GameState current, Enemy enemy, long deltaTime) {
        Cell targetCell = pivotToAdjacentBuildings(enemy, current);
        if (targetCell != null) {
            enemy.accumulateAttackTime(deltaTime);
            if (enemy.canAttack()) {
                enemy.attack(targetCell);
            }
        }
    }

    private void updateEnemyMovement(Enemy enemy, GameState current, long deltaTime) {
        List<Position> path = enemy.getPath();
        int targetIndex = enemy.getCurrentTargetIndex();

        boolean needNewPath = false;
        if (path == null || path.isEmpty()) {
            needNewPath = true;
        } else if (targetIndex >= path.size()) {
            needNewPath = true;
        } else {
            Position nextPos = path.get(targetIndex);
            if (current.getCellAt(nextPos).getObject() != null) {
                needNewPath = true;
            }
        }

        if (needNewPath) {
            path = createPathForEnemy(current, enemy);
            enemy.setCurrentTargetIndex(0);
            if (path == null || path.isEmpty()) {
                attemptAttackIfAdjacent(current, enemy, deltaTime);
                return;
            }
            targetIndex = 0;
        }

        Position nextPos = path.get(targetIndex);
        enemy.updateDirection(enemy.getPosition(), nextPos);
        enemy.accumulateTime(deltaTime);

        float timePerStep = 1000f / enemy.getMovementSpeed();
        if (enemy.getAccumulatedTime() >= timePerStep && targetIndex < path.size()) {
            Cell currentCell = current.getCellAt(enemy.getPosition());
            Cell nextCell    = current.getCellAt(path.get(targetIndex));

            if (nextCell.getObject() == null) {
                int newIndex = moveEnemy(currentCell, nextCell, enemy, timePerStep);
                enemy.setCurrentTargetIndex(newIndex);
            }
            enemy.setState(EnemyState.IDLE);
        }
    }

    private Cell pivotToAdjacentBuildings(Enemy enemy, GameState current) {
        Cell enemyCell = current.getCellAt(enemy.getPosition());
        List<Cell> neighbors = current.getNeighbors(enemyCell);

        Cell targetCell = null;
        float minHealth = Integer.MAX_VALUE;

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
            enemy.updateDirection(enemy.getPosition(), targetCell.getPosition());
            return targetCell;
        }

        return null;
    }


    private int moveEnemy(Cell currentCell, Cell nextCell, Enemy enemy, float timePerStep) {
        Position prevPos = enemy.getPosition();
        Position nextPos = nextCell.getPosition();

        currentCell.removeObject();
        enemy.setPosition(nextPos);
        nextCell.spawnEnemy(enemy);

        enemy.updateDirection(prevPos, nextPos);

        enemy.incrementTargetIndex();
        do {
            enemy.decreaseAccumulatedTime(timePerStep);
        } while (enemy.getAccumulatedTime() >= timePerStep);

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

    public void setSoundEffects(SoundEffectManager soundEffects) {
        this.soundEffects = soundEffects;
    }
}