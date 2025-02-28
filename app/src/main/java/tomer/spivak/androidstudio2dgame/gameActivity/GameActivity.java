package tomer.spivak.androidstudio2dgame.gameActivity;

import android.app.MediaRouteButton;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import tomer.spivak.androidstudio2dgame.FirebaseRepository;
import tomer.spivak.androidstudio2dgame.gameManager.GameView;
import tomer.spivak.androidstudio2dgame.R;
import tomer.spivak.androidstudio2dgame.modelEnums.GameStatus;
import tomer.spivak.androidstudio2dgame.modelEnums.Direction;
import tomer.spivak.androidstudio2dgame.modelObjects.Enemy;
import tomer.spivak.androidstudio2dgame.modelEnums.EnemyState;
import tomer.spivak.androidstudio2dgame.modelEnums.EnemyType;
import tomer.spivak.androidstudio2dgame.viewModel.GameViewModel;
import tomer.spivak.androidstudio2dgame.viewModel.GameViewListener;
import tomer.spivak.androidstudio2dgame.model.Cell;
import tomer.spivak.androidstudio2dgame.model.GameState;
import tomer.spivak.androidstudio2dgame.modelObjects.ModelObject;
import tomer.spivak.androidstudio2dgame.modelObjects.ModelObjectFactory;
import tomer.spivak.androidstudio2dgame.model.Position;


public class GameActivity extends AppCompatActivity implements OnItemClickListener,
        GameViewListener {

    Context context;

    GameView gameView;

    private GameViewModel viewModel;

    Button btnChooseBuildingsCardView;
    Button btnStartGame;
    Button btnSkipRound;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        //set window to fullscreen (hide status bar)
        setContentView(R.layout.activity_game);
        context = this;


        init();

        btnStartGame = findViewById(R.id.btnStartGame);
        btnSkipRound = findViewById(R.id.btnSkipRound);

        btnStartGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameIsOnGoing = true;
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)  {
            backPressedCallback = new OnBackPressedCallback(true /* enabled by default */) {
                @Override
                public void handleOnBackPressed() {

                    // Show the alert dialog and pass a Runnable to be executed when
                    // the dialog is dismissed
                    showAlertDialog();
                }
            };
            getOnBackPressedDispatcher().addCallback(this, backPressedCallback);

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

    //checks if user wants to save his base
    private void showAlertDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.alert_dialog, null);

        AlertDialog alertDialog = new AlertDialog.Builder(this)
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

    private void init(){
        hideSystemUI();

        firebaseRepository = new FirebaseRepository(context);

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
        boardSize = 12;
        gameLayout = findViewById(R.id.gameView);
        gameView = new GameView(context, boardSize, this);
        gameLayout.addView(gameView);
        viewModel = new ViewModelProvider(this).get(GameViewModel.class);
        observeViewModel();

        initPlacingBuilding();

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Loading board")
                .setMessage("Please wait...")
                .setCancelable(false) // Prevents dismissal by tapping outside or pressing back
                .create();

        dialog.show();
        firebaseRepository.loadBoard(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Toast.makeText(context, "success", Toast.LENGTH_SHORT).show();
                Log.d("debug", String.valueOf(documentSnapshot.exists()));
                if (documentSnapshot.exists()) {
                    Cell[][] board = new Cell[boardSize][boardSize];
                    Map<String, Object> data = documentSnapshot.getData();

                    // Log each row and its contents in a more readable format
                    for (Map.Entry<String, Object> entry : data.entrySet()) {
                        Object rowData = entry.getValue();
                        List<Map<String, Object>> rowList = (List<Map<String, Object>>) rowData;

                        for (Map<String, Object> col : rowList) {
                            HashMap map = (HashMap) col.get("position");
                            Position pos = new Position(((Long)(map.get("x"))).intValue(),
                                    ((Long)(map.get("y"))).intValue());
                            HashMap objectMap = (HashMap)(col.get("object"));

                            if (objectMap != null){
                                ModelObject object = ModelObjectFactory.create((String)
                                                objectMap.get("type"), pos);
                                String type = (String) objectMap.get("type");
                                object.setHealth(((Number) Objects.requireNonNull(objectMap.
                                        get("health"))).floatValue());
                                if (isInEnum(type, EnemyType.class)) {
                                    Enemy enemy = (Enemy) object;
                                    enemy.setState((EnemyState) objectMap.get("enemyState"));
                                    enemy.setCurrentDirection((Direction) objectMap.
                                            get("currentDirection"));
                                    enemy.setPath((List<Position>) objectMap.get("path"));
                                    enemy.setCurrentTargetIndex(((Number) Objects.
                                            requireNonNull(objectMap.get("currentTargetIndex")))
                                            .intValue());
                                    enemy.setTimeSinceLastAttack((Float) objectMap.
                                            get("timeSinceLastAttack"));
                                    enemy.setTimeSinceLastMove((Float) objectMap.
                                            get("timeSinceLastMove"));

                                }

                                board[pos.getX()][pos.getY()] = new Cell(pos, object);

                            } else {
                                board[pos.getX()][pos.getY()] = new Cell(pos);
                            }
                        }
                    }
                    board = removeNullRowsAndColumns(board);
                    gameView.setBoard(board);
                    viewModel.initBoardFromCloud(gameView.getBoard());
                    dialog.dismiss();
                } else {
                    Cell[][] board = new Cell[boardSize][boardSize];
                    for (int i = 0; i < boardSize; i++) {
                        for (int j = 0; j < boardSize; j++) {
                            board[i][j] = new Cell(new Position(i, j));
                        }
                    }
                    gameView.setBoard(board);
                    viewModel.initBoardFromCloud(gameView.getBoard());
                    dialog.dismiss();
                }
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });
    }

    public <T extends Enum<T>> boolean isInEnum(String value, Class<T> enumClass) {
        for (T enumValue : enumClass.getEnumConstants()) {
            if (enumValue.name().equals(value)) {
                return true;
            }
        }
        return false;
    }

    public Cell[][] removeNullRowsAndColumns(Cell[][] array) {
            if (array == null || array.length == 0) return new Cell[0][0];

            int rows = array.length;
            int cols = array[0].length;

            // Track valid rows and columns
            boolean[] validRows = new boolean[rows];
            boolean[] validCols = new boolean[cols];

            // Identify valid rows and columns
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if (array[i][j] != null) {
                        validRows[i] = true;
                        validCols[j] = true;
                    }
                }
            }

            // Count valid rows and columns
            int validRowCount = 0;
            for (boolean row : validRows) if (row) validRowCount++;

            int validColCount = 0;
            for (boolean col : validCols) if (col) validColCount++;

            // Create the new array
            Cell[][] result = new Cell[validRowCount][validColCount];

            // Fill the new array
            int newRow = 0;
            for (int i = 0; i < rows; i++) {
                if (validRows[i]) {
                    int newCol = 0;
                    for (int j = 0; j < cols; j++) {
                        if (validCols[j]) {
                            result[newRow][newCol++] = array[i][j];
                        }
                    }
                    newRow++;
                }
            }

            return result;

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
                Log.d("board", "changed");
                gameView.unpackGameState(gameState);
                if (gameState.getGameStatus() == GameStatus.LOST){
                    Toast.makeText(context, "lost", Toast.LENGTH_SHORT).show();
                    Log.d("lost", "lost");
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Title")
                            .setMessage("This is an alert dialog.")
                            .setPositiveButton("Go back to menu", (dialog, which) -> {
                                finish();
                            })
                            .setNegativeButton("Exit App", (dialog, which) -> {
                                finishAffinity();
                                System.exit(0);
                            });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();



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
        viewModel.placeBuilding(row, col);
        if (!viewModel.isEmptyBuildings()) {
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