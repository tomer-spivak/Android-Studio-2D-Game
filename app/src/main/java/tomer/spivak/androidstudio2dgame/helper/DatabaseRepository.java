package tomer.spivak.androidstudio2dgame.helper;


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

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import tomer.spivak.androidstudio2dgame.R;
import tomer.spivak.androidstudio2dgame.intermediate.IntermediateActivity;
import tomer.spivak.androidstudio2dgame.projectManagement.OnBoardLoadedListener;
import tomer.spivak.androidstudio2dgame.intermediate.LeaderboardCallback;
import tomer.spivak.androidstudio2dgame.intermediate.LeaderboardEntry;
import tomer.spivak.androidstudio2dgame.model.Cell;
import tomer.spivak.androidstudio2dgame.model.GameState;
import tomer.spivak.androidstudio2dgame.modelEnums.DifficultyLevel;

public class DatabaseRepository {
    private final FirebaseFirestore db;
    private static DatabaseRepository instance;
    private final FirebaseAuth mAuth;

    private DatabaseRepository(Context context){
        FirebaseApp.initializeApp(context);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    public static synchronized DatabaseRepository getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseRepository(context);
        }
        return instance;
    }

    public void saveBoard(GameState gameState, OnCompleteListener<Void> onCompleteListener, Context context) {
        if (isGuest(context)) {
            Task<Void> fake = Tasks.forException(new Exception("No internet"));
            onCompleteListener.onComplete(fake);
            return;
        }

        FirebaseUser user = getUserInstance();

        Map<String, Object> boardData = new HashMap<>();
        Cell[][] board = gameState.getGrid().clone();
        for (int i = 0; i < board.length; i++) {
            List<Map<String, Object>> rowData = new ArrayList<>();
            for (int j = 0; j < board[i].length; j++) {
                Cell cell = board[i][j];
                if (cell.getObject() != null && !cell.getPosition().equals(cell.getObject().getPosition()))
                    cell.removeObject();
                rowData.add(cell.toMap());
            }
            boardData.put("row_" + i, rowData);
        }

        Map<String, Object> metaData = new HashMap<>();
        metaData.put("Difficulty Level", gameState.getDifficulty().name());
        metaData.put("Time Since Game Start", gameState.getCurrentTimeOfGame());
        metaData.put("Current Round", gameState.getCurrentRound());
        metaData.put("Shnuzes", gameState.getShnuzes());
        metaData.put("Is Day Time", gameState.getDayTime());

        db.collection("users").document(user.getUid()).collection("currentGame").document("meta").set(metaData, SetOptions.merge())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) {
                    onCompleteListener.onComplete(task);
                    return;
                }
                db.collection("users").document(user.getUid()).collection("currentGame").document("board objects").set(boardData, SetOptions.merge())
                        .addOnCompleteListener(onCompleteListener);
            }
        });
}

    public void loadCurrentGame(OnBoardLoadedListener listener, Context context) {
        if (isGuest(context))
            return;
        FirebaseUser user = getUserInstance();
        OnFailureListener failureListener = new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Failed to load game: " + e.getMessage(), Toast.LENGTH_LONG).show();
                listener.onBoardLoaded(null, DifficultyLevel.MEDIUM, 0L, 1, -1, false);
            }
        };
        db.collection("users").document(user.getUid()).collection("currentGame").document("meta").get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot metaSnapshot) {

                        db.collection("users")
                                .document(user.getUid())
                                .collection("currentGame")
                                .document("board objects")
                                .get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        String difficultyName = metaSnapshot.getString("Difficulty Level");
                                        Long gameTimeL = metaSnapshot.getLong("Time Since Game Start");
                                        Long currentRoundL = metaSnapshot.getLong("Current Round");
                                        Long shnuzesL = metaSnapshot.getLong("Shnuzes");
                                        Boolean dayTime = metaSnapshot.getBoolean("Is Day Time");
                                        DifficultyLevel difficultyLevel = DifficultyLevel.MEDIUM;
                                        long timeSinceGameStart = 0L;
                                        int currentRound = 1, shnuzes = -1;
                                        if (difficultyName != null)
                                            difficultyLevel = DifficultyLevel.valueOf(difficultyName);
                                        if (gameTimeL!= null)
                                            timeSinceGameStart = gameTimeL;
                                        if (currentRoundL != null)
                                            currentRound = (currentRoundL.intValue());
                                        if (shnuzesL != null)
                                            shnuzes = (shnuzesL.intValue());

                                        listener.onBoardLoaded(documentSnapshot.getData(), difficultyLevel, timeSinceGameStart, currentRound, shnuzes, Boolean.TRUE.equals(dayTime));
                                    }
                                }).addOnFailureListener(failureListener);
                    }
                }).addOnFailureListener(failureListener);
    }

    public void checkIfTheresAGame(GameCheckCallback callback, Context context) {
        if (isGuest(context)) {
            callback.onCheckCompleted(false);
            return;
        }
        FirebaseUser user = getUserInstance();
        db.collection("users").document(user.getUid()).collection("currentGame").document("board objects").get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        callback.onCheckCompleted(documentSnapshot.exists() && documentSnapshot.getData() != null);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onCheckCompleted(false);
                    }
                });
    }

    public void incrementVictories(Context context) {
        if (isGuest(context)){
            return;
        }
        FirebaseUser user = getUserInstance();
        db.collection("users").document(user.getUid()).update("leaderboard.victories", FieldValue.increment(1));
    }

    public void removeBoard(OnCompleteListener<Void> onCompleteListener, Context context) {
        if (isGuest(context)){
            onCompleteListener.onComplete(null);
            return;
        }
        FirebaseUser user = getUserInstance();
        db.collection("users").document(user.getUid()).collection("currentGame").document("board objects").delete().
                addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                db.collection("users").document(user.getUid()).collection("currentGame").document("meta").delete().
                        addOnCompleteListener(onCompleteListener);
            }
        });

    }

    public void logMaxRound(int round, Context context) {
        if (isGuest(context))
            return;

        FirebaseUser user = getUserInstance();

        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Map<String, Object> leaderboardMap = null;
                        if (documentSnapshot.exists()) {
                            leaderboardMap = (Map<String, Object>) documentSnapshot.get("leaderboard");

                        }
                        Long maxRounds = null;
                        if (leaderboardMap != null) {
                            maxRounds = (Long) leaderboardMap.get("max round");
                        }
                        if (maxRounds == null || round > maxRounds) {
                            Map<String, Object> leaderboard = new HashMap<>();
                            leaderboard.put("max round", round);

                            db.collection("users").document(user.getUid()).set(Collections.singletonMap("leaderboard", leaderboard), SetOptions.merge());
                        }
                    }
                });
    }

    public void fetchLeaderboardFromDatabase(final LeaderboardCallback callback, Context context) {
        if(isGuest(context)){
            callback.onLeaderboardFetched(new ArrayList<>());
            return;
        }
        db.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<LeaderboardEntry> leaderboardEntries = new ArrayList<>();
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                Map<String, Object> leaderboardMap = (Map<String, Object>) document.get("leaderboard");
                                if (leaderboardMap != null && leaderboardMap.get("max round") != null) {
                                    int maxRound = ((Number) Objects.requireNonNull(leaderboardMap.get("max round"))).intValue();
                                    String username = document.getString("username");
                                    leaderboardMap.putIfAbsent("games played", 0);
                                    int gamesPlayed = ((Number) Objects.requireNonNull(leaderboardMap.get("games played"))).intValue();
                                    leaderboardMap.putIfAbsent("enemies defeated", 0);
                                    int enemiesDefeated = ((Number) Objects.requireNonNull(leaderboardMap.get("enemies defeated"))).intValue();
                                    leaderboardMap.putIfAbsent("victories", 0);
                                    int victories = ((Number) Objects.requireNonNull(leaderboardMap.get("victories"))).intValue();
                                    leaderboardEntries.add(new LeaderboardEntry(maxRound, username, gamesPlayed, enemiesDefeated, victories));
                                }
                            }
                            leaderboardEntries.sort(Collections.reverseOrder());
                            callback.onLeaderboardFetched(leaderboardEntries);
                        } else {
                            callback.onLeaderboardFetched(leaderboardEntries);
                        }
                    }
                });
    }

    public void incrementEnemiesDefeated(int enemiesDefeated, Context context) {
        if(isGuest(context)){
            return;
        }
        FirebaseUser user = getUserInstance();
        db.collection("users").document(user.getUid()).update("leaderboard.enemies defeated", FieldValue.increment(enemiesDefeated));
    }

    public FirebaseUser getUserInstance() {
        return mAuth.getCurrentUser();
    }

    public void incrementGamesPlayed(Context context) {
        if(isGuest(context))
            return;
        FirebaseUser user = getUserInstance();

        db.collection("users").document(user.getUid()).update("leaderboard.games played", FieldValue.increment(1));

    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null)
            return false;

        Network network = cm.getActiveNetwork();
        if (network == null)
            return false;
        NetworkCapabilities caps = cm.getNetworkCapabilities(network);
        return caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }

    public boolean isGuest(Context context) {
        if(!isOnline(context))
            return true;
        FirebaseUser u = getUserInstance();
        return u == null || u.isAnonymous();
    }

    public void signUpWithEmailPassword(String email, String password, String username, Context context, Uri uri) {
        if(!isOnline(context)){
            Toast.makeText(context, "You have no internet connection. Continue as guest.", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        FirebaseUser newUser = authResult.getUser();
                        if (newUser == null) {
                            Toast.makeText(context, "Failed to sign up. Continue as guest.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String uid = newUser.getUid();

                        StorageReference profileRef = FirebaseStorage.getInstance().getReference("profileImages/" + uid + ".jpg");

                        profileRef.putFile(uri)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                UserProfileChangeRequest upd = new UserProfileChangeRequest.Builder().setDisplayName(username)
                                                        .setPhotoUri(uri).build();
                                                newUser.updateProfile(upd)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                Map<String,Object> userData = new HashMap<>();
                                                                userData.put("username", username);
                                                                db.collection("users").document(uid).set(userData, SetOptions.merge());
                                                                context.startActivity(new Intent(context, IntermediateActivity.class));
                                                            }
                                                        });
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(context, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Sign-up failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void loginWithEmailAndPassword(String email, String password, OnFailureListener onFailure, Context context) {
        if(!isOnline(context)){
            onFailure.onFailure(new Exception("No internet"));
            return;
        }
        mAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                Intent intent = new Intent(context, IntermediateActivity.class);
                intent.putExtra("guest", false);
                context.startActivity(intent);
            }
        }).addOnFailureListener(onFailure);
    }

    public void forgotPassword(String email, Context context) {
        if(isOnline(context)){
            Toast.makeText(context, "no internet connection", Toast.LENGTH_LONG).show();
            return;
        }
        mAuth.sendPasswordResetEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(context, "Reset link sent to your email.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Error sending link: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void handleGoogleSignInResult(Intent data, GoogleSignInCallback callback, Context context) {
        if(!isOnline(context)){
            callback.onFailure(new Exception("No internet"));
            return;
        }
        GoogleSignIn.getSignedInAccountFromIntent(data).addOnSuccessListener(new OnSuccessListener<GoogleSignInAccount>() {
                    @Override
                    public void onSuccess(GoogleSignInAccount googleSignInAccount) {
                        AuthCredential cred = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);
                        mAuth.signInWithCredential(cred)
                                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
                                        FirebaseUser user = getUserInstance();
                                        if (user == null) {
                                            callback.onFailure(new IllegalStateException("No current user after sign-in"));
                                            return;
                                        }
                                        Map<String,Object> userData = new HashMap<>();
                                        userData.put("username", user.getDisplayName());
                                        db.collection("users").document(user.getUid()).set(userData, SetOptions.merge())
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        callback.onSuccess();
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        callback.onFailure(e);
                                                    }
                                                });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        callback.onFailure(e);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(e);
                    }
                });
    }

    public void reloadUser(OnSuccessListener<Void> onSuccess, OnFailureListener onFailure, Context context) {
        if(isGuest(context)){
            return;
        }
        getUserInstance().reload().addOnSuccessListener(onSuccess).addOnFailureListener(onFailure);
    }

    public void getUsernameAndImage(OnSuccessListener<String> onSuccess, ImageView imageView, Context context) {
        if(isGuest(context)){
            imageView.setImageResource(R.drawable.logo);
            onSuccess.onSuccess("guest");
            return;
        }

        FirebaseUser user = getUserInstance();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        Uri photoUrl = user.getPhotoUrl();
        if (photoUrl != null) {
            Glide.with(context).load(photoUrl).into(imageView);
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

        String uid = user.getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String username = "guest";
                        if (documentSnapshot.exists() && documentSnapshot.contains("username")) {
                            String str = documentSnapshot.getString("username");
                            if (str != null && !str.isEmpty()) {
                                username = str;
                            }
                        }
                        onSuccess.onSuccess(username);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        onSuccess.onSuccess("guest");
                        Toast.makeText(context, "couldnt load username: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public interface GoogleSignInCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

}

