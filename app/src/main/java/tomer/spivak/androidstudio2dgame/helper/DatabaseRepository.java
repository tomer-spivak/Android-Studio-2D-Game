package tomer.spivak.androidstudio2dgame.helper;


import static tomer.spivak.androidstudio2dgame.helper.EmailSender.sendEmail;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import tomer.spivak.androidstudio2dgame.R;
import tomer.spivak.androidstudio2dgame.gameActivity.BoardMapper;
import tomer.spivak.androidstudio2dgame.gameActivity.GameCheckCallback;
import tomer.spivak.androidstudio2dgame.gameActivity.OnBoardLoadedListener;
import tomer.spivak.androidstudio2dgame.intermediate.LeaderboardCallback;
import tomer.spivak.androidstudio2dgame.intermediate.LeaderboardEntry;
import tomer.spivak.androidstudio2dgame.model.Cell;
import tomer.spivak.androidstudio2dgame.modelEnums.DifficultyLevel;
import tomer.spivak.androidstudio2dgame.viewModel.GameViewModel;

public class DatabaseRepository {
    private final FirebaseFirestore db;
    private AuthenticationHelper authHelper;
    private static DatabaseRepository instance;

    private DatabaseRepository(Context context){
        FirebaseApp.initializeApp(context);
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized DatabaseRepository getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseRepository(context);
        }
        return instance;
    }

    public void saveBoard(Cell[][] board, String difficulty, Long gameTime, int currentRound, int shnuzes,
                          OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        if (isGuest()) return;
        FirebaseUser user = authHelper.getUserInstance();

        Map<String, Object> boardData = new HashMap<>();
        for (int i = 0; i < board.length; i++) {
            List<Map<String, Object>> rowData = new ArrayList<>();
            for (int j = 0; j < board[i].length; j++) {
                rowData.add(board[i][j].toMap());
            }
            boardData.put("row_" + i, rowData);
        }

        // Save board structure
        db.collection("users")
                .document(user.getUid())
                .collection("currentGame")
                .document("board objects")
                .set(boardData)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);

        // Save all game metadata in a single "meta" document
        Map<String, Object> metaData = new HashMap<>();
        metaData.put("level", difficulty);
        metaData.put("millis from start of the game", gameTime);
        metaData.put("currentRound", currentRound);
        metaData.put("shnuzes", shnuzes);

        db.collection("users")
                .document(user.getUid())
                .collection("currentGame")
                .document("meta")
                .set(metaData);
    }

    public void loadBoard(OnSuccessListener<DocumentSnapshot> onSuccess, OnFailureListener onFailure) {
        if (isGuest()) {
            return;
        }
        FirebaseUser user = authHelper.getUserInstance();

        db.collection("users")
                .document(user.getUid())
                .collection("currentGame")
                .document("board objects")
                .get()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public void loadDataFromDataBase(BoardMapper boardMapper, OnBoardLoadedListener listener) {
        if (isGuest()) return;
        FirebaseUser user = authHelper.getUserInstance();

        db.collection("users")
                .document(user.getUid())
                .collection("currentGame")
                .document("meta")
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot metaSnapshot) {
                        // Load all meta fields from one document
                        String difficultyName = metaSnapshot.getString("level");
                        Long gameTime = metaSnapshot.getLong("millis from start of the game");
                        Long currentRound = metaSnapshot.getLong("currentRound");
                        Long shnuzes = metaSnapshot.getLong("shnuzes");

                        if (difficultyName != null) {
                            boardMapper.setDifficulty(DifficultyLevel.valueOf(difficultyName));
                        }
                        if (gameTime != null) {
                            boardMapper.setTimeSinceStartOfGame(gameTime);
                        }
                        if (currentRound != null) {
                            boardMapper.setCurrentRound(currentRound.intValue());
                        }
                        if (shnuzes != null) {
                            boardMapper.setShnuzes(shnuzes.intValue());
                        }

                        // Load board structure
                        loadBoard(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot boardSnapshot) {
                                createBoardInBoardMapper(boardMapper, boardSnapshot, listener);
                            }
                        }, null);
                    }
                });
    }

    private void createBoardInBoardMapper(BoardMapper boardMapper, DocumentSnapshot documentSnapshot,
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
        if (isGuest()) {
            callback.onCheckCompleted(false);
            return;
        }
        FirebaseUser user = authHelper.getUserInstance();
        db.collection("users")
                .document(user.getUid())
                .collection("currentGame")
                .document("board objects")
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        callback.onCheckCompleted(documentSnapshot.exists() &&
                                documentSnapshot.getData() != null);
                        Log.d("frank", documentSnapshot.exists() + ", " + documentSnapshot.getData());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onCheckCompleted(false);
                        Log.d("frank", "fail");
                    }
                });
    }

    public void removeBoard() {
        if (isGuest())
            return;
        FirebaseUser user = authHelper.getUserInstance();
        db.collection("users")
                .document(user.getUid())
                .collection("currentGame")
                .document("board objects")
                .delete();

        db.collection("users")
                .document(user.getUid())
                .collection("currentGame")
                .document("meta")
                .delete();
    }

    public void logResults(GameViewModel viewModel) {
        if (isGuest())
            return;
        FirebaseUser user = authHelper.getUserInstance();
        int round = viewModel.getRound();
        int enemiesDefeated = viewModel.getEnemiesDefeated();
        Log.d("oof", "fuck");
        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Log.d("oof", String.valueOf(documentSnapshot));
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        Map<String, Object> leaderboard = (Map<String, Object>) documentSnapshot.get("leaderboard");

                        if (leaderboard == null) {
                            leaderboard = new HashMap<>();
                        }

                        // Save round if needed
                        Long maxRound = (Long) leaderboard.get("max round");
                        if (maxRound == null || round > maxRound) {
                            saveRound(round, user);
                        }
                        Log.d("oof", "wtf");
                        // Always increment these
                        db.collection("users").document(user.getUid()).update("leaderboard.enemies defeated", FieldValue.increment(enemiesDefeated));
                        db.collection("users").document(user.getUid()).update("leaderboard.games played", FieldValue.increment(1));

                    } else {
                        // If document doesn't exist, initialize leaderboard
                        saveRound(round, user);
                        Map<String, Object> initData = new HashMap<>();
                        initData.put("leaderboard.enemies defeated", enemiesDefeated);
                        initData.put("leaderboard.games played", 1);
                        db.collection("users").document(user.getUid()).set(initData, SetOptions.merge());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("oof", "shit: " + e.getMessage());
                    }
                });
    }

    private void saveRound(int round, FirebaseUser user) {
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
                            Map<String, Object> leaderboard = (Map<String, Object>) document.get("leaderboard");
                            if (leaderboard != null && leaderboard.get("max round") != null) {
                                int maxRound = ((Number) Objects.requireNonNull(leaderboard.get("max round"))).intValue();
                                String displayName = document.getString("displayName");
                                int gamesPlayed = ((Number) Objects.requireNonNull(leaderboard.get("games played"))).intValue();
                                int enemiesDefeated = ((Number) Objects.requireNonNull(leaderboard.get("enemies defeated"))).intValue();
                                maxRounds.add(new LeaderboardEntry(maxRound, displayName, gamesPlayed, enemiesDefeated));
                            }
                        }
                        maxRounds.sort(Collections.reverseOrder());
                        callback.onLeaderboardFetched(maxRounds);
                    } else {
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

    public void signUpWithEmailPassword(String string, String string1, String username, OnSuccessListener onSuccessListener, Context context,
                                        Uri uri) {
        Log.d("sigma", "wtf");
        if (authHelper == null)
            authHelper = new AuthenticationHelper();
        authHelper.signUpWithEmailPassword(string, string1, username, onSuccessListener, context, uri);
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

    public void uploadImage(Uri imageUri, OnSuccessListener<Void> onSuccess, Context context, String uid) {
        // 1) Get a fresh root ref

        // 2) Create a one-off child ref for this upload

        // Upload under UID
        StorageReference profileRef = FirebaseStorage
                .getInstance()
                .getReference("profileImages/" + uid + ".jpg");

        // 3) Upload
        Log.d("sigma", "Uploading image for UID = " + uid);

        profileRef.putFile(imageUri)
                .addOnSuccessListener(task -> profileRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            Log.d("sigma", "URL is " + uri);
                            onSuccess.onSuccess(null);
                        }))
                .addOnFailureListener(e -> {
                    Log.d("sigma", "upload failed: " + e.getMessage());
                    Toast.makeText(context,
                            "failed to upload image: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    public void fetchAndSetImage(ImageView imageView, Context context) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        Uri photoUrl = user.getPhotoUrl();

        if (photoUrl != null) {
            Glide.with(context)
                    .load(photoUrl)
                    .into(imageView);
            return;
        }

        StorageReference profileImageRef = storageRef.child("profileImages/" + user.getUid() + ".jpg");
        profileImageRef.getDownloadUrl().addOnSuccessListener(uri ->
                        Glide.with(context)
                                .load(uri)
                                .into(imageView))
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Failed to fetch image: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    public FirebaseUser getUserInstance() {
        // initialize on first use
        if (authHelper == null) {
            authHelper = new AuthenticationHelper();
        }
        return authHelper.getUserInstance();
    }

    public boolean isGuest() {
        // initialize on first use
        if (authHelper == null) {
            authHelper = new AuthenticationHelper();
        }
        return authHelper.isGuest();
    }

    public interface GoogleSignInCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    private class AuthenticationHelper {
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

        public void signUpWithEmailPassword(String email, String password, String username, OnSuccessListener onSuccessListener, Context context,
                                            Uri uri){
            Log.d("sigma", "big");
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            FirebaseUser newUser = authResult.getUser();

                            String uid = newUser.getUid();
                            Log.d("sigma", "chungs");
                            sendEmail(email, context);
                            uploadImage(uri, onSuccessListener, context, uid);
                            Log.d("sigma", "wtf");
                            UserProfileChangeRequest profileUpdates =
                                    new UserProfileChangeRequest.Builder()
                                            .setDisplayName(username)
                                            .build();
                            newUser.updateProfile(profileUpdates)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                                            Map<String, Object> userData = new HashMap<>();
                                            userData.put("displayName", username);
                                            db.collection("users").document(newUser.getUid())
                                                    .set(userData, SetOptions.merge());
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(context, "failed to create a user: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }


        public FirebaseUser getUserInstance() {
            return FirebaseAuth.getInstance().getCurrentUser();
        }
        public boolean isGuest(){
            // initialize on first use
            if (authHelper == null) {
                authHelper = new AuthenticationHelper();
            }
            return getUserInstance() == null ||
                    Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).isAnonymous();
        }
    }
}
