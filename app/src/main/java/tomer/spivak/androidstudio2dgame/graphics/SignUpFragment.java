package tomer.spivak.androidstudio2dgame.graphics;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import tomer.spivak.androidstudio2dgame.R;
import tomer.spivak.androidstudio2dgame.projectManagement.DatabaseRepository;
public class SignUpFragment extends Fragment {
    private DatabaseRepository repository;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private String email;
    private String password;
    private String username;
    private ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);
        repository = new DatabaseRepository(requireContext());
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() != Activity.RESULT_OK) return;
                        Intent data = result.getData();
                        Uri imageUri = null;
                        if (data != null && data.getData() != null) {
                            imageUri = data.getData();
                        }
                        else if (data != null && data.getExtras() != null) {
                            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                            if (bitmap != null) {
                                try {
                                    File file = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "signup_pic.jpg");
                                    try (FileOutputStream fos = new FileOutputStream(file)) {
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                                    }
                                    imageUri = Uri.fromFile(file);
                                } catch (IOException ignored){}
                            }
                        }
                        repository.signUpWithEmailPassword(email, password, username, requireContext(), imageUri, progressBar);
                    }
                }
        );
        EditText etEmail = view.findViewById(R.id.etEmail);
        EditText etPassword = view.findViewById(R.id.etPassword);
        EditText etUsername = view.findViewById(R.id.etUsername);
        TextView tvEmailErr = view.findViewById(R.id.tvEmailError);
        TextView tvPassErr = view.findViewById(R.id.tvPasswordError);
        TextView tvNameErr = view.findViewById(R.id.tvUsernameError);
        Button btnSignUp = view.findViewById(R.id.btnSignUp);
        progressBar = view.findViewById(R.id.progressBar);

        TextWatcher combinedWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                email = etEmail.getText().toString().trim();
                password = etPassword.getText().toString().trim();
                username = etUsername.getText().toString().trim();
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    tvEmailErr.setText("Invalid Email");
                    btnSignUp.setEnabled(false);
                    return;
                } else
                    tvEmailErr.setText("");

                if (password.length() < 6){
                    tvPassErr.setText("Password must be at least 6 characters");
                    btnSignUp.setEnabled(false);
                    return;
                } else
                    tvPassErr.setText("");

                if (username.length() < 3) {
                    tvNameErr.setText("Username must be at least 3 characters");
                    btnSignUp.setEnabled(false);
                    return;
                } else
                    tvNameErr.setText("");
                btnSignUp.setEnabled(true);
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        etEmail.addTextChangedListener(combinedWatcher);
        etPassword.addTextChangedListener(combinedWatcher);
        etUsername.addTextChangedListener(combinedWatcher);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                new AlertDialog.Builder(requireContext()).setTitle("Choose Profile Image")
                        .setItems(new String[]{"Camera", "Gallery"}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent;
                                if (which == 0) {
                                    intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                } else {
                                    intent = new Intent(Intent.ACTION_GET_CONTENT)
                                            .setType("image/*");
                                }
                                pickImageLauncher.launch(intent);
                            }
                        }).setCancelable(true).show();
            }
        });
        return view;
    }
}
