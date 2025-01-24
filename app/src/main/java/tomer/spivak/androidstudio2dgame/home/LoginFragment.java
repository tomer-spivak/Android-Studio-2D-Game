package tomer.spivak.androidstudio2dgame.home;


import androidx.annotation.NonNull;
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

        EditText etUsername = view.findViewById(R.id.etUsername);
        EditText etPassword = view.findViewById(R.id.etPassword);
        TextView tvEmailError = view.findViewById(R.id.tvEmailError);
        TextView tvPasswordError = view.findViewById(R.id.tvPasswordError);


        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String email = ((EditText)view.findViewById(R.id.etUsername)).getText()
                        .toString().trim();
                String pass = ((EditText)(view.findViewById(R.id.etPassword))).getText()
                        .toString().trim();

                boolean isValid = validateEmail(email) && validatePassword(pass);
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
                if (!email.contains("@") || !email.substring(email.indexOf("@")).contains(".com")) {
                    tvEmailError.setText("Invalid Email");
                    return false;
                }
                tvEmailError.setText("");
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
                mAuth.signInWithEmailAndPassword(etUsername.getText().toString(), (etPassword.getText().toString()))
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                Intent intent = new Intent(getActivity(), IntermediateActivity.class);
                                startActivity(intent);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getContext(), "Failure: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        })
                ;
            }
        });

        return view;
    }

}