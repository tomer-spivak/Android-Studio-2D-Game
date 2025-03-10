package tomer.spivak.androidstudio2dgame.gameActivity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
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

import tomer.spivak.androidstudio2dgame.gameManager.GameView;
import tomer.spivak.androidstudio2dgame.R;
import tomer.spivak.androidstudio2dgame.model.Cell;
import tomer.spivak.androidstudio2dgame.modelEnums.DifficultyLevel;
import tomer.spivak.androidstudio2dgame.modelEnums.GameStatus;
import tomer.spivak.androidstudio2dgame.viewModel.GameViewModel;
import tomer.spivak.androidstudio2dgame.viewModel.GameViewListener;
import tomer.spivak.androidstudio2dgame.model.GameState;


public class GameActivity extends AppCompatActivity implements OnItemClickListener,
        GameViewListener{

    Context context;

    GameView gameView;

    private GameViewModel viewModel;

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

    FirebaseRepository firebaseRepository;
    int boardSize;
    boolean gameIsOnGoing = false;

    DialogHandler dialogHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        //set window to fullscreen (hide status bar)
        setContentView(R.layout.activity_game);
        context = this;

        init();

        btnStartGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameIsOnGoing = true;
                btnPause.setVisibility(View.VISIBLE);
                btnStartGame.setVisibility(View.GONE);
                btnSkipRound.setVisibility(View.VISIBLE);
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
                gameView.pauseGameLoop();
                dialogHandler.showPauseAlertDialog(gameView);

           }
        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)  {
            backPressedCallback = new OnBackPressedCallback(true /* enabled by default */) {
                @Override
                public void handleOnBackPressed() {
                    // Show the alert dialog and pass a Runnable to be executed when
                    // the dialog is dismissed
                    dialogHandler.showExitAlertDialog(viewModel);
                }
            };
            getOnBackPressedDispatcher().addCallback(this, backPressedCallback);

        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameView != null) {
            gameView.resumeGameLoop(); // implement this method to start the thread
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameView != null) {
            gameView.pauseGameLoop(); // implement this method to stop the thread
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gameView != null) {
            gameView.stopGameLoop(); // if needed, ensure complete shutdown of the thread
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
        gameLayout = findViewById(R.id.gameView);
        gameView = new GameView(context, boardSize, this);
        gameLayout.addView(gameView);
        viewModel = new ViewModelProvider(this).get(GameViewModel.class);
        firebaseRepository = new FirebaseRepository(context);
        dialogHandler = new DialogHandler(context, firebaseRepository);

        btnStartGame = findViewById(R.id.btnStartGame);
        btnSkipRound = findViewById(R.id.btnSkipRound);
        btnPause = findViewById(R.id.btnPause);

        observeViewModel();

        initPlacingBuilding();

        String difficultyName = getIntent().getStringExtra("difficultyLevel");
        boolean isContinue = getIntent().getBooleanExtra("isContinue", false);
        BoardMapper boardMapper;
        final DifficultyLevel[] difficulty = new DifficultyLevel[1];
        AlertDialog loadingDialog = dialogHandler.showLoadingBoardAlertDialog();
        if (!isContinue){
            //if is continue is false, it means that we are creating a new game
            //in order to create a new game we need to create a new board because there is no
            //board in the database
            //then we need to extract the difficulty level from the intent
            difficulty[0] = DifficultyLevel.valueOf(difficultyName);
            boardMapper = new BoardMapper(boardSize, difficulty[0]);
            boardMapper.createBoard(null);
            initBoardInViewModel(boardMapper.getBoard(), loadingDialog, difficulty[0]);
        } else {
            //if is continue is true, it means that we are continuing a game
            //we need to extract the difficulty from the database
            boardMapper = new BoardMapper(boardSize);

            firebaseRepository.loadBoardFromDataBase(boardMapper, new OnBoardLoadedListener() {
                @Override
                public void onBoardLoaded(Cell[][] board) {
                    Toast.makeText(context, "got board", Toast.LENGTH_SHORT).show();
                    if (!boardMapper.isBoardEmpty())
                        btnStartGame.setEnabled(true);
                    difficulty[0] = boardMapper.getDifficulty();
                    initBoardInViewModel(boardMapper.getBoard(), loadingDialog, difficulty[0]);
                }
            });
        }

    }
    private void initBoardInViewModel(Cell[][] board, AlertDialog dialog,
                                      DifficultyLevel difficulty) {
        gameView.setBoard(board);
        viewModel.initBoardFromCloud(gameView.getBoard(), difficulty);
        dialog.dismiss();
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
    //prepares the recycler view

    private void initPlacingBuilding() {
        initBuildingToChoose();
        adapter = new BuildingsRecyclerViewAdapter(context, buildingImagesURL, this);
        buildingRecyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false));
        buildingRecyclerView.setAdapter(adapter);
    }

    //pop the options for user (need to add more buildings later)
    private void initBuildingToChoose() {
        String obelisk = "OBELISK";
        String archerTower = "ARCHERTOWER";

        buildingImagesURL.add(obelisk);
        buildingImagesURL.add(archerTower);
    }

    //a building has been selected in the card view, sending info to game view
    @Override
    public void onBuildingRecyclerViewItemClick(String buildingImageURL, int position) {
        gameView.setSelectedBuilding(buildingImageURL);
        cvSelectBuildingMenu.setVisibility(View.GONE);
    }

    private void observeViewModel() {
        viewModel.getGameState().observe(this, new Observer<GameState>() {
            @Override
            public void onChanged(GameState gameState) {
                gameView.unpackGameState(gameState);
                if (gameState.getGameStatus() == GameStatus.LOST){
                    Toast.makeText(context, "You Lost", Toast.LENGTH_SHORT).show();
                    dialogHandler.showLostAlertDialog();

                }

                if (gameState.getTimeOfDay()){
                    if (btnChooseBuildingsCardView.getVisibility() == View.GONE)
                        btnChooseBuildingsCardView.setVisibility(View.VISIBLE);
                    if(btnStartGame.getVisibility() == View.GONE && btnSkipRound.getVisibility()
                            != View.VISIBLE){
                        btnSkipRound.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (cvSelectBuildingMenu.getVisibility() == View.VISIBLE)
                        cvSelectBuildingMenu.setVisibility(View.GONE);
                    if (btnChooseBuildingsCardView.getVisibility() != View.GONE)
                        btnChooseBuildingsCardView.setVisibility(View.GONE);
                    if(btnSkipRound.getVisibility() == View.VISIBLE) {
                        btnSkipRound.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    @Override
    public void onCellClicked(int row, int col) {
        viewModel.onCellClicked(row, col);
        if (viewModel.isNotEmptyBuildings()) {
            btnStartGame.setEnabled(true);
        }
    }
    @Override
    public void onBuildingSelected(String buildingType) {
        viewModel.selectBuilding(buildingType);
    }
    @Override
    public void updateGameState(long elapsedTime) {
        if (gameIsOnGoing)
            viewModel.updateGameState(elapsedTime);
    }
}