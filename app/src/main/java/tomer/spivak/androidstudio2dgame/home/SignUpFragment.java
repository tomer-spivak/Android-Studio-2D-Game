package tomer.spivak.androidstudio2dgame.home;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

        TextWatcher textWatcherPassword = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String email = ((EditText)view.findViewById(R.id.etEmail)).getText()
                        .toString().trim();
                String pass = ((EditText)(view.findViewById(R.id.etPassword))).getText()
                        .toString().trim();
                String name = ((EditText)(view.findViewById(R.id.etUsername))).getText().toString();
                boolean isValid = validatePassword(pass, tvPasswordError) &&
                        validateEmail(email, tvEmailError)
                         && validateUsername(name, tvUsernameError);
                Log.d("TAG", "onTextChanged: " + isValid);
                btn.setEnabled(isValid);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };
        TextWatcher textWatcherEmail = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String email = ((EditText)view.findViewById(R.id.etEmail)).getText()
                        .toString().trim();
                String pass = ((EditText)(view.findViewById(R.id.etPassword))).getText()
                        .toString().trim();
                String name = ((EditText)(view.findViewById(R.id.etUsername))).getText().toString();
                boolean isValid = validateEmail(email, tvEmailError) &&
                        validatePassword(pass, tvPasswordError) &&
                        validateUsername(name, tvUsernameError);
                Log.d("TAG", "onTextChanged: " + isValid);
                btn.setEnabled(isValid);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };
        TextWatcher textWatcherUsername = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String email = ((EditText)view.findViewById(R.id.etEmail)).getText()
                        .toString().trim();
                String pass = ((EditText)(view.findViewById(R.id.etPassword))).getText()
                        .toString().trim();
                String name = ((EditText)(view.findViewById(R.id.etUsername))).getText().toString();
                boolean isValid = validateUsername(name, tvUsernameError) &&
                        validateEmail(email, tvEmailError) &&
                        validatePassword(pass, tvPasswordError);
                Log.d("TAG", "onTextChanged: " + isValid);
                btn.setEnabled(isValid);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        etEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    Log.d("TAG", "onFocusChange:" + tvEmailError.getText().toString());
                        etEmail.addTextChangedListener(textWatcherEmail);

                } else {
                    Log.d("TAG", "onFocusChange:" + tvEmailError.getText().toString());
                    validateEmail(etEmail.getText().toString(), tvEmailError);
                }
            }
        });

        etPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                        etPassword.addTextChangedListener(textWatcherPassword);

                } else {
                    validatePassword(etPassword.getText().toString(), tvPasswordError);
                }
            }
        });

        etUsername.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                        etUsername.addTextChangedListener(textWatcherUsername);

                } else {
                    validateUsername(etUsername.getText().toString(), tvUsernameError);
                }
            }
        });


        ActivityResultLauncher<Intent> googleSignInLauncher;

        googleSignInLauncher = registerForActivityResult(new ActivityResultContracts
                .StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    authHelper.handleGoogleSignInResult(result.getData(), new AuthenticationHelper
                            .GoogleSignInCallback() {
                        @Override
                        public void onSuccess() {
                            Intent intent = new Intent(getContext(), IntermediateActivity.class);
                            startActivity(intent);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.w("TAG", "Google sign-in failed", e);
                        }
                    });
                }
            }
        });

        btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = authHelper.getGoogleSignInIntent(requireContext());
                googleSignInLauncher.launch(signInIntent);
            }
        });

        return view;
    }

    private boolean validatePassword(String pass, TextView tvPasswordError) {
        if (pass.length() < 6){
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