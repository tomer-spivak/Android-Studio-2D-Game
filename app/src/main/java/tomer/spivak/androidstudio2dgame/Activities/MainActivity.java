package tomer.spivak.androidstudio2dgame.Activities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import tomer.spivak.androidstudio2dgame.Buildings.ArcherTower;
import tomer.spivak.androidstudio2dgame.Buildings.Building;
import tomer.spivak.androidstudio2dgame.GameView;
import tomer.spivak.androidstudio2dgame.R;



public class MainActivity extends AppCompatActivity {
    GameView gameView;
    Button btnPopUpMenu;
    Context context;
    ArrayList<Building> userBuildings = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        //set window to fullscreen (hide status bar)
        setContentView(R.layout.activity_main);
        context = this;

        init();



        btnPopUpMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(context, v);
                popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.item1) {
                            Toast.makeText(context, "hello", Toast.LENGTH_SHORT).show();
                            //startDragForItem(BitmapFactory.decodeResource(getResources(), ));
                            return true;
                        }

                        return false;
                    }
                });

                popup.show();

            }
        });

    }
    // Method to start the drag process
    private void startDragForItem(Bitmap bitmap) {
        // Create a new TextView (or ImageView) that represents the item being dragged
        ImageView draggableItem = new ImageView(this);
        draggableItem.setImageBitmap(bitmap);

        // Position and add the draggable item to your main layout
        FrameLayout mainLayout = findViewById(R.id.main);
        mainLayout.addView(draggableItem);

        // Set up touch listener for dragging
        //draggableItem.setOnTouchListener(new DragTouchListener());
    }

    void init(){
        hideSystemUI();

        LinearLayout gameLayout = findViewById(R.id.gameView);
        gameView = new GameView(context);
        gameLayout.addView(gameView);

        btnPopUpMenu = findViewById(R.id.btnPopUpMenu);
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

}