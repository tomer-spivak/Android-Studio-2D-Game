package tomer.spivak.androidstudio2dgame.viewModel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.HashMap;
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

    public Cell[][] createBoard(Map<String, Object> data, int boardSize, DifficultyLevel difficultyLevel) {
        Cell[][] board = new Cell[boardSize][boardSize];

        if (data == null){
            for (int i = 0; i < boardSize; i++) {
                for (int j = 0; j < boardSize; j++) {
                    CellState state = (i == 0 || i == boardSize - 1 || j == 0 || j == boardSize - 1)
                            ? CellState.SPAWN : CellState.NORMAL;

                    board[i][j] = new Cell(new Position(i, j), state);

                }
            }

            return board;
        }
        for (Map.Entry<String, Object> entry : Objects.requireNonNull(data).entrySet()) {
            Object rowData = entry.getValue();
            List<Map<String, Object>> rowList = (List<Map<String, Object>>) rowData;

            for (Map<String, Object> col : rowList) {
                HashMap map = (HashMap) col.get("position");
                Position pos = new Position(((Long) Objects.requireNonNull(map.get("x"))).intValue(),
                        ((Long) Objects.requireNonNull(map.get("y"))).intValue());

                if (pos.getX() >= boardSize || pos.getY() >= boardSize) continue;

                HashMap objectMap = (HashMap) (col.get("object"));

                String cellStateName = (String) col.get("state");


                CellState cellState = CellState.valueOf(cellStateName);

                if (objectMap != null) {
                    ModelObject object = ModelObjectFactory.create((String) objectMap.get("type"), pos, difficultyLevel);
                    String type = (String) objectMap.get("type");
                    object.setHealth(((Number) Objects.requireNonNull(objectMap.get("health"))).floatValue());

                    if (type.equals("monster")) {
                        Enemy enemy = (Enemy) ModelObjectFactory.create(type, pos, difficultyLevel);

                        enemy.setState(EnemyState.valueOf(objectMap.get("enemyState").toString()));
                        enemy.setCurrentDirection(Direction.valueOf(objectMap.get("currentDirection").toString()));
                        enemy.setCurrentTargetIndex(((Number) objectMap.get("currentTargetIndex")).intValue());
                        enemy.setTimeSinceLastAttack(((Double) objectMap.get("timeSinceLastAttack")).floatValue());
                        enemy.setTimeSinceLastMove(((Double) objectMap.get("timeSinceLastMove")).floatValue());

                        board[pos.getX()][pos.getY()] = new Cell(pos, enemy, cellState);
                    } else {
                        board[pos.getX()][pos.getY()] = new Cell(pos, object, cellState);
                    }

                } else {
                    board[pos.getX()][pos.getY()] = new Cell(pos, cellState);
                }
            }
        }
        Cell[][] correctdBoard = removeNullRowsAndColumns(board);
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (i == 0 || i == boardSize - 1 || j == 0 || j == boardSize - 1) {
                    if (correctdBoard[i][j] == null) {
                        correctdBoard[i][j] = new Cell(new Position(i, j), CellState.SPAWN);
                    }
                }
            }
        }
        return correctdBoard;
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