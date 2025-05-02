package tomer.spivak.androidstudio2dgame.gameActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import tomer.spivak.androidstudio2dgame.model.Cell;
import tomer.spivak.androidstudio2dgame.model.Position;
import tomer.spivak.androidstudio2dgame.modelEnums.DifficultyLevel;
import tomer.spivak.androidstudio2dgame.modelEnums.Direction;
import tomer.spivak.androidstudio2dgame.modelEnums.EnemyState;
import tomer.spivak.androidstudio2dgame.modelObjects.Enemy;
import tomer.spivak.androidstudio2dgame.modelObjects.ModelObject;
import tomer.spivak.androidstudio2dgame.modelObjects.ModelObjectFactory;

public class BoardMapper {
    final int boardSize;
    Long timeSinceStartOfGame;
    DifficultyLevel difficulty;
    Cell[][] board;
    boolean isBoardEmpty = true;
    private int currentRound;
    private int shnuzes;

    public BoardMapper(int boardSize, DifficultyLevel difficulty) {
        this.boardSize = boardSize;
        this.difficulty = difficulty;
        this.board = new Cell[boardSize][boardSize];
    }

    public BoardMapper(int boardSize) {
        this.boardSize = boardSize;
        this.board = new Cell[boardSize][boardSize];
    }

    public void createBoard(Map<String, Object> data) {
        board = new Cell[boardSize][boardSize];

        for (Map.Entry<String, Object> entry : Objects.requireNonNull(data).entrySet()) {
            Object rowData = entry.getValue();
            List<Map<String, Object>> rowList = (List<Map<String, Object>>) rowData;

            for (Map<String, Object> col : rowList) {
                HashMap map = (HashMap) col.get("position");
                Position pos = new Position(((Long) Objects.requireNonNull(map.get("x"))).intValue(),
                        ((Long) Objects.requireNonNull(map.get("y"))).intValue());

                if (pos.getX() >= boardSize || pos.getY() >= boardSize) continue;

                HashMap objectMap = (HashMap) (col.get("object"));

                if (objectMap != null) {
                    ModelObject object = ModelObjectFactory.create((String) objectMap.get("type"), pos, difficulty);
                    String type = (String) objectMap.get("type");
                    object.setHealth(((Number) Objects.requireNonNull(objectMap.get("health"))).floatValue());

                    if (type.equals("monster")) {
                        Enemy enemy = (Enemy) ModelObjectFactory.create(type, pos, difficulty);

                        enemy.setState(EnemyState.valueOf(objectMap.get("enemyState").toString()));
                        enemy.setCurrentDirection(Direction.valueOf(objectMap.get("currentDirection").toString()));
                        enemy.setCurrentTargetIndex(((Number) objectMap.get("currentTargetIndex")).intValue());
                        enemy.setTimeSinceLastAttack(((Double) objectMap.get("timeSinceLastAttack")).floatValue());
                        enemy.setTimeSinceLastMove(((Double) objectMap.get("timeSinceLastMove")).floatValue());

                        board[pos.getX()][pos.getY()] = new Cell(pos, enemy);
                    } else {
                        board[pos.getX()][pos.getY()] = new Cell(pos, object);
                    }

                    isBoardEmpty = false;
                } else {
                    board[pos.getX()][pos.getY()] = new Cell(pos);
                }
            }
        }
        board = removeNullRowsAndColumns(board);
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

    public void initBoard() {
        Cell[][] board = new Cell[boardSize][boardSize];
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                board[i][j] = new Cell(new Position(i, j));
            }
        }
        this.board = board;
    }

    public Cell[][] getBoard() {
        return board;
    }

    public DifficultyLevel getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(DifficultyLevel difficulty) {
        this.difficulty = difficulty;
    }

    public Long getTimeSinceStartOfGame() {
        return timeSinceStartOfGame;
    }

    public void setTimeSinceStartOfGame(long time) {
        this.timeSinceStartOfGame = time;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(int currentRound) {
        this.currentRound = currentRound;
    }

    public int getShnuzes() {
        return shnuzes;
    }

    public void setShnuzes(int shnuzes) {
        this.shnuzes = shnuzes;
    }
}
