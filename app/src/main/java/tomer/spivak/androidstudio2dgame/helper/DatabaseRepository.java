package tomer.spivak.androidstudio2dgame.helper;


import static tomer.spivak.androidstudio2dgame.helper.EmailSender.sendEmail;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import tomer.spivak.androidstudio2dgame.R;
import tomer.spivak.androidstudio2dgame.gameActivity.GameCheckCallback;
import tomer.spivak.androidstudio2dgame.gameActivity.OnBoardLoadedListener;
import tomer.spivak.androidstudio2dgame.intermediate.LeaderboardCallback;
import tomer.spivak.androidstudio2dgame.intermediate.LeaderboardEntry;
import tomer.spivak.androidstudio2dgame.model.Cell;
import tomer.spivak.androidstudio2dgame.model.GameState;
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

    public void saveBoard(GameState gameState, OnCompleteListener<Void> onCompleteListener, Context context) {
        if (!isOnline(context) || isGuest()) {
            // simulate a failure Task so onComplete still fires:
            Task<Void> fake = Tasks.forException(new Exception("No internet"));
            onCompleteListener.onComplete(fake);
            return;
        }

        FirebaseUser user = getUserInstance();


        Map<String, Object> boardData = convertBoardToMap(gameState.getGrid().clone());
        Map<String, Object> metaData = createMetaData(gameState);

        WriteBatch batch = db.batch();

        DocumentReference boardRef = db.collection("users").document(user.getUid()).collection("currentGame")
                .document("board objects");

        DocumentReference metaRef = db.collection("users").document(user.getUid()).collection("currentGame")
                .document("meta");

        batch.set(boardRef, boardData, SetOptions.merge());
        batch.set(metaRef, metaData, SetOptions.merge());

        batch.commit().addOnCompleteListener(onCompleteListener);
    }

    private Map<String, Object> convertBoardToMap(Cell[][] grid) {
        Map<String, Object> boardData = new HashMap<>();
        for (int i = 0; i < grid.length; i++) {
            List<Map<String, Object>> rowData = new ArrayList<>();
            for (int j = 0; j < grid[i].length; j++) {
                Log.d("zig", "i, j: " + i + ", " + j + ", grid:" + grid[i][j].toMap().toString());
                Cell cell = grid[i][j];
                if (cell.getObject() != null && !cell.getPosition().equals(cell.getObject().getPosition()))
                    cell.removeObject();
                rowData.add(cell.toMap());
            }
            boardData.put("row_" + i, rowData);
        }
        return boardData;
    }

    private Map<String, Object> createMetaData(GameState gameState) {
        Map<String, Object> metaData = new HashMap<>();
        metaData.put("level", gameState.getDifficulty().name());
        metaData.put("millis from start of the game", gameState.getCurrentTimeOfGame());
        metaData.put("currentRound", gameState.getCurrentRound());
        metaData.put("shnuzes", gameState.getShnuzes());
        metaData.put("dayTime", gameState.getDayTime());
        return metaData;
    }
    public void loadCurrentGame(OnBoardLoadedListener listener, Context context) {
        if (isGuest() && !isOnline(context)) return;
        FirebaseUser user = authHelper.getUserInstance();

        db.collection("users").document(user.getUid()).collection("currentGame").document("meta").get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String difficultyName = documentSnapshot.getString("level");
                        Long gameTime        = documentSnapshot.getLong("millis from start of the game");
                        Long currentRoundL    = documentSnapshot.getLong("currentRound");
                        Long shnuzesL         = documentSnapshot.getLong("shnuzes");
                        Boolean dt           = documentSnapshot.getBoolean("dayTime");
                        DifficultyLevel difficultyLevel = DifficultyLevel.MEDIUM;
                        long timeSinceGameStart = 0L;
                        int currentRound = 1, shnuzes = -1;
                        boolean dayTime;
                        if (difficultyName != null)
                            difficultyLevel = DifficultyLevel.valueOf(difficultyName);
                        if (gameTime     != null)
                            timeSinceGameStart = gameTime;
                        if (currentRoundL != null)
                            currentRound = (currentRoundL.intValue());
                        if (shnuzesL      != null)
                            shnuzes = (shnuzesL.intValue());
                        dayTime = (Boolean.TRUE.equals(dt));

                        DifficultyLevel finalDifficultyLevel = difficultyLevel;
                        Long finalTimeSinceGameStart = timeSinceGameStart;
                        int finalCurrentRound = currentRound;
                        int finalShnuzes = shnuzes;
                        boolean finalDayTime = dayTime;
                        db.collection("users")
                                .document(user.getUid())
                                .collection("currentGame")
                                .document("board objects")
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        listener.onBoardLoaded(documentSnapshot.getData(), finalDifficultyLevel, finalTimeSinceGameStart, finalCurrentRound,
                                                finalShnuzes, finalDayTime);
                                    }
                                });
                    }
                });
    }

    public void checkIfTheresAGame(GameCheckCallback callback) {
        if (isGuest()) {
            callback.onCheckCompleted(false);
            return;
        }
        FirebaseUser user = authHelper.getUserInstance();
        db.collection("users").document(user.getUid()).collection("currentGame").document("board objects").get()
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

    public void removeBoard(OnCompleteListener<Void> onCompleteListener) {
        if (isGuest())
            return;
        FirebaseUser user = authHelper.getUserInstance();
        Log.d("sigma", "removing board: " + user);
        db.collection("users").document(user.getUid()).collection("currentGame").document("board objects").delete().
                addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d("sigma", "board removed:" + task.isSuccessful());
                db.collection("users").document(user.getUid()).collection("currentGame").document("meta").delete().
                        addOnCompleteListener(onCompleteListener);
            }
        });

    }



    public void logResults(GameViewModel vm) {
        if (isGuest()) return;

        FirebaseUser user = authHelper.getUserInstance();
        int round = vm.getRound();

        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Map<String,Object> lb = documentSnapshot.exists() ? (Map<String,Object>)documentSnapshot.get("leaderboard") : null;

                        Long maxR = lb == null ? null : (Long)lb.get("max round");
                        if (maxR == null || round > maxR) {
                            Map<String, Object> leaderboard = new HashMap<>();
                            leaderboard.put("max round", round);

                            db.collection("users").document(user.getUid()).set(Collections.singletonMap("leaderboard", leaderboard), SetOptions
                                    .merge());
                        }
                    }
                });
    }


    public void fetchLeaderboardFromDatabase(final LeaderboardCallback callback) {
        final List<LeaderboardEntry> maxRounds = new ArrayList<>();
        db.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                Map<String, Object> leaderboard = (Map<String, Object>) document.get("leaderboard");
                                if (leaderboard != null && leaderboard.get("max round") != null) {
                                    int maxRound = ((Number) Objects.requireNonNull(leaderboard.get("max round"))).intValue();
                                    int gamesPlayed = 0;
                                    String displayName = document.getString("displayName");
                                    if (leaderboard.get("games played") == null)
                                        leaderboard.put("games played", 0); else
                                         gamesPlayed = ((Number) Objects.requireNonNull(leaderboard.get("games played"))).intValue();

                                    leaderboard.putIfAbsent("enemies defeated", 0);
                                    int enemiesDefeated = ((Number) Objects.requireNonNull(leaderboard.get("enemies defeated"))).intValue();
                                    maxRounds.add(new LeaderboardEntry(maxRound, displayName, gamesPlayed, enemiesDefeated));
                                }
                            }
                            maxRounds.sort(Collections.reverseOrder());
                            callback.onLeaderboardFetched(maxRounds);
                        } else {
                            callback.onLeaderboardFetched(new ArrayList<>());
                        }
                    }
                });
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
        profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(context).load(uri).into(imageView);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Failed to fetch image: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    public FirebaseUser getUserInstance() {
        if (authHelper == null) {
            authHelper = new AuthenticationHelper();
        }
        return authHelper.getUserInstance();
    }

    public boolean isGuest() {
        if (authHelper == null) {
            authHelper = new AuthenticationHelper();
        }
        return authHelper.isGuest();
    }

    public void incrementEnemiesDefeated(int enemiesDefeated) {
        FirebaseUser user = getUserInstance();
        db.collection("users").document(user.getUid()).update("leaderboard.enemies defeated", FieldValue.increment(enemiesDefeated));
    }

    public void incrementGamesPlayed(Context context) {
        if(isGuest() && !isOnline(context))
            return;
        FirebaseUser user = getUserInstance();

        db.collection("users").document(user.getUid()).update("leaderboard.games played", FieldValue.increment(1));

    }
    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        Network network = cm.getActiveNetwork();
        if (network == null) return false;
        NetworkCapabilities caps = cm.getNetworkCapabilities(network);
        return caps != null
                && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }



    public interface GoogleSignInCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    private class AuthenticationHelper {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        public void loginWithEmailAndPassword(String email, String password, OnSuccessListener onSuccessListener,
                                              OnFailureListener onFailureListener){
            mAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(onSuccessListener)
                    .addOnFailureListener(onFailureListener);
        }
        public void forgotPassword(String email, OnSuccessListener onSuccessListener,
                                   OnFailureListener onFailureListener){
            FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnSuccessListener(onSuccessListener)
                    .addOnFailureListener(onFailureListener);
        }

        public Intent getGoogleSignInIntent(Context context) {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(context.getString(R.string.default_web_client_id)).requestEmail().build();

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
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if (task.isSuccessful() && user != null) {
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                Map<String, Object> userData = new HashMap<>();
                                String displayName = user.getDisplayName();
                                userData.put("displayName", displayName);

                                db.collection("users").document(user.getUid()).set(userData, SetOptions.merge());
                                callback.onSuccess();
                            } else {
                                callback.onFailure(task.getException());
                            }
                        }
                    });
        }

        public void signUpWithEmailPassword(String email, String password, String username, OnSuccessListener onSuccessListener, Context context,
                                            Uri uri){
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            FirebaseUser newUser = authResult.getUser();

                            String uid = newUser.getUid();
                            sendEmail(email, context);
                            uploadImage(uri, onSuccessListener, context, uid);
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(username).build();
                            newUser.updateProfile(profileUpdates)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                                            Map<String, Object> userData = new HashMap<>();
                                            userData.put("displayName", username);
                                            db.collection("users").document(newUser.getUid()).set(userData, SetOptions.merge());
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "failed to sign up: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }


        public FirebaseUser getUserInstance() {
            return FirebaseAuth.getInstance().getCurrentUser();
        }
        public boolean isGuest(){
            if (authHelper == null) {
                authHelper = new AuthenticationHelper();
            }
            return getUserInstance() == null ||
                    Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).isAnonymous();
        }
    }
}
