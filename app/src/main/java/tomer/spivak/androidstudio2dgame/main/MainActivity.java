package tomer.spivak.androidstudio2dgame.main;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.FirebaseApp;

import java.util.ArrayList;

import tomer.spivak.androidstudio2dgame.buildingHelper.Building;
import tomer.spivak.androidstudio2dgame.buildingHelper.BuildingView;
import tomer.spivak.androidstudio2dgame.gameManager.GameView;
import tomer.spivak.androidstudio2dgame.R;



public class MainActivity extends AppCompatActivity {

    Context context;

    GameView gameView;

    Button btnChooseBuildingsAlertDialog;

    LinearLayout gameLayout;

    ArrayList<BuildingView> userBuildingsViews = new ArrayList<>();

    BuildingView selectedBuildingView;

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
                showAlertDialog();

            }
        });

    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_dialog, null);


        // Find RecyclerView in the inflated layout
        RecyclerView rvBuildings = dialogView.findViewById(R.id.rvBuildings);

        // Set up the RecyclerView (adapter, layout manager)
        rvBuildings.setLayoutManager(new LinearLayoutManager(context));

        ArrayList<Building> buildingArrayList = new ArrayList<>();
        BuildingsRecyclerViewAdapter adapter = new BuildingsRecyclerViewAdapter(context,
                buildingArrayList);
        rvBuildings.setAdapter(adapter);


        fillArrayList(buildingArrayList, adapter);


        builder.setView(dialogView);

        builder.setTitle("Choose a building");
        builder.setPositiveButton("confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Building selectedBuilding = adapter.getSelectedBuilding();

                View view = adapter.getSelectedBuildingView();

                ImageView imageView = new ImageView(context);

                imageView.setImageDrawable(((ImageView)(view.findViewById(R.id.imageView))).getDrawable());

                selectedBuildingView = new BuildingView(selectedBuilding, imageView);
                Log.d("boxClick", selectedBuildingView + "");

                gameView.selectedBuilding = selectedBuildingView;




                //startDragForItem(bitmap);
                Toast.makeText(context, selectedBuilding.getName(), Toast.LENGTH_SHORT).show();
            }
        });
        AlertDialog dialog = builder.create();

        dialog.show();
    }

    private void fillArrayList(ArrayList<Building> buildingArrayList, BuildingsRecyclerViewAdapter adapter) {
        buildingArrayList.add(new Building("https://supercell.com/images/ae58a39e76410b4ae9c2bea65d4a584d/790/hero_bg_clashofclans_.fae7c799.webp", "name"));

        buildingArrayList.add(new Building("https://hips.hearstapps.com/hmg-prod/images/screen-shot-2021-03-17-at-5-44-29-pm-1616017492.png", "colseum"));


        adapter.notifyDataSetChanged();
    }

    private void init(){
        hideSystemUI();

        initViews();

        initGame();

    }

    private void initViews() {
        btnChooseBuildingsAlertDialog = findViewById(R.id.btnPopUpMenu);

    }

    private void initGame() {
        gameLayout = findViewById(R.id.gameView);
        gameView = new GameView(context);
        gameLayout.addView(gameView);
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

    // Method to start the drag process

    public static void fillCell(int x, int y){
        Log.d("fillCell", x + " " + y);
    }
}