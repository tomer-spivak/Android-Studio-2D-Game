package tomer.spivak.androidstudio2dgame.home;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import tomer.spivak.androidstudio2dgame.R;
import tomer.spivak.androidstudio2dgame.intermediate.IntermediateActivity;


public class SignUpFragment extends Fragment {



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        Button btn = view.findViewById(R.id.btnSignUp);

        EditText etEmail = view.findViewById(R.id.etEmail);
        EditText etPassword = view.findViewById(R.id.etPassword);
        EditText etUsername = view.findViewById(R.id.etUsername);
        TextView tvEmailError = view.findViewById(R.id.tvEmailError);
        TextView tvPasswordError = view.findViewById(R.id.tvPasswordError);
        TextView tvUsernameError = view.findViewById(R.id.tvUsernameError);


        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String username = ((EditText)view.findViewById(R.id.etUsername)).getText()
                        .toString().trim();
                String pass = ((EditText)(view.findViewById(R.id.etPassword))).getText()
                        .toString().trim();
                String email = ((EditText)(view.findViewById(R.id.etEmail))).getText().toString().trim();
                boolean isValid = validateUsername(username) && validateEmail(email) && validatePassword(pass);
                btn.setEnabled(isValid);
            }

            private boolean validatePassword(String pass) {
                if (pass.length() < 6){
                    tvPasswordError.setText("Password must be at least 6 characters");
                    return false;
                }
                tvPasswordError.setText("");
                return true;
            }

            private boolean validateEmail(String email) {
                if (!email.contains("@") || !email.substring(email.indexOf("@")).contains(".com")){
                    tvEmailError.setText("Invalid Email");
                    return false;
                }
                tvEmailError.setText("");

                return true;
            }
            private boolean validateUsername(String username) {
                tvUsernameError.setText("");
                return true;
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etUsername.addTextChangedListener(textWatcher);
        etPassword.addTextChangedListener(textWatcher);


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();
                String displayName = etUsername.getText().toString(); // Assuming you have an EditText for the display name

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    // Set the display name
                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                            .setDisplayName(displayName) // Set the user's display name
                                            .build();

                                    user.updateProfile(profileUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(getContext(), "You signed up successfully!", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(getActivity(), IntermediateActivity.class);
                                            startActivity(intent);

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getContext(), "Failed to set display name: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getContext(), "Failure: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });





        return view;
    }
}