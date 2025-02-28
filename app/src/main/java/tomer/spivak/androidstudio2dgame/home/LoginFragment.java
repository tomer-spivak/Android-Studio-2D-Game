package tomer.spivak.androidstudio2dgame.home;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


import tomer.spivak.androidstudio2dgame.R;
import tomer.spivak.androidstudio2dgame.intermediate.IntermediateActivity;


public class LoginFragment extends Fragment {



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        Button btn = view.findViewById(R.id.btnLogin);

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
                mAuth.signInWithEmailAndPassword(etEmail.getText().toString(),
                                (etPassword.getText().toString()))
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                Intent intent = new Intent(getActivity(),
                                        IntermediateActivity.class);
                                startActivity(intent);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                LayoutInflater inflater = LayoutInflater.from(getContext());
                                View layout = inflater.inflate(R.layout.custom_toast, null); // Use null as the parent

                                TextView text = layout.findViewById(R.id.toast_text);
                                text.setText("Couldn't Log in"); // Set the text properly

                                Toast toast = new Toast(getContext());
                                toast.setDuration(Toast.LENGTH_SHORT);
                                toast.setView(layout);
                                toast.show();
                            }
                        })
                ;
            }
        });

        Button btnForgotPassword = view.findViewById(R.id.btnForgotPassword);

        btnForgotPassword.setOnClickListener(v -> {
            EditText editTextEmail = new EditText(v.getContext());
            AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
            passwordResetDialog.setTitle("Reset Password");
            passwordResetDialog.setMessage("Enter your email to receive a reset link.");
            passwordResetDialog.setView(editTextEmail);

            passwordResetDialog.setPositiveButton("Send", (dialog, which) -> {
                String email = editTextEmail.getText().toString().trim();
                if (!email.isEmpty()) {
                    FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                            .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Reset link sent to your email.", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                } else {
                    Toast.makeText(getContext(), "Please enter an email.", Toast.LENGTH_SHORT).show();
                }
            });

            passwordResetDialog.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

            passwordResetDialog.create().show();
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