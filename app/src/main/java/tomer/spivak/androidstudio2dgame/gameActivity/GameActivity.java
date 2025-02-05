package tomer.spivak.androidstudio2dgame.gameActivity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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

import tomer.spivak.androidstudio2dgame.FirebaseRepository;
import tomer.spivak.androidstudio2dgame.gameManager.GameView;
import tomer.spivak.androidstudio2dgame.R;
import tomer.spivak.androidstudio2dgame.modelObjects.Tower;
import tomer.spivak.androidstudio2dgame.viewModel.GameViewModel;
import tomer.spivak.androidstudio2dgame.viewModel.GameViewListener;
import tomer.spivak.androidstudio2dgame.model.Cell;
import tomer.spivak.androidstudio2dgame.model.GameState;
import tomer.spivak.androidstudio2dgame.modelObjects.ModelObject;
import tomer.spivak.androidstudio2dgame.modelObjects.ModelObjectFactory;
import tomer.spivak.androidstudio2dgame.model.Position;


public class GameActivity extends AppCompatActivity implements OnItemClickListener, GameViewListener {

    Context context;

    GameView gameView;

    private GameViewModel viewModel;

    Button btnChooseBuildingsCardView;

    LinearLayout gameLayout;

    ArrayList<String> buildingImagesURL = new ArrayList<>();

    CardView cvSelectBuildingMenu;

    ImageButton btnCloseMenu;

    BuildingsRecyclerViewAdapter adapter;

    RecyclerView buildingRecyclerView;

    OnBackPressedCallback backPressedCallback;

    FirebaseRepository firebaseRepository;
    int boardSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        //set window to fullscreen (hide status bar)
        setContentView(R.layout.activity_main);
        context = this;


        init();

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)  {
            backPressedCallback = new OnBackPressedCallback(true /* enabled by default */) {
                @Override
                public void handleOnBackPressed() {

                    // Show the alert dialog and pass a Runnable to be executed when the dialog is dismissed
                    showAlertDialog();
                }
            };
            getOnBackPressedDispatcher().addCallback(this, backPressedCallback);

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
                firebaseRepository.saveBoard(viewModel.getGameState().getValue().getGrid(), new OnSuccessListener<Void>() {
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
        boardSize = 10;
        gameLayout = findViewById(R.id.gameView);
        gameView = new GameView(context, boardSize, this);
        gameLayout.addView(gameView);

        viewModel = new ViewModelProvider(this).get(GameViewModel.class);
        //viewModel.initGameState(boardSize);
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

                                if (objectMap.get("type").equals("tower")){
                                    Tower tower = (Tower) object;
                                    tower.setHealth(((Number) objectMap.get("health")).floatValue());
                                }

                                board[pos.getX()][pos.getY()] = new Cell(pos, object);

                            } else {
                                board[pos.getX()][pos.getY()] = new Cell(pos);
                            }
                        }
                    }

                    viewModel.initBoardFromCloud(board);
                    dialog.dismiss();
                }
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });
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
        buildingRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        buildingRecyclerView.setAdapter(adapter);
    }

    //pop the options for user (need to add more buildings later)
    private void initBuildingToChoose() {
        String tower = "tower";

        buildingImagesURL.add(tower);
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
                Cell[][] board = gameState.getGrid();
                gameView.setBoard(board);
            }
        });
    }

    @Override
    public void onCellClicked(int row, int col) {
        viewModel.placeBuilding(row, col);
    }

    @Override
    public void onBuildingSelected(String buildingType) {
        viewModel.selectBuilding(buildingType);
    }

    @Override
    public void updateGameState(long elapsedTime) {
        viewModel.updateGameState(elapsedTime);
    }


}