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
import tomer.spivak.androidstudio2dgame.modelEnums.EnemyType;
import tomer.spivak.androidstudio2dgame.modelObjects.Enemy;
import tomer.spivak.androidstudio2dgame.modelObjects.ModelObject;
import tomer.spivak.androidstudio2dgame.modelObjects.ModelObjectFactory;

public class BoardMapper {
    final int boardSize;
    Long timeSinceStartOfGame;
    DifficultyLevel difficulty;
    Cell[][] board;
    boolean isBoardEmpty = true;

    public BoardMapper(int boardSize, DifficultyLevel difficulty) {
        this.boardSize = boardSize;

        this.difficulty = difficulty;
        board = new Cell[boardSize][boardSize];

    }

    public BoardMapper(int boardSize) {
        this.boardSize = boardSize;
        this.board = new Cell[boardSize][boardSize];
    }


    public void createBoard(Map<String, Object> data){

        board = new Cell[boardSize][boardSize];

            // Log each row and its contents in a more readable format
            for (Map.Entry<String, Object> entry : Objects.requireNonNull(data).entrySet()) {
                Object rowData = entry.getValue();
                List<Map<String, Object>> rowList = (List<Map<String, Object>>) rowData;

                for (Map<String, Object> col : rowList) {
                    HashMap map = (HashMap) col.get("position");
                    Position pos = new Position(((Long)(Objects.
                            requireNonNull(Objects.requireNonNull(map).get("x"))))
                            .intValue(), ((Long)(Objects.requireNonNull(map.get("y"))))
                            .intValue());
                    if (pos.getX() >= boardSize || pos.getY() >= boardSize)
                        continue;
                    HashMap objectMap = (HashMap)(col.get("object"));

                    if (objectMap != null){

                        ModelObject object = ModelObjectFactory.create((String)
                                objectMap.get("type"), pos, difficulty);
                        String type = (String) objectMap.get("type");
                        object.setHealth(((Number) Objects.requireNonNull(objectMap.
                                get("health"))).floatValue());
                        if (isInEnum(type, EnemyType.class)) {
                            Enemy enemy = (Enemy) ModelObjectFactory.create(type, pos, difficulty);

                            String stateString = Objects.requireNonNull(objectMap.get("enemyState"))
                                    .toString();
                            EnemyState state = EnemyState.valueOf(stateString);
                            enemy.setState(state);

                            String directionString = Objects.requireNonNull(objectMap.
                                    get("currentDirection")).toString();
                            Direction direction = Direction.valueOf(directionString);
                            enemy.setCurrentDirection(direction);


                            enemy.setCurrentTargetIndex(((Number) Objects.
                                    requireNonNull(objectMap.get("currentTargetIndex")))
                                    .intValue());

                            enemy.setTimeSinceLastAttack(((Double) Objects.requireNonNull(objectMap.
                                    get("timeSinceLastAttack"))).floatValue());

                            enemy.setTimeSinceLastMove(((Double) Objects.requireNonNull(objectMap.
                                    get("timeSinceLastMove"))).floatValue());




                        }
                        else {
                            isBoardEmpty = false;
                        }

                        board[pos.getX()][pos.getY()] = new Cell(pos, object);

                    }
                    else {
                        board[pos.getX()][pos.getY()] = new Cell(pos);
                    }
                }
            }
            board = removeNullRowsAndColumns(board);
    }

    public <T extends Enum<T>> boolean isInEnum(String value, Class<T> enumClass) {
        for (T enumValue : Objects.requireNonNull(enumClass.getEnumConstants())) {
            if (enumValue.name().equals(value)) {
                return true;
            }
        }
        return false;
    }

    public Cell[][] removeNullRowsAndColumns(Cell[][] array) {
        if (array == null || array.length == 0) return new Cell[0][0];

        int rows = array.length;
        int cols = array[0].length;

        // Track valid rows and columns
        boolean[] validRows = new boolean[rows];
        boolean[] validCols = new boolean[cols];

        // Identify valid rows and columns
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (array[i][j] != null) {
                    validRows[i] = true;
                    validCols[j] = true;
                }
            }
        }

        // Count valid rows and columns
        int validRowCount = 0;
        for (boolean row : validRows) if (row) validRowCount++;

        int validColCount = 0;
        for (boolean col : validCols) if (col) validColCount++;

        // Create the new array
        Cell[][] result = new Cell[validRowCount][validColCount];

        // Fill the new array
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

    public void setTimeSinceStartOfGame(long l) {
        this.timeSinceStartOfGame = l;
    }
}
