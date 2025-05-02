

package tomer.spivak.androidstudio2dgame.gameActivity;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;




import tomer.spivak.androidstudio2dgame.helper.DatabaseRepository;
import tomer.spivak.androidstudio2dgame.helper.DialogManager;
import tomer.spivak.androidstudio2dgame.modelObjects.ModelObject;
import tomer.spivak.androidstudio2dgame.gameManager.GameView;
import tomer.spivak.androidstudio2dgame.R;
import tomer.spivak.androidstudio2dgame.model.Cell;
import tomer.spivak.androidstudio2dgame.modelEnums.DifficultyLevel;
import tomer.spivak.androidstudio2dgame.modelEnums.GameStatus;
import tomer.spivak.androidstudio2dgame.viewModel.GameViewModel;
import tomer.spivak.androidstudio2dgame.gameManager.GameViewListener;
import tomer.spivak.androidstudio2dgame.model.GameState;
import tomer.spivak.androidstudio2dgame.music.SoundEffects;


public class GameActivity extends AppCompatActivity implements OnItemClickListener,
        GameViewListener {


    Context context;
    GameView gameView;
    private GameViewModel viewModel;
    private SoundEffects soundEffects;

    Button btnChooseBuildingsCardView;
    Button btnStartGame;
    Button btnSkipRound;
    Button btnPause;
    LinearLayout gameLayout;


    ArrayList<String> buildingImagesURL = new ArrayList<>();


    CardView cvSelectBuildingMenu;


    ImageButton btnCloseMenu;


    BuildingsRecyclerViewAdapter adapter;


    RecyclerView buildingRecyclerView;


    OnBackPressedCallback backPressedCallback;


    DatabaseRepository databaseRepository;


    int boardSize;


    boolean gameIsOnGoing = false;


    DialogManager dialogManager;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);
        context = this;

        init();


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


        btnChooseBuildingsCardView.setOnClickListener(new View.OnClickListener() {
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
                dialogManager.showPauseAlertDialog(gameView, viewModel, volume, soundEffects, context);
            }
        });





        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)  {
            backPressedCallback = new OnBackPressedCallback(true /* enabled by default */) {
                @Override
                public void handleOnBackPressed() {

                    dialogManager.showExitAlertDialog(viewModel, context, gameView);
                }
            };
            getOnBackPressedDispatcher().addCallback(this, backPressedCallback);
        }

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
        if (soundEffects != null) {
            soundEffects.onDestroy();
        }
    }


    private void init(){
        hideSystemUI();
        initViews();
        initGame();
    }


    private void initViews() {
        btnChooseBuildingsCardView = findViewById(R.id.btnPopUpMenu);
        cvSelectBuildingMenu = findViewById(R.id.cvSelectBuildingMenu);
        btnCloseMenu = findViewById(R.id.btnCloseMenu);
        buildingRecyclerView = findViewById(R.id.buildingRecyclerView);
    }


    private void initGame() {
        boardSize = 14;
        gameLayout = findViewById(R.id.gameLinearLayout);
        soundEffects = new SoundEffects(this);
        gameView = new GameView(context, boardSize, this, soundEffects);
        gameLayout.addView(gameView);
        viewModel = new ViewModelProvider(this).get(GameViewModel.class);
        databaseRepository = DatabaseRepository.getInstance(context);
        dialogManager = DialogManager.getInstance(databaseRepository);


        btnStartGame = findViewById(R.id.btnStartGame);
        btnSkipRound = findViewById(R.id.btnSkipRound);
        btnPause = findViewById(R.id.btnPause);
        observeViewModel();
        initPlacingBuilding();




        String difficultyName = getIntent().getStringExtra("difficultyLevel");
        boolean isContinue = getIntent().getBooleanExtra("isContinue", false);
        BoardMapper boardMapper;
        final DifficultyLevel[] difficulty = new DifficultyLevel[1];
        AlertDialog loadingDialog = dialogManager.showLoadingBoardAlertDialog(context);
        if (!isContinue){
            difficulty[0] = DifficultyLevel.valueOf(difficultyName);
            boardMapper = new BoardMapper(boardSize, difficulty[0]);
            boardMapper.initBoard();
            initBoardInViewModel(boardMapper.getBoard(), loadingDialog, difficulty[0], 0L, 1, -1);
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
                    initBoardInViewModel(boardMapper.getBoard(), loadingDialog, difficulty[0],
                            timeSinceStartOfGame, currentRound, shnuzes);
                }
            });
        }
    }


    private void initBoardInViewModel(Cell[][] board, AlertDialog dialog, DifficultyLevel difficulty, Long timeSinceStartOfGame, int currentRound, int shnuzes) {
        gameView.updateBoard(board);
        viewModel.initBoard(board.clone(), difficulty, currentRound, shnuzes);
        viewModel.tick(timeSinceStartOfGame);
        dialog.dismiss();
        viewModel.setSoundEffects(soundEffects);
    }


    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );
    }


    private void initPlacingBuilding() {
        initBuildingToChoose();
        adapter = new BuildingsRecyclerViewAdapter(context, buildingImagesURL, this);
        buildingRecyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false));
        buildingRecyclerView.setAdapter(adapter);
    }


    private void initBuildingToChoose() {
        String obelisk = "obelisk";
        String archerTower = "lightning0tower";
        buildingImagesURL.add(obelisk);
        buildingImagesURL.add(archerTower);
    }


    @Override
    public void onBuildingRecyclerViewItemClick(String buildingImageURL, int position) {
        onBuildingSelected(buildingImageURL.replace("0", ""));
        cvSelectBuildingMenu.setVisibility(View.GONE);
    }


    private void observeViewModel() {
        viewModel.getGameState().observe(this, new Observer<GameState>() {
            @Override
            public void onChanged(GameState gameState) {
                gameView.unpackGameState(gameState);
                if (gameState.getGameStatus() == GameStatus.LOST) {
                    Toast.makeText(context, "You Lost", Toast.LENGTH_SHORT).show();
                    databaseRepository.removeBoard();
                    dialogManager.showLostAlertDialog(viewModel, gameView, context);
                } else if (gameState.getGameStatus() == GameStatus.WON)  {
                    Toast.makeText(context, "You Won", Toast.LENGTH_SHORT).show();
                    databaseRepository.removeBoard();
                    dialogManager.showWonAlertDialog(viewModel, gameView, context);


                }
                if (gameState.getTimeOfDay()) {
                    if (btnChooseBuildingsCardView.getVisibility() == View.GONE)
                        btnChooseBuildingsCardView.setVisibility(View.VISIBLE);
                    if (btnStartGame.getVisibility() == View.GONE && btnSkipRound.getVisibility() != View.VISIBLE) {
                        btnSkipRound.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (cvSelectBuildingMenu.getVisibility() == View.VISIBLE)
                        cvSelectBuildingMenu.setVisibility(View.GONE);
                    if (btnChooseBuildingsCardView.getVisibility() != View.GONE)
                        btnChooseBuildingsCardView.setVisibility(View.GONE);
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


    @Override
    public void onCellClicked(int row, int col) {
        viewModel.onCellClicked(row, col);
    }


    @Override
    public void onBuildingSelected(String buildingType) {
        viewModel.selectBuilding(buildingType);
    }


    @Override
    public void updateGameState(long elapsedTime) {
        if (gameIsOnGoing)
            viewModel.tick(elapsedTime);
    }
}