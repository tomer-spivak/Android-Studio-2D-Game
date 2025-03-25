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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import tomer.spivak.androidstudio2dgame.R;
import tomer.spivak.androidstudio2dgame.gameActivity.DatabaseRepository;
import tomer.spivak.androidstudio2dgame.intermediate.IntermediateActivity;

public class SignUpFragment extends Fragment {
    DatabaseRepository repository;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);
        repository = new DatabaseRepository(requireContext());
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
                            repository.handleGoogleSignInResult(result.getData(),
                                    new DatabaseRepository.GoogleSignInCallback() {
                                @Override
                                public void onSuccess() {
                                            // proceed to the next activity.
                                            Intent intent = new Intent(getActivity(), IntermediateActivity.class);
                                            startActivity(intent);
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
            Intent signInIntent = repository.getGoogleSignInIntent(requireContext());
            googleSignInLauncher.launch(signInIntent);
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                repository.signUpWithEmailPassword(etEmail.getText().toString(), etPassword.getText().toString(),
                        etUsername.getText().toString(), new OnSuccessListener() {
                            @Override
                            public void onSuccess(Object o) {

                                // proceed to the next activity.
                                Intent intent = new Intent(getActivity(), IntermediateActivity.class);
                                startActivity(intent);
                            }
                        }, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("TAG", "Sign up failed", e);
                            }
                        });
            }
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
