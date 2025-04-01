package tomer.spivak.androidstudio2dgame.gameActivity;

import static androidx.core.app.ActivityCompat.finishAffinity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Objects;

import tomer.spivak.androidstudio2dgame.R;
import tomer.spivak.androidstudio2dgame.gameManager.GameView;
import tomer.spivak.androidstudio2dgame.model.GameState;
import tomer.spivak.androidstudio2dgame.viewModel.GameViewModel;

public class DialogManager {
    Context context;
    DatabaseRepository databaseRepository;


    public DialogManager(Context context, DatabaseRepository databaseRepository) {
        this.context = context;
        this.databaseRepository = databaseRepository;
    }

    //checks if user wants to save his base
    public void showExitAlertDialog(GameViewModel viewModel) {
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
                if (viewModel.getGameState().getValue() == null ||
                        viewModel.getGameState().getValue().getGrid() == null)
                    return;

                saveBoard(viewModel, null);
            }
        });

        alertDialog.show();
    }

    public void saveBoard(GameViewModel viewModel, GameView gameView) {
        GameState gameState = viewModel.getGameState().getValue();
        if (gameState == null || gameState.getGrid() == null)
            return;
        Log.d("time", String.valueOf(gameState.getCurrentTimeOfGame()));
        databaseRepository.saveBoard(Objects.requireNonNull(gameState.getGrid()),
                gameState.getDifficulty().name(), gameState.getCurrentTimeOfGame(),
                new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                gameView.stopGameLoop();
                finish();
                Toast.makeText(context, "success", Toast.LENGTH_SHORT).show();
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("save", Objects.requireNonNull(e.getMessage()));
            }
        });
        databaseRepository.logResults(viewModel);
    }

    public void showPauseAlertDialog(GameView gameView, GameViewModel viewModel,
                                     float volume) {
        // Inflate the custom dialog layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_pause, null);
        // Get reference to the SeekBar and set an initial volume if needed
        SeekBar volumeSeekBar = dialogView.findViewById(R.id.volumeSeekBar);
        volumeSeekBar.setProgress((int) volume); // currentVolumeLevel should be defined based on your app logic


        Log.d("music", "volume: " + volume);
        // Build and show the AlertDialog with the custom view
        new AlertDialog.Builder(context)
                .setTitle("Game Paused")
                .setView(dialogView)
                .setPositiveButton("Resume", (dialog, which) -> {
                    Log.d("music", "v:" + volumeSeekBar.getProgress());
                    gameView.resumeGameLoop(volumeSeekBar.getProgress());
                    dialog.dismiss();
                })
                .setNegativeButton("Exit", (dialog, which) -> {
                    saveBoard(viewModel, gameView);
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

    public void showLostAlertDialog(GameViewModel viewModel, GameView gameView) {
        Log.d("debug", "tried to save");
        databaseRepository.logResults(viewModel);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Title")
                .setMessage("This is an alert dialog.")
                .setPositiveButton("Go back to menu", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("Exit App", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        gameView.stopGameLoop();
                        finishAffinity((Activity) context);
                        System.exit(0);
                    }
                });
        builder.setCancelable(false);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void finish() {
        if (context instanceof Activity) {
            ((Activity) context).finish();
        }
    }
}
