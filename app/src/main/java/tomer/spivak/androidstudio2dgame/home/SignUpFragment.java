package tomer.spivak.androidstudio2dgame.home;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import tomer.spivak.androidstudio2dgame.R;
import tomer.spivak.androidstudio2dgame.intermediate.IntermediateActivity;

public class SignUpFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);
        AuthenticationHelper authHelper = new AuthenticationHelper();

        Button btn = view.findViewById(R.id.btnSignUp);
        Button btnGoogle = view.findViewById(R.id.btnGoogleSignUp);
        EditText etEmail = view.findViewById(R.id.etEmail);
        EditText etPassword = view.findViewById(R.id.etPassword);
        EditText etUsername = view.findViewById(R.id.etUsername);
        TextView tvEmailError = view.findViewById(R.id.tvEmailError);
        TextView tvPasswordError = view.findViewById(R.id.tvPasswordError);
        TextView tvUsernameError = view.findViewById(R.id.tvUsernameError);

        // TextWatchers for validation (unchanged)…
        TextWatcher textWatcherPassword = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String email = etEmail.getText().toString().trim();
                String pass = etPassword.getText().toString().trim();
                String name = etUsername.getText().toString();
                boolean isValid = validatePassword(pass, tvPasswordError) &&
                        validateEmail(email, tvEmailError) &&
                        validateUsername(name, tvUsernameError);
                Log.d("TAG", "onTextChanged: " + isValid);
                btn.setEnabled(isValid);
            }
            @Override
            public void afterTextChanged(Editable s) { }
        };
        TextWatcher textWatcherEmail = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String email = etEmail.getText().toString().trim();
                String pass = etPassword.getText().toString().trim();
                String name = etUsername.getText().toString();
                boolean isValid = validateEmail(email, tvEmailError) &&
                        validatePassword(pass, tvPasswordError) &&
                        validateUsername(name, tvUsernameError);
                Log.d("TAG", "onTextChanged: " + isValid);
                btn.setEnabled(isValid);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        };
        TextWatcher textWatcherUsername = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String email = etEmail.getText().toString().trim();
                String pass = etPassword.getText().toString().trim();
                String name = etUsername.getText().toString();
                boolean isValid = validateUsername(name, tvUsernameError) &&
                        validateEmail(email, tvEmailError) &&
                        validatePassword(pass, tvPasswordError);
                Log.d("TAG", "onTextChanged: " + isValid);
                btn.setEnabled(isValid);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        };

        etEmail.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                etEmail.addTextChangedListener(textWatcherEmail);
            } else {
                validateEmail(etEmail.getText().toString(), tvEmailError);
            }
        });

        etPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                etPassword.addTextChangedListener(textWatcherPassword);
            } else {
                validatePassword(etPassword.getText().toString(), tvPasswordError);
            }
        });

        etUsername.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                etUsername.addTextChangedListener(textWatcherUsername);
            } else {
                validateUsername(etUsername.getText().toString(), tvUsernameError);
            }
        });

        ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            authHelper.handleGoogleSignInResult(result.getData(), new AuthenticationHelper.GoogleSignInCallback() {
                                @Override
                                public void onSuccess() {
                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                                    Map<String, Object> userData = new HashMap<>();
                                    String displayName = user.getDisplayName();
                                    userData.put("displayName", displayName);
                                    db.collection("users").document(user.getUid())
                                            .set(userData, SetOptions.merge())
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    // Now that the display name is stored both in Firebase Auth and Firestore,
                                                    // proceed to the next activity.
                                                    Intent intent = new Intent(getActivity(), IntermediateActivity.class);
                                                    startActivity(intent);
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.d("displayName",
                                                            Objects.requireNonNull(e
                                                                    .getMessage()));
                                                }
                                            });
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Log.w("TAG", "Google sign-in failed", e);
                                }
                            });
                        }
                    }
                }
        );

        btnGoogle.setOnClickListener(v -> {
            Intent signInIntent = authHelper.getGoogleSignInIntent(requireContext());
            googleSignInLauncher.launch(signInIntent);
        });

        btn.setOnClickListener(v -> {
            authHelper.signUpWithEmailPassword(etEmail.getText().toString(),
                    etPassword.getText().toString(),
                    new OnSuccessListener() {
                        @Override
                        public void onSuccess(Object o) {
                            // Send welcome email
                            String usernameEmail = "spivak.toti@gmail.com";
                            String passwordEmail = "axzwhdzahfkamgzo";
                            String recipient = etEmail.getText().toString();
                            String subject = "TowerLands";
                            String body = "Thank you for signing up!\nI hope you enjoy the game!";
                            new EmailSender(usernameEmail, passwordEmail, recipient, subject, body).execute();

                            // Get the current Firebase user and update the profile with the display name
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if (user != null) {
                                UserProfileChangeRequest profileUpdates =
                                        new UserProfileChangeRequest.Builder()
                                                .setDisplayName(etUsername.getText().toString())
                                                .build();
                                user.updateProfile(profileUpdates)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                // Also update the Firestore "users" document with the display name
                                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                                Map<String, Object> userData = new HashMap<>();
                                                userData.put("displayName", etUsername.getText().toString());
                                                db.collection("users").document(user.getUid())
                                                        .set(userData, SetOptions.merge())
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                // Now that the display name is stored both in Firebase Auth and Firestore,
                                                                // proceed to the next activity.
                                                                Intent intent = new Intent(getActivity(), IntermediateActivity.class);
                                                                startActivity(intent);
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Toast.makeText(getContext(), "Failed to update user data", Toast.LENGTH_LONG).show();
                                                            }
                                                        });
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(getContext(), "Failed to update profile", Toast.LENGTH_LONG).show();
                                            }
                                        });
                            }
                        }
                    },
                    new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        return view;
    }

    private boolean validatePassword(String pass, TextView tvPasswordError) {
        if (pass.length() < 6) {
            tvPasswordError.setText("Password must be at least 6 characters");
            return false;
        }
        tvPasswordError.setText("");
        return true;
    }

    private boolean validateEmail(String email, TextView tvEmailError) {
        Log.d("TAG", "validateEmail: " + email);
        if (!email.contains("@") || !email.substring(email.indexOf("@")).contains(".com")) {
            tvEmailError.setText("Invalid Email");
            return false;
        }
        tvEmailError.setText("");
        return true;
    }

    private boolean validateUsername(String username, TextView tvUsernameError) {
        if (username.length() < 3) {
            tvUsernameError.setText("Username must be at least 3 characters");
            return false;
        }
        tvUsernameError.setText("");
        return true;
    }
}
