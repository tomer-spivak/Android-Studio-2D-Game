package tomer.spivak.androidstudio2dgame.gameActivity;


import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import tomer.spivak.androidstudio2dgame.intermediate.LeaderboardCallback;
import tomer.spivak.androidstudio2dgame.intermediate.LeaderboardEntry;
import tomer.spivak.androidstudio2dgame.model.Cell;
import tomer.spivak.androidstudio2dgame.modelEnums.DifficultyLevel;
import tomer.spivak.androidstudio2dgame.viewModel.GameViewModel;

public class FirebaseRepository {
    private final FirebaseFirestore db;
    private final FirebaseUser user;

    public FirebaseRepository(Context context){

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
                .addOnSuccessListener(onSuccess);
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
        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        handleLeaderboardData(documentSnapshot, round);
                    }
                });
    }

    private void handleLeaderboardData(DocumentSnapshot documentSnapshot, int round) {
        if (documentSnapshot != null && documentSnapshot.exists()) {
            Map<String, Object> leaderboard = (Map<String, Object>) documentSnapshot
                    .get("leaderboard");

            if (leaderboard != null) {
                Long maxRound = (Long) leaderboard.get("max round");
                if (maxRound == null)
                    saveRound(round);
                else if (round > maxRound) {
                    saveRound(round);
                }
            } else {
                saveRound(round);
            }
        } else {
            saveRound(round);
        }
    }

    private void saveRound(int round) {
        // Fetch all users (each document under "users" collection)
        Map<String, Object> leaderboard = new HashMap<>();
        leaderboard.put("max round", round);

        db.collection("users").document(user.getUid()).set(Collections
                .singletonMap("leaderboard", leaderboard), SetOptions
                .merge());

    }

    public void fetchLeaderboardFromDatabase(final LeaderboardCallback callback) {
        final List<LeaderboardEntry> maxRounds = new ArrayList<>();
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            // Assuming "leaderboard" is stored as a map inside each user document
                            Map<String, Object> leaderboard = (Map<String, Object>) document
                                    .get("leaderboard");
                            if (leaderboard != null && leaderboard.get("max round") != null) {
                                // Cast the value to a Number then convert to int
                                int maxRound = ((Number) Objects.requireNonNull(leaderboard.
                                        get("max round"))).intValue();
                                maxRounds.add(new LeaderboardEntry(document.getId(), maxRound));
                            }
                        }
                        maxRounds.sort(Collections.reverseOrder());
                        // Call the callback with the sorted list
                        callback.onLeaderboardFetched(maxRounds);
                    } else {
                        // Optionally handle errors
                        callback.onLeaderboardFetched(new ArrayList<>());
                    }
                });
    }
}
