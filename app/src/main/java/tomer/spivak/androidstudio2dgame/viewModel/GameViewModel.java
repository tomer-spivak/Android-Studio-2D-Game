package tomer.spivak.androidstudio2dgame.viewModel;

import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import tomer.spivak.androidstudio2dgame.GameObjectData;
import tomer.spivak.androidstudio2dgame.model.Cell;
import tomer.spivak.androidstudio2dgame.model.GameState;
import tomer.spivak.androidstudio2dgame.model.Position;
import tomer.spivak.androidstudio2dgame.modelEnums.CellState;
import tomer.spivak.androidstudio2dgame.modelEnums.DifficultyLevel;
import tomer.spivak.androidstudio2dgame.model.ModelGameManager;
import tomer.spivak.androidstudio2dgame.modelObjects.Building;
import tomer.spivak.androidstudio2dgame.modelObjects.Enemy;
import tomer.spivak.androidstudio2dgame.modelObjects.ModelObject;
import tomer.spivak.androidstudio2dgame.modelObjects.ModelObjectFactory;
import tomer.spivak.androidstudio2dgame.music.SoundEffectManager;

public class GameViewModel extends ViewModel {
    private final MutableLiveData<GameState> viewModelGameState = new MutableLiveData<>();

    private final MutableLiveData<Pair<List<GameObjectData>,List<Position>>> delta = new MutableLiveData<>(Pair.create(new ArrayList<>(), new ArrayList<>()));

    private List<GameObjectData> lastList = new ArrayList<>();

    private final ModelGameManager gameManager;

    public GameViewModel() {
        this.gameManager = new ModelGameManager();
    }

    public LiveData<GameState> getGameState() {
        return viewModelGameState;
    }

    public LiveData<Pair<List<GameObjectData>,List<Position>>> getDelta() {
        return delta;
    }

    private void refresh() {
        List<GameObjectData> newList = gridToGameObjectData(gameManager.getState().getGrid());
        List<GameObjectData> changed = new ArrayList<>();
        List<Position> removed = new ArrayList<>();

        // 1) find additions or updates
        for (GameObjectData newGameObjectData : newList) {
            boolean found = false;
            for (GameObjectData oldGameObjectData : lastList) {
                if (oldGameObjectData.getX() == newGameObjectData.getX() && oldGameObjectData.getY() == newGameObjectData.getY()) {
                    found = true;
                    if (!oldGameObjectData.getState().equals(newGameObjectData.getState()) || !oldGameObjectData.getDirection()
                            .equals(newGameObjectData.getDirection())) {
                        changed.add(newGameObjectData);
                    }
                    break;
                }
            }
            if (!found) {
                changed.add(newGameObjectData);
            }
        }

        // 2) find removals
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

        if (!changed.isEmpty() || !removed.isEmpty()) {
            delta.postValue(Pair.create(changed, removed));
        }
        lastList = newList;
    }

    private List<GameObjectData> gridToGameObjectData(Cell[][] grid) {
        List<GameObjectData> list = new ArrayList<>();

        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                Cell cell = grid[i][j];
                if (!cell.isOccupied()) continue;

                ModelObject obj = cell.getObject();
                String type = obj.getType().toLowerCase();

                if ("mainbuilding".equals(type)) {
                    Position origin = obj.getPosition();
                    if (origin.getX() != i || origin.getY() != j) {
                        // skip the other three tiles of the main building
                        continue;
                    }
                }
                String state = "";
                String direction = "";

                if (obj instanceof Enemy) {
                    Enemy e = (Enemy) obj;
                    state = e.getEnemyState().name().toLowerCase();
                    direction = e.getCurrentDirection().name().toLowerCase();
                }
                else if (obj instanceof Building) {
                    state = ((Building) obj).getState().name().toLowerCase();
                }

                list.add(new GameObjectData(type, i, j, state, direction, obj.getHealth()/ obj.getMaxHealth()));
            }
        }

        return list;
    }


    public void selectBuilding(String type) {
        gameManager.setSelectedBuildingType(type);
    }

    public void onCellClicked(int row, int col) {
        gameManager.handleCellClick(row, col);
        gameManager.setSelectedBuildingType(null);
        viewModelGameState.postValue(gameManager.getState());
        refresh();
    }

    public void tick(long deltaTime) {
        gameManager.update(deltaTime);
        viewModelGameState.postValue(gameManager.getState());
        refresh();
    }

    public void skipToNextRound() {
        gameManager.skipToNextRound();
        viewModelGameState.postValue(gameManager.getState());
        refresh();
    }

    public boolean canStartGame() {
        return gameManager.canStartGame();
    }

    public void initModelBoardWithDataFromDataBase(SoundEffectManager soundEffectsManager, Map<String, Object> data, int boardSize,
                                                   DifficultyLevel difficultyLevel, int currentRound, int shnuzes, Long timeSinceGameStart, boolean dayTime){
        Cell[][] board = createBoard(data, boardSize, difficultyLevel);
        gameManager.init(board, difficultyLevel);
        viewModelGameState.setValue(gameManager.getState());
        gameManager.setCurrentRound(currentRound);
        if (shnuzes < 0)
            gameManager.initShnuzes();
        else
            gameManager.setShnuzes(shnuzes);
        setSoundEffects(soundEffectsManager);
        tick(timeSinceGameStart);
        setDayTime(dayTime);
    }

    public Cell[][] createBoard(Map<String,Object> data,
                                int boardSize,
                                DifficultyLevel difficultyLevel) {
        // 1) initialize every Cell once with its defaultState
        Cell[][] board = new Cell[boardSize][boardSize];
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                CellState defaultSt = (i==0||j==0||i==boardSize-1||j==boardSize-1)
                        ? CellState.SPAWN
                        : CellState.NORMAL;
                board[i][j] = new Cell(new Position(i, j), defaultSt);
            }
        }

        if (data == null || data.isEmpty()) {
            return board;
        }

        // 2) overlay saved objects & states on the existing Cells
        for (Object rowObj : data.values()) {
            List<Map<String, Object>> rowList = (List<Map<String, Object>>) rowObj;
            for (Map<String, Object> col : rowList) {
                Map<String, Object> posMap = (Map<String, Object>) col.get("position");
                if (posMap == null)
                    continue;
                int x = ((Number) Objects.requireNonNull(posMap.get("x"))).intValue();
                int y = ((Number) Objects.requireNonNull(posMap.get("y"))).intValue();
                if (x<0||y<0||x>=boardSize||y>=boardSize) continue;

                // pull out the saved CellState
                CellState savedState = CellState.valueOf((String) col.get("state"));

                // get your existing Cell (with its defaultState still intact)
                Cell cell = board[x][y];

                // overwrite its “current” state to your saved one
                cell.setState(savedState);

                @SuppressWarnings("unchecked")
                Map<String, Object> objectMap = (Map<String, Object>) col.get("object");
                if (objectMap != null && "monster".equals(objectMap.get("type"))) {
                    // reconstruct your Enemy
                    Enemy e = (Enemy) ModelObjectFactory.create("monster",
                            new Position(x, y), difficultyLevel);
                    // …populate e’s health / direction / etc…
                    cell.spawnEnemy(e);

                } else if (objectMap != null) {
                    // any non‐enemy building/turret
                    String type = (String) objectMap.get("type");
                    ModelObject obj = ModelObjectFactory.create(type,
                            new Position(x, y), difficultyLevel);
                    // …populate obj’s health / state…
                    cell.placeBuilding((Building) obj);
                }
                // else leave it “empty” (object==null) but with your savedState
            }
        }

        // …any trimming or other post‐processing…
        return board;
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
}