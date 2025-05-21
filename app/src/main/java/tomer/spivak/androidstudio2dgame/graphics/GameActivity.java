package tomer.spivak.androidstudio2dgame.graphics;

import static tomer.spivak.androidstudio2dgame.projectManagement.DatabaseRepository.isOnline;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
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


import java.util.List;
import java.util.Map;

import tomer.spivak.androidstudio2dgame.projectManagement.GameObjectData;
import tomer.spivak.androidstudio2dgame.projectManagement.GameEventListener;
import tomer.spivak.androidstudio2dgame.projectManagement.DatabaseRepository;
import tomer.spivak.androidstudio2dgame.R;
import tomer.spivak.androidstudio2dgame.logic.Position;
import tomer.spivak.androidstudio2dgame.logic.modelEnums.DifficultyLevel;
import tomer.spivak.androidstudio2dgame.logic.modelEnums.GameStatus;
import tomer.spivak.androidstudio2dgame.projectManagement.MusicService;
import tomer.spivak.androidstudio2dgame.projectManagement.SoundEffectManager;
import tomer.spivak.androidstudio2dgame.projectManagement.GameViewModel;
import tomer.spivak.androidstudio2dgame.logic.GameState;

public class GameActivity extends AppCompatActivity implements GameEventListener, BuildingsRecyclerViewAdapter.OnBuildingClickListener {
    private Context context;

    private GameView gameView;


    private GameViewModel viewModel;

    private SoundEffectManager soundEffectsManager;

    private Button btnOpenMenu;
    private Button btnStartGame;
    private Button btnSkipRound;

    private CardView cvSelectBuildingMenu;

    private DatabaseRepository databaseRepository;

    private boolean gameIsOnGoing = false;
    //continue a game after exiting and saving it
    private boolean continueGame;

    //if the user chose to remove the save
    private boolean skipAutoSave = false;

    private Intent musicIntent;
    private ServiceConnection serviceConnection;
    private MusicService musicService;

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
        gameView = new GameView(context, boardSize, this);
        gameLayout.addView(gameView);

        databaseRepository = new DatabaseRepository(context);

        //init card view
        int[] buildingImagesRes = { R.drawable.obelisk, R.drawable.lightning_tower, R.drawable.exploding_tower};
        RecyclerView buildingRecyclerView = findViewById(R.id.buildingRecyclerView);
        buildingRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        buildingRecyclerView.setAdapter(new BuildingsRecyclerViewAdapter(this, buildingImagesRes,  this));

        viewModel = new ViewModelProvider(this).get(GameViewModel.class);

        //did the user continue a game or started a new one
        continueGame = getIntent().getBooleanExtra("isContinue", false);

        if(continueGame && isOnline(this)){
            databaseRepository.loadCurrentGame(new OnSuccessListener<Map<String, Object>>() {
                @Override
                public void onSuccess(Map<String, Object> data) {
                    Map<String,Object> boardData = (Map<String,Object>) data.get("boardData");
                    String diffName = (String) data.get("Difficulty Level");
                    DifficultyLevel difficulty = DifficultyLevel.MEDIUM;
                    if (diffName != null) {
                        difficulty = DifficultyLevel.valueOf(diffName);
                    }
                    long timeSinceStart = 0L;
                    Long timeObj = (Long) data.get("Time Since Game Start");
                    if (timeObj != null)
                        timeSinceStart = timeObj;
                    int currentRound = 1;
                    Long roundObj = (Long) data.get("Current Round");
                    if (roundObj != null)
                        currentRound = roundObj.intValue();
                    int shnuzes = -1;
                    Long shnuzesObj = (Long) data.get("Shnuzes");
                    if (shnuzesObj != null)
                        shnuzes = shnuzesObj.intValue();
                    boolean dayTime = Boolean.TRUE.equals(data.get("Is Day Time"));
                    //init the board in the model
                    viewModel.initModelBoardWithDataFromDataBase(soundEffectsManager, boardData, boardSize, difficulty, currentRound, shnuzes, timeSinceStart, dayTime);
                    //loading finish
                    dialogLoadingBoard.dismiss();
                }
            }, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //init the board in the model with default values
                    viewModel.initModelBoardWithDataFromDataBase(soundEffectsManager, null, boardSize, DifficultyLevel.MEDIUM,
                            1, -1, 0L, true);
                    Toast.makeText(context, "Failed to load game: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    //loading finish
                    dialogLoadingBoard.dismiss();
                }
            });
        }
        else {
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
                //update UI
                if (gameState.getDayTime()) {
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
                //update the game view (things related to the current state of the game - not the board)
                gameView.updateFromGameState(gameState);

                //check for game end
                if(gameState.getGameStatus() != GameStatus.PLAYING){
                    boolean userWon = gameState.getGameStatus() == GameStatus.WON;
                    soundEffectsManager.stopAllSoundEffects();
                    gameView.stopGameLoop();
                    skipAutoSave = true;
                    //if not online, dont try to save at all
                    if (isOnline(context)) {
                        int layout;
                        if (userWon)
                            layout = R.layout.alert_dialog_victory;
                        else
                            layout = R.layout.alert_dialog_defeat;

                        View view = LayoutInflater.from(context).inflate(layout, null);

                        view.findViewById(R.id.tvMessage).setVisibility(View.GONE);
                        view.findViewById(R.id.btnMenu).setVisibility(View.GONE);
                        view.findViewById(R.id.btnExit).setVisibility(View.GONE);

                        AlertDialog alertDialog = new AlertDialog.Builder(context).setView(view).setCancelable(false).create();
                        alertDialog.show();

                        databaseRepository.logMaxRound(viewModel.getRound(), context);
                        if (userWon) {
                            databaseRepository.incrementVictories(context);
                        }

                        Handler handler = new Handler(Looper.getMainLooper());
                        Runnable fallback = new Runnable() {
                            @Override
                            public void run() {
                                if (alertDialog.isShowing()) {
                                    if (!isFinishing() && !isDestroyed() && alertDialog.isShowing()) {
                                        alertDialog.dismiss();
                                        showEndGameDialog(userWon);
                                        databaseRepository.removeBoard(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                            }
                                        }, context);
                                    }
                                }
                            }
                        };
                        handler.postDelayed(fallback, 5_000);

                        databaseRepository.removeBoard(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                handler.removeCallbacks(fallback);
                                if (alertDialog.isShowing()) {
                                    alertDialog.dismiss();
                                    showEndGameDialog(userWon);
                                }
                            }
                        }, context);

                    } else {
                        showEndGameDialog(userWon);
                    }
                }
            }
        });

        viewModel.getChangedDelta().observe(this, new Observer<List<GameObjectData>>() {
            @Override
            public void onChanged(List<GameObjectData> gameObjectData) {
                //list of changed and new gameObjects (gets them in the class of GameObjectData,
                // which translates the model data types to something the view can use)
                gameView.applyChanged(gameObjectData);
            }
        });

        viewModel.getRemovedDelta().observe(this, new Observer<List<Position>>() {
            @Override
            public void onChanged(List<Position> positions) {
                //list of positions where the view needs to remove the gameObject
                //make the actual change
                gameView.applyRemoved(positions);
            }
        });

        viewModel.getEnemiesDefeatedDelta().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer delta) {
                databaseRepository.incrementEnemiesDefeated(delta, context);
            }
        });

        musicIntent = new Intent(this, MusicService.class);

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                musicService = ((MusicService.LocalBinder)binder).getService();
                float volumeLevel = getSharedPreferences("MyPrefs", MODE_PRIVATE).getFloat("volume", 0.07f) * 100;
                musicService.setVolumeLevel(volumeLevel);
            }
            @Override
            public void onServiceDisconnected(ComponentName name) {
                musicService = null;
            }
        };

        btnStartGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if the game can be started (if there are any buildings) start the game
                if (viewModel.canStartGame() || continueGame) {
                    gameIsOnGoing = true;
                    btnStartGame.setVisibility(View.GONE);
                    btnSkipRound.setVisibility(View.VISIBLE);
                    //if this is a new game update the database
                    if (!continueGame){
                        databaseRepository.incrementGamesPlayed(context);
                    }
                } else {
                    Toast.makeText(context, "In order to start a game\n" + "you need to build something", Toast.LENGTH_LONG).show();
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
                int volumeLevel;
                if (musicService == null)
                    volumeLevel = 7;
                else
                    volumeLevel = musicService.getCurrentVolumeLevel();

                gameView.pauseGameLoop();
                boolean oldGameIsOnGoing = gameIsOnGoing;
                gameIsOnGoing = false;
                LayoutInflater inflater = LayoutInflater.from(context);
                View dialogView = inflater.inflate(R.layout.alert_dialog_pause, null);
                SeekBar musicVolumeSeekBar = dialogView.findViewById(R.id.volumeSeekBar);
                musicVolumeSeekBar.setProgress(volumeLevel);
                SeekBar soundEffectsVolumeSeekBar = dialogView.findViewById(R.id.soundEffectsSeekBar);
                soundEffectsVolumeSeekBar.setProgress(soundEffectsManager.getVolumeLevel());
                new AlertDialog.Builder(context).setTitle("Game Paused").setView(dialogView)
                        .setPositiveButton("resume", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                gameIsOnGoing = oldGameIsOnGoing;
                                gameView.resumeGameLoop(musicVolumeSeekBar.getProgress());
                                musicService.setVolumeLevel(musicVolumeSeekBar.getProgress());
                                soundEffectsManager.setVolume(soundEffectsVolumeSeekBar.getProgress() / 100f);
                            }
                        })
                        .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                GameState state = viewModel.getGameState().getValue();
                                if (state != null) {
                                    databaseRepository.saveBoard(state, new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful())
                                                databaseRepository.logMaxRound(viewModel.getRound(), context);
                                            soundEffectsManager.stopAllSoundEffects();
                                            gameView.stopGameLoop();
                                            dialog.dismiss();
                                            finish();
                                        }
                                    }, context);
                                } else {
                                    soundEffectsManager.stopAllSoundEffects();
                                    gameView.stopGameLoop();
                                    dialog.dismiss();
                                    finish();
                                }
                            }
                        }).setCancelable(false).show();
            }
        });

        //overiding the back button
        OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                int volumeLevel = 7;
                if(musicService != null)
                    volumeLevel = musicService.getCurrentVolumeLevel();
                int finalVolumeLevel = volumeLevel;
                soundEffectsManager.pauseSoundEffects();
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
                        soundEffectsManager.resumeSoundEffects();
                        gameView.resumeGameLoop(finalVolumeLevel);
                    }
                });

                btnDiscardSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //logs in the leaderboard
                        databaseRepository.logMaxRound(viewModel.getRound(), context);
                        //removes the save
                        databaseRepository.removeBoard(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                alertDialog.dismiss();
                                soundEffectsManager.stopAllSoundEffects();
                                gameView.stopGameLoop();
                                skipAutoSave = true;
                                finish();
                            }
                        }, context);
                    }
                });

                btnSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                        GameState gameState = viewModel.getGameState().getValue();
                        if (gameState != null) {
                            databaseRepository.saveBoard(gameState,
                                    new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful())
                                                databaseRepository.logMaxRound(viewModel.getRound(), context);
                                            else
                                                Toast.makeText(context, "Failed to save game: " + task.getException(), Toast.LENGTH_LONG).show();
                                            soundEffectsManager.stopAllSoundEffects();
                                            gameView.stopGameLoop();
                                            finish();
                                        }
                                    }, context);

                        } else {
                            soundEffectsManager.stopAllSoundEffects();
                            gameView.stopGameLoop();
                            finish();
                        }
                    }
                });
            }
        };

        getOnBackPressedDispatcher().addCallback(this, backPressedCallback);
    }

    private void showEndGameDialog(boolean userWon) {
        int layout;
        if(userWon)
            layout = R.layout.alert_dialog_victory;
        else
            layout = R.layout.alert_dialog_defeat;

        View view = LayoutInflater.from(this).inflate(layout, null);
        AlertDialog alertDialog = new AlertDialog.Builder(this).setView(view).setCancelable(false).create();

        view.findViewById(R.id.progressBar).setVisibility(View.GONE);

        view.findViewById(R.id.btnExit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                finishAffinity();
                System.exit(0);
            }
        });
        view.findViewById(R.id.btnMenu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                finish();
            }
        });
        alertDialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        startForegroundService(musicIntent);
        bindService(musicIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameView != null) {
            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            float volume = prefs.getFloat("volume", 0.07f) * 100;
            soundEffectsManager.resumeSoundEffects();
            gameView.resumeGameLoop(volume);
        }
        if (musicService != null) {
            musicService.resumeMusic();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameView != null) {
            soundEffectsManager.pauseSoundEffects();
            gameView.pauseGameLoop();
        }
        if (musicService != null) {
            musicService.pauseMusic();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(musicService != null){
            unbindService(serviceConnection);
            musicService = null;
        }
    }

    @Override
    protected void onDestroy() {
        GameState gameState = viewModel.getGameState().getValue();

        if (gameState != null && !skipAutoSave) {
            databaseRepository.saveBoard(gameState, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                }
            }, context);
        }

        if (gameView != null) {
            soundEffectsManager.stopAllSoundEffects();
            gameView.stopGameLoop();
        }

        if (soundEffectsManager != null) {
            soundEffectsManager.onDestroy();
        }


        if (musicService != null) {
            unbindService(serviceConnection);
            musicService = null;
        }

        stopService(musicIntent);


        super.onDestroy();
    }

    @Override
    public void onTick(long deltaTime) {
        if(gameIsOnGoing)
            viewModel.tick(deltaTime);
    }

    public void onCellClicked(int row, int col) {
        viewModel.onCellClicked(row, col);
    }

    public void onBuildingSelected(String buildingType) {
        viewModel.selectBuilding(buildingType);
    }

    @Override
    public void onCloseBuildingMenu() {
        cvSelectBuildingMenu.setVisibility(View.GONE);
    }

    public MusicService getMusicService() {
        return musicService;
    }

    public SoundEffectManager getSoundEffectsManager() {
        return soundEffectsManager;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) {
            gameView.pauseGameLoop();
            if (musicService != null) {
                musicService.pauseMusic();
            }
        } else {
            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            float volume = prefs.getFloat("volume", 0.07f) * 100;
            gameView.resumeGameLoop(volume);
            if (musicService != null) {
                musicService.resumeMusic();
            }
        }
    }
}
