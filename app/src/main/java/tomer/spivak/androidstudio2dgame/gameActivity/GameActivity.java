package tomer.spivak.androidstudio2dgame.gameActivity;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.FirebaseApp;

import java.util.ArrayList;

import tomer.spivak.androidstudio2dgame.gameManager.GameView;
import tomer.spivak.androidstudio2dgame.R;
import tomer.spivak.androidstudio2dgame.gameObjects.GameBuilding;


public class GameActivity extends AppCompatActivity implements OnItemClickListener{

    Context context;

    GameView gameView;

    Button btnChooseBuildingsCardView;

    LinearLayout gameLayout;

    ArrayList<BuildingToPick> buildingsToPick;

    CardView cvSelectBuildingMenu;

    ImageButton btnCloseMenu;

    BuildingsRecyclerViewAdapter adapter;

    RecyclerView buildingRecyclerView;

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


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    OnBackInvokedDispatcher.PRIORITY_DEFAULT,
                    new OnBackInvokedCallback() {
                        @Override
                        public void onBackInvoked() {
                            Log.d("debug", "pls pls pls");
                            showAlertDialog();
                        }
                    }
            );
            Log.d("debug", "registerd back");
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
                finish();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //save base
            }
        });

        alertDialog.show();
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
        buildingsToPick = new ArrayList<>();
        popBuildingArrayList();
        adapter = new BuildingsRecyclerViewAdapter(context, buildingsToPick, this);
        buildingRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        buildingRecyclerView.setAdapter(adapter);
    }

    //pop the options for user (need to add more buildings later)
    private void popBuildingArrayList() {
        BuildingToPick tower = new BuildingToPick("Tower", R.drawable.tower);

        buildingsToPick.add(tower);
    }




    //a building has been selected in the card view, sending info to game view
    @Override
    public void onBuildingRecyclerViewItemClick(BuildingToPick building, int position) {
        GameBuilding gameBuilding = new GameBuilding(context, new Point(0,0), building.getImageUrl(), building.getName());
        gameView.setSelectedBuilding(gameBuilding);
        cvSelectBuildingMenu.setVisibility(View.GONE);
    }
}