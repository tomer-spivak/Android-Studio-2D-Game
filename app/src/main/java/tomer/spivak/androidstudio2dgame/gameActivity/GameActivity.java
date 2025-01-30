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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tomer.spivak.androidstudio2dgame.gameManager.GameView;
import tomer.spivak.androidstudio2dgame.R;


public class GameActivity extends AppCompatActivity implements OnItemClickListener{

    Context context;

    GameView gameView;

    Button btnChooseBuildingsCardView;

    LinearLayout gameLayout;

    ArrayList<String> buildingImagesURL = new ArrayList<>();

    CardView cvSelectBuildingMenu;

    ImageButton btnCloseMenu;

    BuildingsRecyclerViewAdapter adapter;

    RecyclerView buildingRecyclerView;
    OnBackPressedCallback backPressedCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        //set window to fullscreen (hide status bar)
        setContentView(R.layout.activity_main);
        context = this;

        FirebaseApp.initializeApp(this);

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
        Log.d("debug", "tried to show alert");
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
                saveBoard();
            }
        });

        alertDialog.show();
    }

    private void saveBoard() {

        String[][] board = gameView.getBoard();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // Handle case where user is not logged in
            return;
        }
        String userId = user.getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> boardData = new HashMap<>();
        for (int i = 0; i < board.length; i++) {
            boardData.put("row_" + i, Arrays.asList(board[i]));
        }

        db.collection("users")
                .document(userId)
                .collection("board")
                .document("board objects")
                .set(boardData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context, "successfully saved board", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "failed to save board", Toast.LENGTH_SHORT).show();
                    }
                })
        ;
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
        gameLayout = findViewById(R.id.gameView);
        gameView = new GameView(context);
        gameLayout.addView(gameView);


        initPlacingBuilding();


        initBoard();
    }

    private void initBoard() {
       // ProgressBar progressBar = findViewById(R.id.progressBar);
        //progressBar.setVisibility(View.VISIBLE);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Loading")
                .setMessage("loading please wait")
                .setCancelable(false)
                .show();
        //fetch from firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // Handle case where user is not logged in
            return;
        }
        String userId = user.getUid();
        db.collection("users")
                .document(userId)
                .collection("board")
                .document("board objects")
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        //progressBar.setVisibility(View.GONE);
                        dialog.dismiss();
                        Toast.makeText(context, "successfully fetched board",
                                Toast.LENGTH_SHORT).show();
                        Map<String, Object> data = documentSnapshot.getData();
                        int rows = data.size();
                        int cols = 10; // Assuming each row has 10 columns

                        // Initialize the String[][] array
                        String[][] board = new String[rows][cols];

                        // Populate the array
                        for (Map.Entry<String, Object> entry : data.entrySet()) {
                            // Extract row index from key, e.g., "row_0" -> 0
                            String key = entry.getKey();
                            int rowIndex = Integer.parseInt(key.split("_")[1]);

                            // Cast the value to a List<String> (or handle nulls)
                            List<String> row = (List<String>) entry.getValue();
                            if (row != null) {
                                for (int col = 0; col < row.size(); col++) {
                                    board[rowIndex][col] = row.get(col);
                                }
                            }
                        }

                        // Debug print the array
                        for (String[] row : board) {
                            Log.d("debug", Arrays.toString(row));
                        }

                        gameView.setBoard(board);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                      //  progressBar.setVisibility(View.GONE);
                        Toast.makeText(context, "failed to fetch board",
                                Toast.LENGTH_SHORT).show();
                    }
                });

         //update board



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
        popBuildingArrayList();
        adapter = new BuildingsRecyclerViewAdapter(context, buildingImagesURL, this);
        buildingRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        buildingRecyclerView.setAdapter(adapter);
    }

    //pop the options for user (need to add more buildings later)
    private void popBuildingArrayList() {
        String tower = "tower";

        buildingImagesURL.add(tower);
    }




    //a building has been selected in the card view, sending info to game view
    @Override
    public void onBuildingRecyclerViewItemClick(String buildingImageURL, int position) {

        gameView.setSelectedBuilding(buildingImageURL);
        cvSelectBuildingMenu.setVisibility(View.GONE);
    }
}