package tomer.spivak.androidstudio2dgame.gameActivity;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.FirebaseApp;

import java.util.ArrayList;

import tomer.spivak.androidstudio2dgame.gameManager.GameView;
import tomer.spivak.androidstudio2dgame.R;
import tomer.spivak.androidstudio2dgame.gameObjects.GameBuilding;
import tomer.spivak.androidstudio2dgame.gameObjects.GameObject;


public class GameActivity extends AppCompatActivity implements OnItemClickListener{

    Context context;

    GameView gameView;

    Button btnChooseBuildingsAlertDialog;

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

        btnChooseBuildingsAlertDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showAlertDialog();
                showBuildingsCardView();
            }
        });

        btnCloseMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cvSelectBuildingMenu.setVisibility(View.GONE);   
            }
        });

    }

    private void init(){
        hideSystemUI();

        initViews();

        initGame();

    }

    private void initViews() {
        btnChooseBuildingsAlertDialog = findViewById(R.id.btnPopUpMenu);

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

    private void initPlacingBuilding() {
        buildingsToPick = new ArrayList<>();
        popBuildingArrayList();
        adapter = new BuildingsRecyclerViewAdapter(context, buildingsToPick, this);
        buildingRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        buildingRecyclerView.setAdapter(adapter);
    }

    private void popBuildingArrayList() {
        BuildingToPick tower = new BuildingToPick("Tower", R.drawable.tower);

        buildingsToPick.add(tower);
    }

    private void showBuildingsCardView() {
        cvSelectBuildingMenu.setVisibility(View.VISIBLE);
    }

    //a building was selected in the cardview, transporting info to game view
    void buildingSelected(GameObject selectedBuilding){
        gameView.setSelectedBuilding(selectedBuilding);
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

    @Override
    public void onBuildingRecyclerViewItemClick(BuildingToPick building, int position) {
        //Toast.makeText(this, "url: " + building.getImageUrl(), Toast.LENGTH_SHORT).show();
        //Toast.makeText(this, ": " + R.drawable.tower, Toast.LENGTH_SHORT).show();
        //Toast.makeText(this, "Clicked item position: " + position, Toast.LENGTH_SHORT).show();
        GameBuilding gameBuilding = new GameBuilding(context, new Point(0,0), building.getImageUrl(), building.getName());
        buildingSelected(gameBuilding);
        cvSelectBuildingMenu.setVisibility(View.GONE);
    }
}