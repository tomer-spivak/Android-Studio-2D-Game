package tomer.spivak.androidstudio2dgame.home;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.internal.TextWatcherAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import tomer.spivak.androidstudio2dgame.R;
import tomer.spivak.androidstudio2dgame.helper.DatabaseRepository;
import tomer.spivak.androidstudio2dgame.intermediate.IntermediateActivity;

public class SignUpFragment extends Fragment {
    private DatabaseRepository repository;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private String email, password, username;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
                            Bitmap bmp = (Bitmap) data.getExtras().get("data");
                            if (bmp != null) {
                                imageUri = saveBitmapToFile(bmp);
                            }
                        }

                        repository.signUpWithEmailPassword(email, password, username, requireContext(), imageUri
                        );
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);
        repository = DatabaseRepository.getInstance(requireContext());

        EditText etEmail    = view.findViewById(R.id.etEmail);
        EditText etPassword = view.findViewById(R.id.etPassword);
        EditText etUsername = view.findViewById(R.id.etUsername);
        TextView tvEmailErr = view.findViewById(R.id.tvEmailError);
        TextView tvPassErr  = view.findViewById(R.id.tvPasswordError);
        TextView tvNameErr  = view.findViewById(R.id.tvUsernameError);
        Button   btnSignUp  = view.findViewById(R.id.btnSignUp);

        TextWatcher combinedWatcher = new TextWatcherAdapter() {
            @Override public void onTextChanged(CharSequence s, int i, int b, int c) {
                email    = etEmail.getText().toString().trim();
                password = etPassword.getText().toString().trim();
                username = etUsername.getText().toString().trim();
                boolean valid = validateEmail(email, tvEmailErr)
                        && validatePassword(password, tvPassErr)
                        && validateUsername(username, tvNameErr);
                btnSignUp.setEnabled(valid);
            }
        };
        etEmail.addTextChangedListener(combinedWatcher);
        etPassword.addTextChangedListener(combinedWatcher);
        etUsername.addTextChangedListener(combinedWatcher);

        btnSignUp.setOnClickListener(v -> showImageSourceChooser());

        return view;
    }

    private void showImageSourceChooser() {
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
                })
                .setCancelable(true)
                .show();
    }

    private boolean validateEmail(String email, TextView err) {
        if (!email.contains("@") || !email.substring(email.indexOf("@")).contains(".com")) {
            err.setText("Invalid Email");
            return false;
        }
        err.setText("");
        return true;
    }

    private boolean validatePassword(String pass, TextView err) {
        if (pass.length() < 6) {
            err.setText("Password must be at least 6 characters");
            return false;
        }
        err.setText("");
        return true;
    }

    private boolean validateUsername(String name, TextView err) {
        if (name.length() < 3) {
            err.setText("Username must be at least 3 characters");
            return false;
        }
        err.setText("");
        return true;
    }

    private Uri saveBitmapToFile(Bitmap bitmap) {
        try {
            File file = new File(
                    requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "signup_pic.jpg"
            );
            try (FileOutputStream fos = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            }
            return Uri.fromFile(file);
        } catch (IOException e) {
            Log.e("SignUpFragment", "Error saving camera image", e);
            return null;
        }
    }
}
