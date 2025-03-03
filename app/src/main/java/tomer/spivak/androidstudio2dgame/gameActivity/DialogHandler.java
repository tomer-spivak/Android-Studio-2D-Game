package tomer.spivak.androidstudio2dgame.gameActivity;

import static androidx.core.app.ActivityCompat.finishAffinity;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Objects;

import tomer.spivak.androidstudio2dgame.FirebaseRepository;
import tomer.spivak.androidstudio2dgame.R;
import tomer.spivak.androidstudio2dgame.gameManager.GameView;
import tomer.spivak.androidstudio2dgame.viewModel.GameViewModel;

public class DialogHandler {
    Context context;
    FirebaseRepository firebaseRepository;
    GameViewModel viewModel;

    public DialogHandler(Context context, FirebaseRepository firebaseRepository,
                         GameViewModel viewModel) {
        this.context = context;
        this.firebaseRepository = firebaseRepository;
        this.viewModel = viewModel;
    }

    //checks if user wants to save his base
    public void showExitAlertDialog() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.alert_dialog, null);

        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        Button btnCancel = dialogView.findViewById(R.id.dialog_cancel);
        Button btnDontSave = dialogView.findViewById(R.id.dialog_exit_without_saving);
        Button btnSave = dialogView.findViewById(R.id.dialog_exit_with_saving);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        btnDontSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                finish();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                firebaseRepository.saveBoard(Objects.requireNonNull(viewModel.getGameState().
                        getValue()).getGrid(), new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        finish();
                        Toast.makeText(context, "success", Toast.LENGTH_SHORT).show();
                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
            }
        });

        alertDialog.show();
    }

    public void showPauseAlertDialog(GameView gameView) {
        new AlertDialog.Builder(context)
                .setTitle("Game Paused")
                .setPositiveButton("Resume", (dialog, which) -> {
                    gameView.resumeGameLoop();
                    dialog.dismiss();
                })
                .setNegativeButton("Exit", (dialog, which) -> {
                    gameView.stopGameLoop();
                    finish();
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
    }

    public AlertDialog showLoadingBoardAlertDialog() {
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Loading board")
                .setMessage("Please wait...")
                .setCancelable(false) // Prevents dismissal by tapping outside or pressing back
                .create();
        dialog.show();
        return dialog;
    }



    public void showLostAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Title")
                .setMessage("This is an alert dialog.")
                .setPositiveButton("Go back to menu", (dialog, which) -> {
                    finish();
                })
                .setNegativeButton("Exit App", (dialog, which) -> {
                    finishAffinity((Activity) context);
                    System.exit(0);
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void finish() {
        if (context instanceof Activity) {
            ((Activity) context).finish();
        }
    }
}
