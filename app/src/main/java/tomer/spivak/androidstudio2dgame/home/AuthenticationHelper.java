package tomer.spivak.androidstudio2dgame.home;


import android.content.Context;
import android.content.Intent;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import tomer.spivak.androidstudio2dgame.R;

public class AuthenticationHelper {
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

    public interface GoogleSignInCallback {
        void onSuccess();
        void onFailure(Exception e);
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
                    if (task.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    public void signUpWithEmailPassword(String email, String password, OnSuccessListener
            onSuccessListener, OnFailureListener onFailureListener){
        mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(onFailureListener);
    }
}
