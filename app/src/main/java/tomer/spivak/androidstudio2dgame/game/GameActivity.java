package tomer.spivak.androidstudio2dgame.game;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.FirebaseApp;

import java.util.ArrayList;

import tomer.spivak.androidstudio2dgame.buildingHelper.Building;
import tomer.spivak.androidstudio2dgame.buildingHelper.BuildingView;
import tomer.spivak.androidstudio2dgame.gameManager.GameView;
import tomer.spivak.androidstudio2dgame.R;



public class GameActivity extends AppCompatActivity implements OnItemClickListener{

    Context context;

    GameView gameView;

    Button btnChooseBuildingsAlertDialog;

    LinearLayout gameLayout;

    ArrayList<Building> buildings;

    BuildingView selectedBuildingView;

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

        Log.d("debug", FirebaseApp.getApps(context).toString());

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

    private void showBuildingsCardView() {
        cvSelectBuildingMenu.setVisibility(View.VISIBLE);
        buildingRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        adapter = new BuildingsRecyclerViewAdapter(context, buildings, this);

        buildingRecyclerView.setAdapter(adapter);


        adapter.notifyDataSetChanged();
    }

    private void popBuildingArrayList() {
        Building tower = new Building(R.drawable.tower, "tower");
        buildings.add(tower);

        Building monster = new Building(R.drawable.monster, "monster");
        buildings.add(monster);


    }

    void buildingSelected(Building selectedBuilding){

        ImageView imageView = new ImageView(context);

        imageView.setImageResource(selectedBuilding.getImageUrl());

        selectedBuildingView = new BuildingView(selectedBuilding, imageView);
        //Log.d("boxClick", selectedBuildingView + "");

        gameView.setSelectedBuilding(selectedBuildingView);

        //startDragForItem(bitmap);
        //Toast.makeText(context, selectedBuilding.getName(), Toast.LENGTH_SHORT).show();
    }

    private void fillArrayList(ArrayList<Building> buildingArrayList, BuildingsRecyclerViewAdapter adapter) {
        buildingArrayList.add(new Building(R.drawable.tower, "tower"));



        adapter.notifyDataSetChanged();
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
        buildings = new ArrayList<>();
        popBuildingArrayList();

       // buildingRecyclerView.setOn
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
    public void onBuildingRecyclerViewItemClick(Building building,  int position) {
        Toast.makeText(this, "url: " + building.getImageUrl(), Toast.LENGTH_SHORT).show();
        Toast.makeText(this, ": " + R.drawable.tower, Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "Clicked item position: " + position, Toast.LENGTH_SHORT).show();
        buildingSelected(building);
        cvSelectBuildingMenu.setVisibility(View.GONE);
    }
}