package tomer.spivak.androidstudio2dgame.projectManagement;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tomer.spivak.androidstudio2dgame.logic.Cell;
import tomer.spivak.androidstudio2dgame.logic.GameState;
import tomer.spivak.androidstudio2dgame.logic.Position;
import tomer.spivak.androidstudio2dgame.logic.modelEnums.BuildingState;
import tomer.spivak.androidstudio2dgame.logic.modelEnums.CellState;
import tomer.spivak.androidstudio2dgame.logic.modelEnums.DifficultyLevel;
import tomer.spivak.androidstudio2dgame.logic.ModelGameManager;
import tomer.spivak.androidstudio2dgame.logic.modelEnums.Direction;
import tomer.spivak.androidstudio2dgame.logic.modelEnums.EnemyState;
import tomer.spivak.androidstudio2dgame.logic.Building;
import tomer.spivak.androidstudio2dgame.logic.Enemy;
import tomer.spivak.androidstudio2dgame.logic.ModelObject;
import tomer.spivak.androidstudio2dgame.logic.ModelObjectFactory;

public class GameViewModel extends ViewModel {
    private final MutableLiveData<GameState> viewModelGameState = new MutableLiveData<>();

    private final MutableLiveData<List<GameObjectData>> changedDelta  = new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<List<Position>> removedDelta  = new MutableLiveData<>(new ArrayList<>());

    private List<GameObjectData> lastList = new ArrayList<>();

    private final ModelGameManager gameManager;

    private final MutableLiveData<Integer> enemiesDefeatedDelta = new MutableLiveData<>();

    private int lastEnemiesDefeated = 0;

    public GameViewModel() {
        this.gameManager = new ModelGameManager();
    }

    private void publishGameState() {
        GameState gameState = gameManager.getState();

        viewModelGameState.postValue(gameState);

        ArrayList<GameObjectData> changed = new ArrayList<>();
        ArrayList<Position> removed = new ArrayList<>();

        ArrayList<GameObjectData> newList = new ArrayList<>();
        for (int i = 0; i < gameState.getGrid().length; i++) {
            for (int j = 0; j < gameState.getGrid()[i].length; j++) {
                Cell cell = gameState.getGrid()[i][j];
                if (cell.isOccupied()){
                    ModelObject modelObject = cell.getObject();
                    String type = modelObject.getType();
                    if ("mainbuilding".equals(type)) {
                        Position origin = modelObject.getPosition();
                        if (origin.getX() != i || origin.getY() != j) {
                            continue;
                        }
                    }
                    String state = "";
                    String direction = "";
                    if (modelObject instanceof Enemy) {
                        Enemy enemy = (Enemy) modelObject;
                        state = enemy.getEnemyState().name().toLowerCase();
                        direction = enemy.getCurrentDirection().name().toLowerCase();
                    }
                    else if (modelObject instanceof Building) {
                        state = ((Building) modelObject).getState().name().toLowerCase();
                    }
                    newList.add(new GameObjectData(type, i, j, state, direction, modelObject.getHealth() / modelObject.getMaxHealth()));
                }
            }
        }

        for (GameObjectData newGameObjectData : newList) {
            boolean found = false;
            for (GameObjectData oldGameObjectData : lastList) {
                if (oldGameObjectData.getX() == newGameObjectData.getX() && oldGameObjectData.getY() == newGameObjectData.getY()) {
                    found = true;
                    if (!oldGameObjectData.getState().equals(newGameObjectData.getState()) ||
                            !oldGameObjectData.getDirection().equals(newGameObjectData.getDirection()) ||
                            oldGameObjectData.getHealthPercentage() != newGameObjectData.getHealthPercentage()) {
                        changed.add(newGameObjectData);
                    }
                    break;
                }
            }
            if (!found) {
                changed.add(newGameObjectData);
            }
        }

        for (GameObjectData oldGameObjectData : lastList) {
            boolean stillThere = false;
            for (GameObjectData newGameObjectData : newList) {
                if (oldGameObjectData.getX() == newGameObjectData.getX() && oldGameObjectData.getY() == newGameObjectData.getY()) {
                    stillThere = true;
                    break;
                }
            }
            if (!stillThere) {
                removed.add(new Position(oldGameObjectData.getX(), oldGameObjectData.getY()));
            }
        }
        if (!changed.isEmpty())
            changedDelta.postValue(changed);
        if (!removed.isEmpty()) {
            removedDelta.postValue(removed);
        }
        lastList = newList;

        if (gameState.getEnemiesDefeated() - lastEnemiesDefeated > 0) {
            enemiesDefeatedDelta.postValue(gameState.getEnemiesDefeated() - lastEnemiesDefeated);
            lastEnemiesDefeated = gameState.getEnemiesDefeated();
        }
    }

    public void onCellClicked(int row, int col) {
        gameManager.handleCellClick(row, col);
        publishGameState();
    }

    public void tick(long deltaTime) {
        gameManager.update(deltaTime);
        publishGameState();
    }

    public void skipToNextRound() {
        gameManager.skipToNextRound();
        publishGameState();
    }

    public boolean canStartGame() {
        return gameManager.canStartGame();
    }

    public void initModelBoardWithDataFromDataBase(SoundEffectManager soundEffectsManager, Map<String, Object> data, int boardSize,
                                                   DifficultyLevel difficultyLevel, int currentRound, int shnuzes, Long timeSinceGameStart, boolean dayTime){
        Cell[][] board = new Cell[boardSize][boardSize];
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                CellState defaultState;
                if (i == 0 || j == 0 || i == boardSize - 1 || j == boardSize - 1) {
                    defaultState = CellState.SPAWN;
                } else {
                    defaultState = CellState.NORMAL;
                }
                board[i][j] = new Cell(new Position(i, j), defaultState);
            }
        }

        if (data != null && !data.isEmpty()) {
            List<Enemy> attackingEnemies = new ArrayList<>();
            List<Position> attackedPositions = new ArrayList<>();

            for (Object row : data.values()) {
                List<Map<String, Object>> rowList = (List<Map<String, Object>>) row;
                for (Map<String, Object> cellMap : rowList) {
                    Map<String, Object> posMap = (Map<String, Object>) cellMap.get("position");
                    if (posMap == null)
                        continue;
                    int x = ((Number) (posMap.get("x"))).intValue();
                    int y = ((Number) (posMap.get("y"))).intValue();
                    if (x < 0 || y < 0 || x >= boardSize || y >= boardSize)
                        continue;

                    CellState state = CellState.valueOf((String) cellMap.get("state"));
                    Cell cell = board[x][y];
                    cell.setState(state);

                    Map<String, Object> modelObjectMap = (Map<String, Object>) cellMap.get("object");
                    if (modelObjectMap != null) {
                        String type = (String) modelObjectMap.get("type");
                        ModelObject modelObject = ModelObjectFactory.create(type, new Position(x, y), difficultyLevel);
                        modelObject.setHealth(((Double)modelObjectMap.get("health")).floatValue());
                        if ("monster".equals(type)) {
                            Enemy enemy = (Enemy) modelObject;
                            enemy.setState(EnemyState.valueOf((String) modelObjectMap.get("state")));
                            enemy.setCurrentDirection(Direction.valueOf((String) modelObjectMap.get("currentDirection")));
                            enemy.setAttackAnimationRunning((Boolean) modelObjectMap.get("attackAnimationRunning"));
                            enemy.setAttackAnimationElapsedTime((Long) modelObjectMap.get("attackAnimationElapsedTime"));
                            Map<String, Object> tposMap = (Map<String, Object>) modelObjectMap.get("targetCellPos");
                            if (tposMap != null) {
                                Position targetPos = new Position(((Number) (tposMap.get("x"))).intValue(), ((Number) (tposMap.get("y"))).intValue());
                                attackingEnemies.add(enemy);
                                attackedPositions.add(targetPos);
                            }
                            enemy.setTimeSinceLastAttack((long) modelObjectMap.get("timeSinceLastAttack"));
                            enemy.setTimeSinceLastMove((float) (double) modelObjectMap.get("timeSinceLastMove"));
                            enemy.setInTookDamageAnimation((Boolean) modelObjectMap.get("inTookDamageAnimation"));
                            enemy.setTimeSinceTookDamage((Long) modelObjectMap.get("timeSinceTookDamage"));
                            enemy.setStateBeforeHurt(EnemyState.valueOf((String) modelObjectMap.get("stateBeforeHurt")));
                            cell.spawnEnemy(enemy);
                        } else {
                            Building building = (Building) modelObject;
                            building.setState(BuildingState.valueOf((String) modelObjectMap.get("state")));
                            building.setAnimationTime((Long) modelObjectMap.get("timeSinceTookDamage"));
                            building.setInAnimation((Boolean) modelObjectMap.get("inAnimation"));
                            cell.placeBuilding(building);
                        }
                    }
                }
            }

            for (int i = 0; i < attackingEnemies.size(); i++) {
                Enemy enemy = attackingEnemies.get(i);
                Position position = attackedPositions.get(i);
                if (board[position.getX()][position.getY()] != null) {
                    Cell targetCell = board[position.getX()][position.getY()];
                    enemy.setTargetCell(targetCell);
                }
            }
        }
        gameManager.init(board, difficultyLevel);
        viewModelGameState.setValue(gameManager.getState());
        gameManager.setCurrentRound(currentRound);
        if (shnuzes > 0)
            gameManager.setShnuzes(shnuzes);
        setSoundEffects(soundEffectsManager);
        tick(timeSinceGameStart);
        setDayTime(dayTime);
    }

    public void selectBuilding(String type) {
        gameManager.setSelectedBuildingType(type);
    }

    public LiveData<GameState> getGameState() {
        return viewModelGameState;
    }

    public MutableLiveData<List<GameObjectData>> getChangedDelta() {
        return changedDelta;
    }

    public MutableLiveData<List<Position>> getRemovedDelta() {
        return removedDelta;
    }

    public void setSoundEffects(SoundEffectManager effects) {
        gameManager.setSoundEffects(effects);
    }

    public int getRound() {
        return gameManager.getRound();
    }

    public void setDayTime(boolean dayTime) {
        gameManager.setDayTime(dayTime);
    }

    public LiveData<Integer> getEnemiesDefeatedDelta() {
        return enemiesDefeatedDelta;
    }
}