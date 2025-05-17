package tomer.spivak.androidstudio2dgame.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

import tomer.spivak.androidstudio2dgame.modelEnums.EnemyState;
import tomer.spivak.androidstudio2dgame.modelObjects.Building;
import tomer.spivak.androidstudio2dgame.modelObjects.Enemy;
import tomer.spivak.androidstudio2dgame.modelObjects.ModelObjectFactory;
import tomer.spivak.androidstudio2dgame.music.SoundEffectManager;

public class EnemyManager {
    private SoundEffectManager soundEffects;

    public void spawnEnemies(GameState gameState, int amount) {
        Cell[][] grid = gameState.getGrid();
        int boardSize = grid.length;
        List<Cell> freeFrameCells = new ArrayList<>();

        for (int i = 0; i < boardSize; i++) {
            freeFrameCells.add(grid[0][i]);
            freeFrameCells.add(grid[boardSize-1][i]);
        }
        for (int i = 1; i < boardSize-1; i++) {
            freeFrameCells.add(grid[i][0]);
            freeFrameCells.add(grid[i][boardSize-1]);
        }

        Random rnd = new Random();
        for (int i = 0; i < amount; i++) {
            Cell spawnCell = freeFrameCells.remove(rnd.nextInt(freeFrameCells.size()));
            Enemy enemy = (Enemy)ModelObjectFactory.create("monster", spawnCell.getPosition(), gameState.getDifficulty());
            enemy.setSoundEffects(soundEffects);
            spawnCell.spawnEnemy(enemy);
        }
    }

    public void updateEnemies(GameState gameState, long deltaTime) {
        List<Enemy> enemies = getEnemies(gameState);
        for (Enemy enemy : enemies) {
            enemy.update(deltaTime);
            if (enemy.getEnemyState() != EnemyState.IDLE) {
                continue;
            }
            List<Position> currentPath = enemy.getPath();
            int targetIndex = enemy.getCurrentTargetIndex();
            boolean needNewPath = false;
            if (currentPath == null || currentPath.isEmpty()) {
                needNewPath = true;
            } else if (targetIndex >= currentPath.size()) {
                needNewPath = true;
            } else {
                Position nextPos = currentPath.get(targetIndex);
                if (gameState.getCellAt(nextPos).isOccupied()) {
                    needNewPath = true;
                }
            }

            if (needNewPath) {
                List<Position> newPath = new ArrayList<>();
                Map<Position, Position> cameFrom = new HashMap<>();
                Queue<Position> frontier = new LinkedList<>();
                if (!hasAdjacentBuilding(gameState, enemy.getPosition())) {
                    for (Position enemyPositionNeighbor : enemy.getPosition().getNeighbors()) {
                        if (!gameState.isValidPosition(enemyPositionNeighbor) || gameState.getCellAt(enemyPositionNeighbor).isOccupied()) {
                            continue;
                        }
                        if (hasAdjacentBuilding(gameState, enemyPositionNeighbor)) {
                            newPath = new ArrayList<>();
                            newPath.add(enemyPositionNeighbor);
                            frontier.clear();
                            break;
                        }
                        cameFrom.put(enemyPositionNeighbor, enemy.getPosition());
                        frontier.add(enemyPositionNeighbor);
                    }

                    boolean found = false;
                    while (!frontier.isEmpty() && !found) {
                        Position currentPos = frontier.poll();
                        if (currentPos == null)
                            continue;
                        for (Position currentNeighbor : currentPos.getNeighbors()) {
                            if (!gameState.isValidPosition(currentNeighbor) || gameState.getCellAt(currentNeighbor).isOccupied() || cameFrom.containsKey(currentNeighbor)) {
                                continue;
                            }

                            if (hasAdjacentBuilding(gameState, currentNeighbor)) {
                                cameFrom.put(currentNeighbor, currentPos);
                                ArrayList<Position> reconstructPath = new ArrayList<>();
                                for (Position pathMapPositions = currentNeighbor; pathMapPositions != null; pathMapPositions = cameFrom.get(pathMapPositions)) {
                                    reconstructPath.add(0, pathMapPositions);
                                }
                                if (!reconstructPath.isEmpty() && reconstructPath.get(0).equals(enemy.getPosition())) {
                                    reconstructPath.remove(0);
                                }
                                newPath = reconstructPath;
                                found = true;
                                break;
                            }
                            cameFrom.put(currentNeighbor, currentPos);
                            frontier.add(currentNeighbor);
                        }
                    }
                }
                enemy.setPath(newPath);
                enemy.setCurrentTargetIndex(0);
                if (newPath.isEmpty()) {
                    //Cell enemyCell = gameState.getCellAt(enemy.getPosition());
                    //List<Cell> neighbors = gameState.getNeighbors(enemyCell);
                    List<Cell> neighborsCells = new ArrayList<>();
                    for (Position neighborsPos: enemy.getPosition().getNeighbors()){
                        if(gameState.isValidPosition(neighborsPos))
                            neighborsCells.add(gameState.getCellAt(neighborsPos));
                    }
                    Cell targetCell = null;
                    float minHealth = Integer.MAX_VALUE;

                    for (Cell neighborCell : neighborsCells) {
                        if (neighborCell.getObject() instanceof Building) {
                            Building b = (Building) neighborCell.getObject();
                            if (b.getHealth() < minHealth) {
                                minHealth = b.getHealth();
                                targetCell = neighborCell;
                            }
                        }
                    }

                    if (targetCell != null) {
                        enemy.updateDirection(enemy.getPosition(), targetCell.getPosition());
                        enemy.accumulateAttackTime(deltaTime);
                        if (enemy.canAttack()) {
                            enemy.attack(targetCell);
                        }
                    }
                    continue;
                }
                targetIndex = 0;
                currentPath = newPath;
            }

            Position nextPos = currentPath.get(targetIndex);
            enemy.updateDirection(enemy.getPosition(), nextPos);
            enemy.accumulateTime(deltaTime);

            float timePerStep = 1000f / enemy.getMovementSpeed();
            if (enemy.getAccumulatedTime() >= timePerStep && targetIndex < currentPath.size()) {
                Cell currentCell = gameState.getCellAt(enemy.getPosition());
                Cell nextCell = gameState.getCellAt(currentPath.get(targetIndex));

                if (!nextCell.isOccupied()) {
                    Position prevPos = enemy.getPosition();
                    nextPos = nextCell.getPosition();
                    currentCell.removeObject();
                    nextCell.spawnEnemy(enemy);
                    enemy.updateDirection(prevPos, nextPos);
                    enemy.incrementTargetIndex();
                    do {
                        enemy.decreaseAccumulatedTime(timePerStep);
                    } while (enemy.getAccumulatedTime() >= timePerStep);
                }
            }
        }
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

    public void setSoundEffects(SoundEffectManager soundEffects) {
        this.soundEffects = soundEffects;
    }

    private boolean hasAdjacentBuilding(GameState gameState, Position pos) {
        for (Position neighborPosition : pos.getNeighbors()) {
            if (!gameState.isValidPosition(neighborPosition))
                continue;
            Cell cell = gameState.getCellAt(neighborPosition);
            if (cell.getObject() instanceof Building) {
                return true;
            }
        }
        return false;
    }

}