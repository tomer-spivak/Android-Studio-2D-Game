package tomer.spivak.androidstudio2dgame.gameActivity;


import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import tomer.spivak.androidstudio2dgame.R;
import tomer.spivak.androidstudio2dgame.home.EmailSender;
import tomer.spivak.androidstudio2dgame.intermediate.LeaderboardCallback;
import tomer.spivak.androidstudio2dgame.intermediate.LeaderboardEntry;
import tomer.spivak.androidstudio2dgame.model.Cell;
import tomer.spivak.androidstudio2dgame.modelEnums.DifficultyLevel;
import tomer.spivak.androidstudio2dgame.viewModel.GameViewModel;

public class DatabaseRepository {
    private final FirebaseFirestore db;
    private final FirebaseUser user;
    AuthenticationHelper authHelper;

    public DatabaseRepository(Context context){

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
        if (documentSnapshot == null || !documentSnapshot.exists()){
            boardMapper.initBoard();
            return;
        }
        Map<String, Object> data = documentSnapshot.getData();
        boardMapper.createBoard(data);
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
                            Map<String, Object> leaderboard = (Map<String, Object>)
                                    document.get("leaderboard");
                            if (leaderboard != null && leaderboard.get("max round") != null) {
                                // Cast the value to a Number then convert to int
                                int maxRound = ((Number) Objects.
                                        requireNonNull(leaderboard.get("max round"))).intValue();
                                // Now that each document has been updated, we can safely get the displayName
                                String displayName = document.getString("displayName");
                                maxRounds.add(new LeaderboardEntry(maxRound, displayName));
                            }
                        }
                        maxRounds.sort(Collections.reverseOrder());
                        callback.onLeaderboardFetched(maxRounds);
                    } else {
                        // Optionally handle errors
                        callback.onLeaderboardFetched(new ArrayList<>());
                    }
                });
    }


    public String getDisplayName() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return "guest";
        }
        return currentUser.getDisplayName();
    }



    public void handleGoogleSignInResult(Intent data, GoogleSignInCallback googleSignInCallback) {
        if (authHelper == null)
            authHelper = new AuthenticationHelper();
        authHelper.handleGoogleSignInResult(data, googleSignInCallback);
    }

    public Intent getGoogleSignInIntent(Context context) {
        if (authHelper == null)
            authHelper = new AuthenticationHelper();
        return authHelper.getGoogleSignInIntent(context);
    }

    public void signUpWithEmailPassword(String string, String string1, String username, OnSuccessListener onSuccessListener,
                                        OnFailureListener onFailureListener) {
        if (authHelper == null)
            authHelper = new AuthenticationHelper();
        authHelper.signUpWithEmailPassword(string, string1, username, onSuccessListener, onFailureListener);
    }

    public void loginWithEmailAndPassword(String string, String string1, OnSuccessListener onSuccessListener,
                                          OnFailureListener onFailureListener) {
        if (authHelper == null)
            authHelper = new AuthenticationHelper();
        authHelper.loginWithEmailAndPassword(string, string1, onSuccessListener, onFailureListener);
    }

    public void forgotPassword(String email, OnSuccessListener onSuccessListener, OnFailureListener onFailureListener) {
        if (authHelper == null)
            authHelper = new AuthenticationHelper();
        authHelper.forgotPassword(email, onSuccessListener, onFailureListener);
    }

    public interface GoogleSignInCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    private static class AuthenticationHelper {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        public void loginWithEmailAndPassword(String email, String password,
                                              OnSuccessListener onSuccessListener,
                                              OnFailureListener onFailureListener){
            mAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(onSuccessListener)
                    .addOnFailureListener(onFailureListener);
        }
        public void forgotPassword(String email, OnSuccessListener onSuccessListener,
                                   OnFailureListener onFailureListener){
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnSuccessListener(onSuccessListener)
                    .addOnFailureListener(onFailureListener);
        }




        public Intent getGoogleSignInIntent(Context context) {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(context.getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();

            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(context, gso);
            // Optional: sign out to force a new login each time
            googleSignInClient.signOut();
            return googleSignInClient.getSignInIntent();
        }

        public void handleGoogleSignInResult(Intent data, GoogleSignInCallback callback) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account, callback);
            } catch (ApiException e) {
                callback.onFailure(e);
            }
        }
        private void firebaseAuthWithGoogle(GoogleSignInAccount acct,
                                            final GoogleSignInCallback callback) {
            AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(task -> {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (task.isSuccessful() && user != null) {
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            Map<String, Object> userData = new HashMap<>();
                            String displayName = user.getDisplayName();
                            userData.put("displayName", displayName);
                            db.collection("users").document(user.getUid())
                                    .set(userData, SetOptions.merge())
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d("displayName",
                                                    Objects.requireNonNull(e
                                                            .getMessage()));
                                        }
                                    });
                            callback.onSuccess();
                        } else {
                            callback.onFailure(task.getException());
                        }
                    });
        }

        public void signUpWithEmailPassword(String email, String password, String username, OnSuccessListener
                onSuccessListener, OnFailureListener onFailureListener){
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(new OnSuccessListener() {
                        @Override
                        public void onSuccess(Object o) {

                            // Send welcome email
                            final String usernameEmail = "spivak.toti@gmail.com";
                            final String passwordEmail = "axzwhdzahfkamgzo";
                            final String subject = "TowerLands";
                            final String body = "Thank you for signing up!\nI hope you enjoy the game!";
                            new EmailSender(usernameEmail, passwordEmail, email, subject, body).execute();

                            // Get the current Firebase user and update the profile with the display name
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if (user != null) {
                                UserProfileChangeRequest profileUpdates =
                                        new UserProfileChangeRequest.Builder()
                                                .setDisplayName(username)
                                                .build();
                                user.updateProfile(profileUpdates)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                // Also update the Firestore "users" document with the display name
                                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                                Map<String, Object> userData = new HashMap<>();
                                                userData.put("displayName", username);
                                                db.collection("users").document(user.getUid())
                                                        .set(userData, SetOptions.merge())
                                                        .addOnSuccessListener(onSuccessListener);
                                            }
                                        });
                            }
                        }

            })
                    .addOnFailureListener(onFailureListener);
        }
    }
}
