package tomer.spivak.androidstudio2dgame;


import android.content.Context;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import tomer.spivak.androidstudio2dgame.gameManager.GameView;
import tomer.spivak.androidstudio2dgame.model.Cell;
import tomer.spivak.androidstudio2dgame.model.Position;
import tomer.spivak.androidstudio2dgame.modelEnums.Direction;
import tomer.spivak.androidstudio2dgame.modelEnums.EnemyState;
import tomer.spivak.androidstudio2dgame.modelEnums.EnemyType;
import tomer.spivak.androidstudio2dgame.modelObjects.Enemy;
import tomer.spivak.androidstudio2dgame.modelObjects.ModelObject;
import tomer.spivak.androidstudio2dgame.modelObjects.ModelObjectFactory;
import tomer.spivak.androidstudio2dgame.viewModel.GameViewModel;

public class FirebaseRepository {
    private final FirebaseFirestore db;
    private final FirebaseUser user;
    private final Context context;
    private AlertDialog dialog;
    private final int boardSize;
    private final GameViewModel viewModel;
    private final GameView gameView;

    public FirebaseRepository(Context context, int boardSize, GameViewModel viewModel, GameView gameView) {
        this.context = context;
        this.viewModel = viewModel;
        this.gameView = gameView;
        this.dialog = null;
        this.boardSize = boardSize;
        FirebaseApp.initializeApp(context);
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
    }



    public void saveBoard(Cell[][] board, OnSuccessListener<Void> onSuccess,
                          OnFailureListener onFailure) {
        if (user == null) return;

        Map<String, Object> boardData = new HashMap<>();
        for (int i = 0; i < board.length; i++) {
            List<Map<String, Object>> rowData = new ArrayList<>();
            for (int j = 0; j < board[i].length; j++) {
                rowData.add(board[i][j].toMap()); // Convert each Cell to a map
            }
            boardData.put("row_" + i, rowData);
        }

        db.collection("users")
                .document(user.getUid())
                .collection("board")
                .document("board objects")
                .set(boardData)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
        for (Map.Entry<String, Object> entry : boardData.entrySet()) {
            Log.d("debug", "board data: " + entry.getKey() + ": " + entry.getValue());
        }
    }


    public void loadBoard(OnSuccessListener<DocumentSnapshot> onSuccess,
                          OnFailureListener onFailure) {
        if (user == null) {
            Log.d("debug", "user is null");
            return;
        }

        db.collection("users")
                .document(user.getUid())
                .collection("board")
                .document("board objects")
                .get()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public void loadBoardFromDataBase(Button btnStartGame) {
        loadBoard(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Toast.makeText(context, "success", Toast.LENGTH_SHORT).show();
                Log.d("debug", "database exists");
                Cell[][] board;
                if (documentSnapshot.exists()) {
                    board = new Cell[boardSize][boardSize];
                    Map<String, Object> data = documentSnapshot.getData();

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
                            HashMap objectMap = (HashMap)(col.get("object"));

                            if (objectMap != null){
                                ModelObject object = ModelObjectFactory.create((String)
                                        objectMap.get("type"), pos);
                                String type = (String) objectMap.get("type");
                                object.setHealth(((Number) Objects.requireNonNull(objectMap.
                                        get("health"))).floatValue());
                                if (isInEnum(type, EnemyType.class)) {
                                    Enemy enemy = (Enemy) object;
                                    enemy.setState((EnemyState) objectMap.get("enemyState"));
                                    enemy.setCurrentDirection((Direction) objectMap.
                                            get("currentDirection"));
                                    enemy.setPath((List<Position>) objectMap.get("path"));
                                    enemy.setCurrentTargetIndex(((Number) Objects.
                                            requireNonNull(objectMap.get("currentTargetIndex")))
                                            .intValue());
                                    enemy.setTimeSinceLastAttack((Float) objectMap.
                                            get("timeSinceLastAttack"));
                                    enemy.setTimeSinceLastMove((Float) objectMap.
                                            get("timeSinceLastMove"));

                                }
                                else{
                                    btnStartGame.setEnabled(true);
                                }

                                board[pos.getX()][pos.getY()] = new Cell(pos, object);

                            } else {
                                board[pos.getX()][pos.getY()] = new Cell(pos);
                            }
                        }
                    }
                    board = removeNullRowsAndColumns(board);
                }
                else {
                    board = initBoard();
                }
                initBoardInViewModel(board, dialog);
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                initBoardInViewModel(initBoard(), dialog);
                Toast.makeText(context, "failed to fetch board from database", Toast.LENGTH_SHORT).show();
                Log.d("debug", Objects.requireNonNull(e.getMessage()));
            }
        });
    }

    private void initBoardInViewModel(Cell[][] board, AlertDialog dialog) {
        gameView.setBoard(board);
        viewModel.initBoardFromCloud(gameView.getBoard());
        dialog.dismiss();
    }

    private Cell[][] initBoard() {
        Cell[][] board = new Cell[boardSize][boardSize];
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                board[i][j] = new Cell(new Position(i, j));
            }
        }
        return board;
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

    public void setDialog(AlertDialog alertDialog) {
        this.dialog = alertDialog;
    }
}
