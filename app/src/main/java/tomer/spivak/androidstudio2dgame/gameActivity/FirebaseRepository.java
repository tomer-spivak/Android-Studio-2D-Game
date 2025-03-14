package tomer.spivak.androidstudio2dgame.gameActivity;


import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

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

import tomer.spivak.androidstudio2dgame.model.Cell;
import tomer.spivak.androidstudio2dgame.modelEnums.DifficultyLevel;
import tomer.spivak.androidstudio2dgame.viewModel.GameViewModel;

public class FirebaseRepository {
    private final FirebaseFirestore db;
    private final FirebaseUser user;
    //private AlertDialog dialog;
    //private final int boardSize;
    //private final GameViewModel viewModel;
    //private final GameView gameView;

    public FirebaseRepository(Context context){

        //this.viewModel = viewModel;
        //this.gameView = gameView;
        //this.dialog = null;
        //this.boardSize = boardSize;
        FirebaseApp.initializeApp(context);
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
    }



    public void saveBoard(Cell[][] board, String difficulty, Long gameTime,
                          OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
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
        db.collection("users")
                .document(user.getUid())
                .collection("board")
                .document("difficulty level")
                .set(new HashMap<String, Object>() {{
                    put("level", difficulty);
                }});
        db.collection("users")
                .document(user.getUid())
                .collection("board")
                .document("time of game")
                .set(new HashMap<String, Object>() {{
                    put("millis from start of the game", gameTime);
                }});
    }


    public void loadBoard(OnSuccessListener<DocumentSnapshot> onSuccess,
                          OnFailureListener onFailure) {
        if (user == null) {
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
    private void loadDifficultyLevel(OnSuccessListener<DocumentSnapshot> onSuccess) {
        if (user == null) {
            return;
        }

        db.collection("users")
                .document(user.getUid())
                .collection("board")
                .document("difficulty level")
                .get()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(null);
    }

    public void loadBoardFromDataBase(BoardMapper boardMapper,
                                      OnBoardLoadedListener listener) {
        //if difficulty is null, it means that its in the database,
        // and that we are currently in a game

        loadDifficultyLevel(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    String difficultyName = (String) documentSnapshot.get("level");
                    DifficultyLevel difficultyLevel = DifficultyLevel
                            .valueOf(difficultyName);
                    boardMapper.setDifficulty(difficultyLevel);
                    Log.d("debug", "data: " + documentSnapshot.getData());

                    loadTimeOfGame(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            Long timeOfGame =
                                    (Long) documentSnapshot.get("millis from start of the game");
                            boardMapper.setTimeSinceStartOfGame(timeOfGame);

                            loadBoard(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    createBoardInBoardMapper(boardMapper, documentSnapshot, listener);
                                }
                            }, null);
                        }
                    });


                }
            });
    }
    private void loadTimeOfGame(OnSuccessListener<DocumentSnapshot> onSuccess) {
        if (user == null) {
            return;
        }

        db.collection("users")
                .document(user.getUid())
                .collection("board")
                .document("time of game")
                .get()
                .addOnSuccessListener(onSuccess);
    }

    private void createBoardInBoardMapper(BoardMapper boardMapper,
                                          DocumentSnapshot documentSnapshot,
                                          OnBoardLoadedListener listener) {
        boardMapper.createBoard(documentSnapshot);
        Cell[][] board = boardMapper.getBoard();
        listener.onBoardLoaded(board);
    }


    public void checkIfTheresAGame(GameCheckCallback callback) {
        if (user == null) {
            callback.onCheckCompleted(false);
            return;
        }

        db.collection("users")
                .document(user.getUid())
                .collection("board")
                .document("board objects")
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        callback.onCheckCompleted(documentSnapshot.exists() &&
                                documentSnapshot.getData() != null);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onCheckCompleted(false);
                    }
                });
    }

    public void removeBoard() {
        if (user == null) return;
        db.collection("users")
                .document(user.getUid())
                .collection("board")
                .document("board objects")
                .delete();
        db.collection("users")
                .document(user.getUid())
                .collection("board")
                .document("difficulty level")
                .delete();
        db.collection("users")
                .document(user.getUid())
                .collection("board")
                .document("time of game")
                .delete();
    }

    public void logResults(GameViewModel viewModel) {
        if (user == null) return;

        int round = viewModel.getRound();
        db.collection("users")
                .document(user.getUid())
                .collection("board")
                .document("leaderboard")
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        if (documentSnapshot.exists() && documentSnapshot.getData() != null) {
                            if (documentSnapshot.get("max round") == null){
                                Log.d("debug", "roundd: " + round);
                                saveRound(round);
                            }
                            else {
                                Long maxRound = (Long) documentSnapshot.get("max round");
                                if (round > maxRound) {
                                    saveRound(round);
                                }
                            }
                        } else{
                            Log.d("debug", "roundd: " + round);
                            saveRound(round);

                        }
                    }

                    private void saveRound(int round) {
                        db.collection("users")
                                .document(user.getUid())
                                .collection("board")
                                .document("leaderboard")
                                .set(new HashMap<String, Object>() {{
                                    put("max round", round);
                                }});
                    }
                });
    }
}
