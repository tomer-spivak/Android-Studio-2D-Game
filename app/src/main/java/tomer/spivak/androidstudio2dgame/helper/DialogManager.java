package tomer.spivak.androidstudio2dgame.helper;

import static androidx.core.app.ActivityCompat.finishAffinity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;


import java.util.Objects;

import tomer.spivak.androidstudio2dgame.R;
import tomer.spivak.androidstudio2dgame.gameActivity.GameActivity;
import tomer.spivak.androidstudio2dgame.gameManager.GameView;
import tomer.spivak.androidstudio2dgame.model.GameState;
import tomer.spivak.androidstudio2dgame.modelEnums.DifficultyLevel;
import tomer.spivak.androidstudio2dgame.music.SoundEffects;
import tomer.spivak.androidstudio2dgame.viewModel.GameViewModel;

public class DialogManager {
    DatabaseRepository databaseRepository;
    private static DialogManager instance;


    private DialogManager(DatabaseRepository databaseRepository) {
        this.databaseRepository = databaseRepository;
    }

    public static synchronized DialogManager getInstance(DatabaseRepository databaseRepository) {
        if (instance == null) {
            instance = new DialogManager(databaseRepository);
        } else {
            instance.databaseRepository = databaseRepository;
        }
        return instance;
    }

    public void showImagePickerDialog(ImageChooser imageChooser, Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select Image")
                .setItems(new CharSequence[]{"Take Photo", "Choose from Gallery"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            imageChooser.takePhoto();
                        } else if (which == 1) {
                            imageChooser.openGallery();
                        }
                    }
                })
                .show();
    }


    //checks if user wants to save his base
    public void showExitAlertDialog(GameViewModel viewModel, Context context) {
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
                finish(context);
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alertDialog.dismiss();
                if (viewModel.getGameState().getValue() == null ||
                        viewModel.getGameState().getValue().getGrid() == null)
                    return;

                saveBoard(viewModel, null, context);
            }
        });

        alertDialog.show();
    }

    public void saveBoard(GameViewModel viewModel, GameView gameView, Context context) {
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
                finish(context);
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

    public void showPauseAlertDialog(GameView gameView, GameViewModel viewModel, float volume, SoundEffects soundEffects, Context context) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_pause, null);
        SeekBar volumeSeekBar = dialogView.findViewById(R.id.volumeSeekBar);
        volumeSeekBar.setProgress((int) volume); // currentVolumeLevel should be defined based on your app logic

        SeekBar soundEffectsSeekBar = dialogView.findViewById(R.id.soundEffectsSeekBar);
        soundEffectsSeekBar.setProgress(soundEffects.getVolumeLevel());


        Log.d("music", "volume: " + volume);
        new AlertDialog.Builder(context)
                .setTitle("Game Paused")
                .setView(dialogView)
                .setPositiveButton("Resume", (dialog, which) -> {
                    Log.d("music", "v:" + volumeSeekBar.getProgress());
                    gameView.resumeGameLoop(volumeSeekBar.getProgress());
                    soundEffects.setVolume(soundEffectsSeekBar.getProgress() / 100f);
                    dialog.dismiss();
                })
                .setNegativeButton("Exit", (dialog, which) -> {
                    saveBoard(viewModel, gameView, context);
                    dialog.dismiss();

                })
                .setCancelable(false)
                .show();
    }


    public AlertDialog showLoadingBoardAlertDialog(Context context) {
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Loading board")
                .setMessage("Please wait...")
                .setCancelable(false)
                .create();
        dialog.show();
        return dialog;
    }

    public void showLostAlertDialog(GameViewModel viewModel, GameView gameView, Context context) {
        Log.d("debug", "tried to save");
        databaseRepository.logResults(viewModel);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Title")
                .setMessage("This is an alert dialog.")
                .setPositiveButton("Go back to menu", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish(context);
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

    public void showDifficultyAlertDialog(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.alert_dialog_difficulty, null);

        RadioGroup group = dialogView.findViewById(R.id.difficultyGroup);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        Button continueButton = dialogView.findViewById(R.id.continueButton);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        continueButton.setOnClickListener(v -> {
            DifficultyLevel selected = DifficultyLevel.EASY;
            int checkedId = group.getCheckedRadioButtonId();
            if (checkedId == R.id.normal) {
                selected = DifficultyLevel.MEDIUM;
            } else if (checkedId == R.id.hard) {
                selected = DifficultyLevel.HARD;
            }
            Intent intent = new Intent(context, GameActivity.class);
            intent.putExtra("difficultyLevel", selected.name());
            intent.putExtra("isContinue", false);
            context.startActivity(intent);
            dialog.dismiss();
        });

        dialog.show();

    }


    private void finish(Context context) {
        if (context instanceof Activity) {
            ((Activity) context).finish();
        }
    }

}
