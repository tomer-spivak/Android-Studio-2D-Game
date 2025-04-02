package tomer.spivak.androidstudio2dgame.helper;

import static android.app.Activity.RESULT_OK;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageChooser {

    private final Context context;
    ActivityResultLauncher<Intent> takePictureLauncher;


    public ImageChooser(Fragment fragment, OnImageChosenListener listener) {
        this.context = fragment.getContext();
        takePictureLauncher = fragment.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK) {
                            Intent data = result.getData();
                            Uri imageUri = null;

                            if (data != null) {
                                // Handle image from gallery
                                if (data.getData() != null) {
                                    imageUri = data.getData();
                                }
                                // Handle image from camera
                                else if (data.getExtras() != null) {
                                    Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                                    if (bitmap != null) {
                                        imageUri = saveBitmapToFile(bitmap);
                                    }
                                }
                            }

                            if (imageUri != null) {
                                listener.onImageChosen(imageUri);
                            }
                        }
                    }
                }
        );
    }



    private Uri saveBitmapToFile(Bitmap bitmap) {
        try {
            File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "myImage.jpg");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            return Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureLauncher.launch(takePictureIntent);
    }


    public void openGallery() {
    }


    public interface OnImageChosenListener {
        void onImageChosen(Uri imageUri);
    }
}
