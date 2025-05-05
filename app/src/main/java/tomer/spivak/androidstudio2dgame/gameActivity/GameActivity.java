package tomer.spivak.androidstudio2dgame.gameActivity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;



import tomer.spivak.androidstudio2dgame.helper.DatabaseRepository;
import tomer.spivak.androidstudio2dgame.gameManager.GameView;
import tomer.spivak.androidstudio2dgame.R;
import tomer.spivak.androidstudio2dgame.modelEnums.DifficultyLevel;
import tomer.spivak.androidstudio2dgame.modelEnums.GameStatus;
import tomer.spivak.androidstudio2dgame.music.SoundEffectManager;
import tomer.spivak.androidstudio2dgame.viewModel.GameViewModel;
import tomer.spivak.androidstudio2dgame.model.GameState;

public class GameActivity extends AppCompatActivity{
    private Context context;

    private GameView gameView;

    private GameViewModel viewModel;

    private SoundEffectManager soundEffectsManager;

    private  Button btnOpenMenu, btnStartGame, btnSkipRound;
    private  CardView cvSelectBuildingMenu;

    //helper class which does firebase operations
    private DatabaseRepository databaseRepository;

    private boolean gameIsOnGoing = false;

    //every time an enemy is defeated the firebase data gets incremented. so I need to check if there has been any change in
    // the number of enemies Defeated, and in that case increment the data in firebase.
    private int enemiesDefeatedCache = 0;

    //continue a game after exiting and saving it
    private boolean continueGame;

    //if the user chose to remove the save
    private boolean skipAutoSave = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);
        context = this;

        //alert dialog that blocks the screen
        AlertDialog dialogLoadingBoard = new AlertDialog.Builder(context).setView(R.layout.alert_dialog_loading_board).setCancelable(false).create();
        dialogLoadingBoard.show();

        //init views
        btnOpenMenu = findViewById(R.id.btnOpenMenu);
        btnStartGame = findViewById(R.id.btnStartGame);
        btnSkipRound = findViewById(R.id.btnSkipRound);
        Button btnPause = findViewById(R.id.btnPause);
        ImageButton btnCloseMenu = findViewById(R.id.btnCloseMenu);
        cvSelectBuildingMenu = findViewById(R.id.cvSelectBuildingMenu);

        int boardSize = 14;

        soundEffectsManager = new SoundEffectManager(this);

        //init game view
        LinearLayout gameLayout = findViewById(R.id.gameLinearLayout);
        gameView = new GameView(context, boardSize, this, soundEffectsManager);
        gameLayout.addView(gameView);

        databaseRepository = DatabaseRepository.getInstance(context);

        //init card view
        String[] buildingImages = { "obelisk", "lightningtower", "explodingtower" };
        RecyclerView buildingRecyclerView = findViewById(R.id.buildingRecyclerView);
        buildingRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        buildingRecyclerView.setAdapter(new BuildingsRecyclerViewAdapter(this, buildingImages,  this));


        viewModel = new ViewModelProvider(this).get(GameViewModel.class);

        //did the user continue a game or started a new one
        continueGame = getIntent().getBooleanExtra("isContinue", false);

        if(continueGame){
            //we need a listener that will tell us if the board was loaded, and if so get all the data.
            OnBoardLoadedListener listener = new OnBoardLoadedListener() {
                @Override
                public void onBoardLoaded(DocumentSnapshot documentSnapshot, DifficultyLevel finalDifficultyLevel, Long finalTimeSinceGameStart,
                                          int finalCurrentRound, int finalShnuzes, boolean finalDayTime) {
                    //init the board in the model
                    viewModel.initModelBoardWithDataFromDataBase(soundEffectsManager, documentSnapshot.getData(), boardSize, finalDifficultyLevel,
                            finalCurrentRound, finalShnuzes, finalTimeSinceGameStart, finalDayTime);
                    //loading finish
                    dialogLoadingBoard.dismiss();
                }
            };
            databaseRepository.loadCurrentGame(listener);
        } else {
            //start a new game, we can take get the difficulty selected
            DifficultyLevel difficultyLevel = DifficultyLevel.valueOf(getIntent().getStringExtra("difficultyLevel"));
            //init the board in the model with default values
            viewModel.initModelBoardWithDataFromDataBase(soundEffectsManager, null, boardSize, difficultyLevel,
                    1, -1, 0L, true);
            //loading finish
            dialogLoadingBoard.dismiss();
        }

        //start observing changes in model
        viewModel.getGameState().observe(this, new Observer<GameState>() {
            @Override
            public void onChanged(GameState gameState) {
                //update game view
                gameView.updateFromGameState(gameState);
                if (gameState.getGameStatus() == GameStatus.LOST) {
                    triggerDefeat();
                } else if (gameState.getGameStatus() == GameStatus.WON)  {
                    triggerVictory();
                }
                int enemiesDefeated = gameState.getEnemiesDefeated();
                if (enemiesDefeated == enemiesDefeatedCache + 1){
                    enemiesDefeatedCache = enemiesDefeated;
                    databaseRepository.incrementEnemiesDefeated();
                }
                //update UI
                if (gameState.isDayTime()) {
                    btnOpenMenu.setVisibility(View.VISIBLE);
                    //we need to check if the start game button appears.
                    // if it does the game hasn't started yet, and so we cant skip to the next round.
                    if (btnStartGame.getVisibility() == View.GONE)
                        btnSkipRound.setVisibility(View.VISIBLE);
                } else {
                    btnOpenMenu.setVisibility(View.GONE);
                    btnSkipRound.setVisibility(View.GONE);
                    cvSelectBuildingMenu.setVisibility(View.GONE);
                }
            }
        });

        btnStartGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if the game can be started (if there are any buildings) start the game
                if (viewModel.canStartGame()) {
                    gameIsOnGoing = true;
                    btnStartGame.setVisibility(View.GONE);
                    btnSkipRound.setVisibility(View.VISIBLE);

                    //if this is a new game update the database
                    if (!continueGame){
                        databaseRepository.incrementGamesPlayed();
                    }
                } else {
                    Toast.makeText(context, "In order to start a game\n" + "you need to build something",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        btnOpenMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cvSelectBuildingMenu.setVisibility(View.VISIBLE);
            }
        });

        btnCloseMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cvSelectBuildingMenu.setVisibility(View.GONE);
            }
        });

        btnSkipRound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.skipToNextRound();
                btnSkipRound.setVisibility(View.GONE);
            }
        });

        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float volume = gameView.getMusicService().getCurrentVolumeLevel();
                gameView.pauseGameLoop();

                LayoutInflater inflater = LayoutInflater.from(context);
                View dialogView = inflater.inflate(R.layout.dialog_pause, null);
                SeekBar volumeSeekBar = dialogView.findViewById(R.id.volumeSeekBar);
                volumeSeekBar.setProgress((int) volume);
                SeekBar soundEffectsSeekBar = dialogView.findViewById(R.id.soundEffectsSeekBar);
                soundEffectsSeekBar.setProgress(soundEffectsManager.getVolumeLevel());

                new AlertDialog.Builder(context).setTitle("Game Paused").setView(dialogView)
                        .setPositiveButton("resume", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                gameView.resumeGameLoop(volumeSeekBar.getProgress());
                                soundEffectsManager.setVolume(soundEffectsSeekBar.getProgress() / 100f);
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                GameState state = viewModel.getGameState().getValue();
                                if (state != null) {
                                    databaseRepository.saveBoard(state,
                                            new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    databaseRepository.logResults(viewModel);
                                                    gameView.stopGameLoop();
                                                    dialog.dismiss();
                                                    finish();
                                                }
                                            },
                                            new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(context, "Saving failed: " + e.getMessage(),
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                    );
                                } else {
                                    gameView.stopGameLoop();
                                    finish();
                                }
                            }
                        })
                        .setCancelable(false)
                        .show();
            }
        });
        //overiding the back button
        OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                int volume = gameView.getMusicService().getCurrentVolumeLevel();
                gameView.pauseGameLoop();

                LayoutInflater inflater = LayoutInflater.from(context);
                View dialogView = inflater.inflate(R.layout.alert_dialog_back, null);

                AlertDialog alertDialog = new AlertDialog.Builder(context).setView(dialogView).setCancelable(false).create();
                alertDialog.show();

                Button btnCancel = dialogView.findViewById(R.id.dialog_cancel);
                Button btnDiscardSave = dialogView.findViewById(R.id.dialog_exit_without_saving);
                Button btnSave = dialogView.findViewById(R.id.dialog_exit_with_saving);

                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                        gameView.resumeGameLoop(volume);
                    }
                });

                btnDiscardSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //logs in the leaderboard
                        databaseRepository.logResults(viewModel);
                        //removes the save
                        databaseRepository.removeBoard(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                alertDialog.dismiss();
                                gameView.stopGameLoop();
                                skipAutoSave = true;
                                finish();
                            }
                        });
                    }
                });

                btnSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                        GameState gameState = viewModel.getGameState().getValue();
                        if (gameState != null) {
                            databaseRepository.saveBoard(
                                    gameState,
                                    new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            databaseRepository.logResults(viewModel);
                                            gameView.stopGameLoop();
                                            Toast.makeText(context, "Game saved successfully", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                    },
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(context, "Failed to save game: " + e.getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                            gameView.stopGameLoop();
                                            finish();
                                        }
                                    }
                            );
                        } else {
                            gameView.stopGameLoop();
                            finish();
                        }
                    }
                });
            }
        };
        //assigns the callback
        getOnBackPressedDispatcher().addCallback(this, backPressedCallback);
    }

    private void triggerVictory() {
        databaseRepository.removeBoard(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                skipAutoSave = true;
                databaseRepository.logResults(viewModel);

                LayoutInflater inflater = LayoutInflater.from(context);
                View dialogView = inflater.inflate(R.layout.alert_dialog_won, null);

                AlertDialog alertDialog = new AlertDialog.Builder(context).setView(dialogView).setCancelable(false).create();

                Button btnExitApp = dialogView.findViewById(R.id.exit_app_btn);
                Button btnGoBack = dialogView.findViewById(R.id.go_back_btn);

                btnExitApp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                        gameView.stopGameLoop();
                        finishAffinity();
                        System.exit(0);
                    }
                });

                btnGoBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                        gameView.stopGameLoop();
                        if (context instanceof Activity) {
                            ((Activity) context).finish();
                        }
                    }
                });

                alertDialog.show();
            }
        });

    }

    private void triggerDefeat() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.alert_dialog_defeat, null);

        Button btnMenu = dialogView.findViewById(R.id.btnMenu);
        Button btnExit = dialogView.findViewById(R.id.btnExit);

        AlertDialog alertDialog = builder.setView(dialogView).setCancelable(false).create();

        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseRepository.logResults(viewModel);
                databaseRepository.removeBoard(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        alertDialog.dismiss();
                        finish();
                    }
                });
            }
        });
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseRepository.removeBoard(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        alertDialog.dismiss();
                        finishAffinity();
                        System.exit(0);
                    }
                });
            }
        });
        skipAutoSave = true;

        gameView.stopGameLoop();

        alertDialog.show();
    }

    public void closeBuildingMenu() {
        cvSelectBuildingMenu.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameView != null) {
            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            float volume = prefs.getFloat("volume", 0.07f) ;
            gameView.resumeGameLoop(volume * 100);
        }
    }

    @Override
    protected void onPause() {
        if (gameView != null) {
            gameView.pauseGameLoop();
        }
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        GameState gameState = viewModel.getGameState().getValue();

        if (gameState != null && !skipAutoSave) {
            databaseRepository.saveBoard(gameState, null, null);
        }

        if (gameView != null) {
            gameView.stopGameLoop();
        }

        if (soundEffectsManager != null) {
            soundEffectsManager.onDestroy();
        }

        super.onDestroy();
    }

    public void onCellClicked(int row, int col) {
        viewModel.onCellClicked(row, col);
    }

    public void onBuildingSelected(String buildingType) {
        viewModel.selectBuilding(buildingType);
    }

    public void updateGameState(long elapsedTime) {
        if (gameIsOnGoing)
            viewModel.tick(elapsedTime);
    }

}