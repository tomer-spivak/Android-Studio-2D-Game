package tomer.spivak.androidstudio2dgame.home;


import static android.app.Activity.RESULT_OK;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
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



import tomer.spivak.androidstudio2dgame.R;
import tomer.spivak.androidstudio2dgame.helper.DatabaseRepository;
import tomer.spivak.androidstudio2dgame.intermediate.IntermediateActivity;


public class LoginFragment extends Fragment {
    DatabaseRepository databaseRepository;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        databaseRepository = DatabaseRepository.getInstance(getContext());

        Button btn = view.findViewById(R.id.btnLogin);
        Button btnGoogleLogin = view.findViewById(R.id.btnGoogleLogin);
        Button btnGuestLogin = view.findViewById(R.id.btnGuestLogin);


        EditText etEmail = view.findViewById(R.id.etUsername);
        EditText etPassword = view.findViewById(R.id.etPassword);
        TextView tvEmailError = view.findViewById(R.id.tvEmailError);
        TextView tvPasswordError = view.findViewById(R.id.tvPasswordError);

        TextWatcher textWatcherPassword = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String email = ((EditText)view.findViewById(R.id.etUsername)).getText()
                        .toString().trim();
                String pass = ((EditText)(view.findViewById(R.id.etPassword))).getText()
                        .toString().trim();

                boolean isValid = validateEmail(email, tvEmailError) &&
                        validatePassword(pass, tvPasswordError);
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
                String email = ((EditText)view.findViewById(R.id.etUsername)).getText()
                        .toString().trim();
                String pass = ((EditText)(view.findViewById(R.id.etPassword))).getText()
                        .toString().trim();

                boolean isValid = validateEmail(email, tvEmailError) &&
                        validatePassword(pass, tvPasswordError);
                btn.setEnabled(isValid);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                        etEmail.addTextChangedListener(textWatcherEmail);
                } else {
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

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseRepository.loginWithEmailAndPassword(etEmail.getText().toString(),
                        etPassword.getText().toString(), new OnSuccessListener() {
                            @Override
                            public void onSuccess(Object o) {
                                Intent intent = new Intent(getActivity(), IntermediateActivity.class);
                                intent.putExtra("guest", false);
                                startActivity(intent);
                            }
                        }, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                LayoutInflater inflater = LayoutInflater.from(getContext());
                                View layout = inflater.inflate(R.layout.custom_toast, null);

                                TextView text = layout.findViewById(R.id.toast_text);
                                text.setText("Couldn't Log in" + e.getMessage());
                                Toast toast = new Toast(getContext());
                                toast.setDuration(Toast.LENGTH_SHORT);
                                toast.setView(layout);
                                toast.show();
                                btnGuestLogin.setVisibility(View.VISIBLE);
                            }
                        });
            }
        });

        Button btnForgotPassword = view.findViewById(R.id.btnForgotPassword);

        btnForgotPassword.setOnClickListener(v -> {
            // Create the EditText for email input
            EditText editTextEmail = new EditText(v.getContext());
            // Set the input type to email
            editTextEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

            // Build the AlertDialog
            AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
            passwordResetDialog.setTitle("Reset Password");
            passwordResetDialog.setMessage("Enter your email to receive a reset link.");
            passwordResetDialog.setView(editTextEmail);

            // Positive button action
            passwordResetDialog.setPositiveButton("Send", (dialog, which) -> {
                String email = editTextEmail.getText().toString().trim();
                if (!email.isEmpty()) {
                    // Call your password reset function
                    databaseRepository.forgotPassword(email, new OnSuccessListener() {
                        @Override
                        public void onSuccess(Object o) {
                            Toast.makeText(getContext(), "Reset link sent to your email.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG)
                                    .show();
                        }
                    });
                } else {
                    Toast.makeText(getContext(), "Please enter an email.",
                            Toast.LENGTH_SHORT).show();
                }
            });

            // Negative button action
            passwordResetDialog.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

            // Show the dialog
            passwordResetDialog.create().show();
        });

        ActivityResultLauncher<Intent> googleSignInLauncher;

        googleSignInLauncher = registerForActivityResult(new ActivityResultContracts
                .StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    databaseRepository.handleGoogleSignInResult(result.getData(),
                            new DatabaseRepository.GoogleSignInCallback() {
                        @Override
                        public void onSuccess() {
                            Intent intent = new Intent(getContext(), IntermediateActivity.class);
                            intent.putExtra("guest", false);
                            startActivity(intent);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            btnGuestLogin.setVisibility(View.VISIBLE);
                            Log.w("TAG", "Google sign-in failed", e);
                        }
                    });
                }
            }
        });

        btnGoogleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = databaseRepository.getGoogleSignInIntent(requireContext());
                googleSignInLauncher.launch(signInIntent);
            }
        });

        btnGuestLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), IntermediateActivity.class);
                intent.putExtra("guest", true);
                startActivity(intent);
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
        if (!email.contains("@") || !email.substring(email.indexOf("@")).contains(".com")) {
            tvEmailError.setText("Invalid Email");
            return false;
        }
        tvEmailError.setText("");
        return true;
    }

}