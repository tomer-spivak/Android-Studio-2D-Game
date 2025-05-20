package tomer.spivak.androidstudio2dgame.graphics;


import static android.app.Activity.RESULT_OK;

import static tomer.spivak.androidstudio2dgame.projectManagement.DatabaseRepository.isOnline;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;


import tomer.spivak.androidstudio2dgame.R;
import tomer.spivak.androidstudio2dgame.projectManagement.DatabaseRepository;


public class LoginFragment extends Fragment {
    private DatabaseRepository databaseRepository;
    private Button btnGuestLogin;
    private  ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        googleSignInLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    databaseRepository.handleGoogleSignInResult(result.getData(), new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            btnGuestLogin.setVisibility(View.VISIBLE);
                        }
                    }, getContext());
                }
            }
        });

        databaseRepository = new DatabaseRepository(getContext());

        Button btnLogin = view.findViewById(R.id.btnLogin);
        Button btnGoogleLogin = view.findViewById(R.id.btnGoogleLogin);
        btnGuestLogin = view.findViewById(R.id.btnGuestLogin);
        Button btnForgotPassword = view.findViewById(R.id.btnForgotPassword);
        EditText etEmail = view.findViewById(R.id.etUsername);
        EditText etPassword = view.findViewById(R.id.etPassword);
        TextView tvEmailError = view.findViewById(R.id.tvEmailError);
        TextView tvPasswordError = view.findViewById(R.id.tvPasswordError);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String email = etEmail.getText().toString().trim();
                String pass = etPassword.getText().toString().trim();
                boolean isValid = validateEmail(email, tvEmailError) && validatePassword(pass, tvPasswordError);
                btnLogin.setEnabled(isValid);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };

        etEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                        etEmail.addTextChangedListener(textWatcher);
                } else {
                    validateEmail(etEmail.getText().toString(), tvEmailError);
                }
            }
        });

        etPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    etPassword.addTextChangedListener(textWatcher);
                } else {
                    validatePassword(etPassword.getText().toString(), tvPasswordError);
                }
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseRepository.loginWithEmailAndPassword(etEmail.getText().toString(), etPassword.getText().toString(), new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                LayoutInflater inflater = LayoutInflater.from(getContext());
                                View layout = inflater.inflate(R.layout.custom_toast, null);
                                TextView toastText = layout.findViewById(R.id.toast_text);
                                toastText.setText("Couldn't Log in: " + e.getMessage());
                                Toast toast = new Toast(getContext());
                                toast.setView(layout);
                                toast.show();
                                btnGuestLogin.setVisibility(View.VISIBLE);
                            }
                        }, getContext());
            }
        });

        btnForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                LayoutInflater inflater = LayoutInflater.from(context);
                View dialogView = inflater.inflate(R.layout.alert_dialog_reset_password, null, false);
                EditText etEmail = dialogView.findViewById(R.id.et_reset_email);
                AlertDialog dialog = new AlertDialog.Builder(context).setTitle("Reset Password").setView(dialogView)
                        .setPositiveButton("Send", null)
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create();
                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String email = etEmail.getText().toString().trim();
                                if (email.isEmpty()) {
                                    etEmail.setError("Please enter an email.");
                                } else {
                                    databaseRepository.forgotPassword(email, getContext());
                                    dialog.dismiss();
                                }
                            }
                        });
            }
        });

        btnGoogleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isOnline(requireContext())) {
                    Toast.makeText(getContext(), "No internet – signing in as guest", Toast.LENGTH_SHORT).show();
                    btnGuestLogin.setVisibility(View.VISIBLE);
                    return;
                }
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(requireContext().getString(R.string.default_web_client_id)).requestEmail().build();
                GoogleSignInClient client = GoogleSignIn.getClient(requireContext(), gso);
                client.signOut();
                Intent signInIntent = client.getSignInIntent();
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
        boolean valid = Patterns.EMAIL_ADDRESS.matcher(email).matches();
        if (!valid) {
            tvEmailError.setText("Invalid Email");
            return false;
        }
        tvEmailError.setText("");
        return true;
    }

}