

package tomer.spivak.androidstudio2dgame.gameActivity;




import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Objects;


import tomer.spivak.androidstudio2dgame.helper.DatabaseRepository;
import tomer.spivak.androidstudio2dgame.modelObjects.ModelObject;
import tomer.spivak.androidstudio2dgame.gameManager.GameView;
import tomer.spivak.androidstudio2dgame.R;
import tomer.spivak.androidstudio2dgame.model.Cell;
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

    private  Button btnOpenMenu;
    private  Button btnStartGame;
    private  Button btnSkipRound;
    private  CardView cvSelectBuildingMenu;


    private DatabaseRepository databaseRepository;


    private boolean gameIsOnGoing = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);
        context = this;


         btnOpenMenu = findViewById(R.id.btnPopUpMenu);
           btnStartGame = findViewById(R.id.btnStartGame);
           btnSkipRound = findViewById(R.id.btnSkipRound);
        Button btnPause = findViewById(R.id.btnPause);
        ImageButton btnCloseMenu = findViewById(R.id.btnCloseMenu);
           cvSelectBuildingMenu = findViewById(R.id.cvSelectBuildingMenu);




        int boardSize = 14;

        LinearLayout gameLayout = findViewById(R.id.gameLinearLayout);

        soundEffectsManager = new SoundEffectManager(this);

        gameView = new GameView(context, boardSize, this, soundEffectsManager);

        gameLayout.addView(gameView);

        viewModel = new ViewModelProvider(this).get(GameViewModel.class);

        databaseRepository = DatabaseRepository.getInstance(context);



        observeViewModel();
        initPlacingBuilding();




        String difficultyName = getIntent().getStringExtra("difficultyLevel");
        boolean isContinue = getIntent().getBooleanExtra("isContinue", false);
        BoardMapper boardMapper;
        final DifficultyLevel[] difficulty = new DifficultyLevel[1];
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Loading board")
                .setMessage("Please wait...")
                .setCancelable(false)
                .create();
        dialog.show();
        if (!isContinue){
            difficulty[0] = DifficultyLevel.valueOf(difficultyName);
            boardMapper = new BoardMapper(boardSize, difficulty[0]);
            boardMapper.initBoard();
            initBoardInViewModel(boardMapper.getBoard(), dialog, difficulty[0], 0L, 1, -1);
        } else {
            boardMapper = new BoardMapper(boardSize);
            databaseRepository.loadDataFromDataBase(boardMapper, new OnBoardLoadedListener() {
                @Override
                public void onBoardLoaded(Cell[][] board) {
                    Toast.makeText(context, "got board", Toast.LENGTH_SHORT).show();
                    difficulty[0] = boardMapper.getDifficulty();
                    Long timeSinceStartOfGame = boardMapper.getTimeSinceStartOfGame();
                    int currentRound = boardMapper.getCurrentRound();
                    int shnuzes = boardMapper.getShnuzes();
                    initBoardInViewModel(boardMapper.getBoard(), dialog, difficulty[0],
                            timeSinceStartOfGame, currentRound, shnuzes);
                }
            });
        }


        btnStartGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canStartGame()) {
                    gameIsOnGoing = true;
                    btnStartGame.setVisibility(View.GONE);
                    btnSkipRound.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(context, "In order to start a game\n" +
                            "you need to build something", Toast.LENGTH_LONG).show();
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
                Log.d("music", "volume: " + volume);
                gameView.pauseGameLoop();

                LayoutInflater inflater = LayoutInflater.from(context);
                View dialogView = inflater.inflate(R.layout.dialog_pause, null);
                SeekBar volumeSeekBar = dialogView.findViewById(R.id.volumeSeekBar);
                volumeSeekBar.setProgress((int) volume);
                SeekBar soundEffectsSeekBar = dialogView.findViewById(R.id.soundEffectsSeekBar);
                soundEffectsSeekBar.setProgress(soundEffectsManager.getVolumeLevel());

                new AlertDialog.Builder(context)
                        .setTitle("Game Paused")
                        .setView(dialogView)
                        .setPositiveButton("Resume", (d, w) -> {
                            gameView.resumeGameLoop(volumeSeekBar.getProgress());
                            soundEffectsManager.setVolume(soundEffectsSeekBar.getProgress() / 100f);
                            d.dismiss();
                        })
                        .setNegativeButton("Exit", (d, w) -> {
                            // 1) Dismiss UI
                            d.dismiss();

                            // 2) Always stop the game loop and finish


                            // 3) Then *attempt* to save in the background
                            GameState state = viewModel.getGameState().getValue();
                            if (state != null && state.getGrid() != null) {
                                databaseRepository.saveBoard(
                                        state.getGrid(),
                                        state.getDifficulty().name(),
                                        state.getCurrentTimeOfGame(),
                                        state.getCurrentRound(),
                                        state.getShnuzes(),
                                        /* onSuccess */ unused -> { /* no‑op */ },
                                        /* onFailure */ e -> Log.w("DBG", "offline save failed", e)
                                );

                                databaseRepository.logResults(viewModel);
                            }

                            gameView.stopGameLoop();
                            finish();
                        })
                        .setCancelable(false)
                        .show();
            }
        });





        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)  {
            /* enabled by default */
            //
            OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true /* enabled by default */) {
                @Override
                public void handleOnBackPressed() {

                    int volume = gameView.getMusicService().getCurrentVolumeLevel();
                    gameView.pauseGameLoop();      // always stop immediately
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
                            gameView.resumeGameLoop(volume);
                        }
                    });

                    btnDontSave.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(!databaseRepository.isGuest())
                            {
                                databaseRepository.logResults(viewModel);
                                databaseRepository.removeBoard();
                            }
                            alertDialog.dismiss();

                            gameView.stopGameLoop();

                            finish();
                        }
                    });

                    btnSave.setOnClickListener(v -> {
                        alertDialog.dismiss();
                        gameView.stopGameLoop();


                        FirebaseUser u = databaseRepository.getUserInstance();
                        // 1) offline? bail immediately
                        if (!isNetworkAvailable(context)) {
                            Toast.makeText(context, "No network, exiting as guest", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                        // 2) guest? just exit
                        if (u == null || u.isAnonymous()) {
                            finish();
                        }
                        // 3) real user & online? save to Firestore
                        else {
                            saveBoard(viewModel, gameView, context);
                        }
                    });

                    alertDialog.show();
                }
            };
            getOnBackPressedDispatcher().addCallback(this, backPressedCallback);
        }

    }
    public static boolean isNetworkAvailable(Context ctx) {
        ConnectivityManager cm =
                (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnected();
    }


    public void saveBoard(GameViewModel viewModel, GameView gameView, Context context) {
        GameState gameState = viewModel.getGameState().getValue();
        if (gameState == null || gameState.getGrid() == null)
            return;
        Log.d("time", String.valueOf(gameState.getCurrentTimeOfGame()));
        databaseRepository.saveBoard(Objects.requireNonNull(gameState.getGrid()),
                gameState.getDifficulty().name(), gameState.getCurrentTimeOfGame(), gameState.getCurrentRound(),
                gameState.getShnuzes(),
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
                        Toast.makeText(context, "failed", Toast.LENGTH_SHORT).show();
                        gameView.stopGameLoop();
                        finish();
                    }
                });
        databaseRepository.logResults(viewModel);
    }

    public void closeBuildingMenu() {
        cvSelectBuildingMenu.setVisibility(View.GONE);
    }





    private boolean canStartGame() {
        return viewModel.canStartGame();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (gameView != null) {
            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            float volume = prefs.getFloat("volume", 0.07f) ;
            Log.d("volume", "get" +volume);
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
        super.onDestroy();
        if (gameView != null) {
            gameView.stopGameLoop();
        }
        if (soundEffectsManager != null) {
            soundEffectsManager.onDestroy();
        }
    }








    private void initBoardInViewModel(Cell[][] board, AlertDialog dialog, DifficultyLevel difficulty, Long timeSinceStartOfGame, int currentRound, int shnuzes) {
        gameView.updateBoard(board);
        viewModel.initBoard(board.clone(), difficulty, currentRound, shnuzes);
        viewModel.tick(timeSinceStartOfGame);
        dialog.dismiss();
        viewModel.setSoundEffects(soundEffectsManager);
    }

    private void initPlacingBuilding() {
        ArrayList<String> buildingImagesURL = new ArrayList<>();
        String obelisk = "obelisk";
        String archerTower = "lightning0tower";
        buildingImagesURL.add(obelisk);
        buildingImagesURL.add(archerTower);
        BuildingsRecyclerViewAdapter adapter = new BuildingsRecyclerViewAdapter(context, buildingImagesURL, this);
        RecyclerView buildingRecyclerView = findViewById(R.id.buildingRecyclerView);
        buildingRecyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false));
        buildingRecyclerView.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getGameState().observe(this, new Observer<GameState>() {
            @Override
            public void onChanged(GameState gameState) {
                gameView.unpackGameState(gameState);
                if (gameState.getGameStatus() == GameStatus.LOST) {
                    Toast.makeText(context, "You Lost", Toast.LENGTH_SHORT).show();
                    databaseRepository.removeBoard();
                    Log.d("debug", "tried to save");
                    if(!databaseRepository.isGuest())
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
                                    finishAffinity();
                                    System.exit(0);
                                }
                            });
                    builder.setCancelable(false);
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                } else if (gameState.getGameStatus() == GameStatus.WON)  {
                    Toast.makeText(context, "You Won", Toast.LENGTH_SHORT).show();
                    databaseRepository.removeBoard();
                    if (!databaseRepository.isGuest())
                        databaseRepository.logResults(viewModel);

                    LayoutInflater inflater = LayoutInflater.from(context);
                    View dialogView = inflater.inflate(R.layout.alert_dialog_won, null);

                    AlertDialog alertDialog = new AlertDialog.Builder(context)
                            .setView(dialogView)
                            .setCancelable(false)
                            .create();

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
                if (gameState.getTimeOfDay()) {
                    if (btnOpenMenu.getVisibility() == View.GONE)
                        btnOpenMenu.setVisibility(View.VISIBLE);
                    if (btnStartGame.getVisibility() == View.GONE && btnSkipRound.getVisibility() != View.VISIBLE) {
                        btnSkipRound.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (cvSelectBuildingMenu.getVisibility() == View.VISIBLE)
                        cvSelectBuildingMenu.setVisibility(View.GONE);
                    if (btnOpenMenu.getVisibility() != View.GONE)
                        btnOpenMenu.setVisibility(View.GONE);
                    if (btnSkipRound.getVisibility() == View.VISIBLE) {
                        btnSkipRound.setVisibility(View.GONE);
                    }
                }


                checkDeadObjects(gameState);
            }
        });
    }


    private void checkDeadObjects(GameState gameState) {
        Cell[][] grid = gameState.getGrid();
        for (Cell[] row : grid) {
            for (Cell cell : row) {
                ModelObject object = cell.getObject();
                if (object == null)
                    continue;
                boolean dead = object.getHealth() <= 0;
                Log.d("dead", "Object health: " + object.getHealth());
                Log.d("dead", "Object is dead: " + dead);
            }
        }
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