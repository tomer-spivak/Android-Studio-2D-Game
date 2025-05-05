package tomer.spivak.androidstudio2dgame.viewModel;

import android.util.Log;
import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import tomer.spivak.androidstudio2dgame.model.Cell;
import tomer.spivak.androidstudio2dgame.model.GameState;
import tomer.spivak.androidstudio2dgame.model.Position;
import tomer.spivak.androidstudio2dgame.modelEnums.CellState;
import tomer.spivak.androidstudio2dgame.modelEnums.DifficultyLevel;
import tomer.spivak.androidstudio2dgame.model.ModelGameManager;
import tomer.spivak.androidstudio2dgame.modelEnums.Direction;
import tomer.spivak.androidstudio2dgame.modelEnums.EnemyState;
import tomer.spivak.androidstudio2dgame.modelObjects.Enemy;
import tomer.spivak.androidstudio2dgame.modelObjects.ModelObject;
import tomer.spivak.androidstudio2dgame.modelObjects.ModelObjectFactory;
import tomer.spivak.androidstudio2dgame.music.SoundEffectManager;

public class GameViewModel extends ViewModel {
    private final MutableLiveData<GameState> viewModelGameState = new MutableLiveData<>();
    private final ModelGameManager gameManager;

    public GameViewModel() {
        this.gameManager = new ModelGameManager();
    }

    public LiveData<GameState> getGameState() {
        return viewModelGameState;
    }

    public void selectBuilding(String type) {
        gameManager.setSelectedBuildingType(type);
    }

    public void onCellClicked(int row, int col) {
        gameManager.handleCellClick(row, col);
        gameManager.setSelectedBuildingType(null);
        viewModelGameState.postValue(gameManager.getState());
    }

    public void tick(long deltaTime) {
        gameManager.update(deltaTime);
        viewModelGameState.postValue(gameManager.getState());
    }

    public void skipToNextRound() {
        gameManager.skipToNextRound();
        viewModelGameState.postValue(gameManager.getState());
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

    public Cell[][] createBoard(Map<String, Object> data,
                                int boardSize,
                                DifficultyLevel difficultyLevel) {
        // 1) Initialize every cell with a default state (SPAWN on edges, NORMAL inside)
        Cell[][] board = new Cell[boardSize][boardSize];
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                CellState st = (i == 0 || j == 0 || i == boardSize - 1 || j == boardSize - 1)
                        ? CellState.SPAWN
                        : CellState.NORMAL;
                board[i][j] = new Cell(new Position(i, j), st);
            }
        }
        if (data == null || data.isEmpty()) {
            return board;
        }

        // 2) First pass: overlay every saved object and collect enemy→targetPos pairs
        List<Pair<Enemy, Position>> pendingTargets = new ArrayList<>();
        for (Object rowObj : data.values()) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rowList = (List<Map<String, Object>>) rowObj;
            for (Map<String, Object> col : rowList) {
                @SuppressWarnings("unchecked")
                Map<String, Object> posMap = (Map<String, Object>) col.get("position");
                int x = ((Number) Objects.requireNonNull(posMap.get("x"))).intValue();
                int y = ((Number) Objects.requireNonNull(posMap.get("y"))).intValue();
                if (x < 0 || y < 0 || x >= boardSize || y >= boardSize) continue;

                CellState cellState = CellState.valueOf((String) col.get("state"));
                @SuppressWarnings("unchecked")
                Map<String, Object> objectMap = (Map<String, Object>) col.get("object");

                if (objectMap != null && "monster".equals(objectMap.get("type"))) {
                    // Reconstruct the Enemy
                    Enemy e = (Enemy) ModelObjectFactory.create(
                            "monster", new Position(x, y), difficultyLevel);
                    e.setHealth(((Number) Objects.requireNonNull(objectMap.get("health"))).floatValue());
                    e.setState(EnemyState.valueOf(Objects.requireNonNull(objectMap.get("enemyState")).toString()));
                    e.setCurrentDirection(
                            Direction.valueOf(Objects.requireNonNull(objectMap.get("currentDirection")).toString()));
                    e.setCurrentTargetIndex(
                            ((Number) Objects.requireNonNull(objectMap.get("currentTargetIndex"))).intValue());
                    e.setTimeSinceLastAttack(
                            ((Number) Objects.requireNonNull(objectMap.get("timeSinceLastAttack"))).floatValue());
                    e.setTimeSinceLastMove(
                            ((Number) Objects.requireNonNull(objectMap.get("timeSinceLastMove"))).floatValue());
                    e.setAttackAnimationElapsedTime(
                            ((Number) Objects.requireNonNull(objectMap.get("attackAnimationElapsedTime"))).longValue());
                    e.setAttackAnimationRunning(
                            (Boolean) objectMap.get("attackAnimationRunning"));

                    Map<String, Object> tposMap = (Map<String, Object>) objectMap.get("targetCellPos");
                    if (tposMap != null) {
                        Position targetPos = new Position(((Number) Objects.requireNonNull(tposMap.get("x"))).intValue(),
                                ((Number) Objects.requireNonNull(tposMap.get("y"))).intValue());
                        pendingTargets.add(Pair.create(e, targetPos));
                    }
                    board[x][y] = new Cell(new Position(x, y), e, cellState);
                }
                else if (objectMap != null) {
                    // Any non‐enemy ModelObject
                    String type = (String) objectMap.get("type");
                    ModelObject obj = ModelObjectFactory.create(type, new Position(x, y), difficultyLevel);
                    obj.setHealth(((Number) Objects.requireNonNull(objectMap.get("health"))).floatValue());
                    board[x][y] = new Cell(new Position(x, y), obj, cellState);
                }
                // else leave the default EMPTY cell
            }
        }

        // 3) Trim out any fully‐null rows/columns
        Cell[][] trimmed = removeNullRowsAndColumns(board);

        // 4) Wire up every pending Enemy → its actual targetCell in the trimmed grid
        int trimmedRows = trimmed.length;
        int trimmedCols = trimmedRows > 0 ? trimmed[0].length : 0;
        for (Pair<Enemy, Position> pair : pendingTargets) {
            Enemy e = pair.first;
            Position p = pair.second;
            if (p.getX() >= 0 && p.getX() < trimmedRows
                    && p.getY() >= 0 && p.getY() < trimmedCols
                    && trimmed[p.getX()][p.getY()] != null) {
                Cell targetCell = trimmed[p.getX()][p.getY()];
                Log.d("cell", "target cell:" + targetCell.getObject());
                e.setTargetCell(targetCell);
            } else {
                Log.e("GameViewModel", "Invalid targetPos for enemy: " + p);
            }
        }

        // 5) Ensure the outer ring of trimmed[][] is all SPAWN cells
        for (int i = 0; i < trimmedRows; i++) {
            for (int j = 0; j < trimmedCols; j++) {
                if (i == 0 || j == 0 || i == trimmedRows - 1 || j == trimmedCols - 1) {
                    if (trimmed[i][j] == null) {
                        trimmed[i][j] = new Cell(new Position(i, j), CellState.SPAWN);
                    }
                }
            }
        }

        return trimmed;
    }


    public Cell[][] removeNullRowsAndColumns(Cell[][] array) {
        if (array == null || array.length == 0) return new Cell[0][0];

        int rows = array.length;
        int cols = array[0].length;

        boolean[] validRows = new boolean[rows];
        boolean[] validCols = new boolean[cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (array[i][j] != null) {
                    validRows[i] = true;
                    validCols[j] = true;
                }
            }
        }

        int validRowCount = 0;
        for (boolean row : validRows) if (row) validRowCount++;

        int validColCount = 0;
        for (boolean col : validCols) if (col) validColCount++;

        Cell[][] result = new Cell[validRowCount][validColCount];
        int newRow = 0;
        for (int i = 0; i < rows; i++) {
            if (validRows[i]) {
                int newCol = 0;
                for (int j = 0; j < cols; j++) {
                    if (validCols[j]) {
                        result[newRow][newCol++] = array[i][j];
                    }
                }
                newRow++;
            }
        }
        return result;
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