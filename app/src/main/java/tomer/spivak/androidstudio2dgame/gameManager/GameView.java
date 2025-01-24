package tomer.spivak.androidstudio2dgame.gameManager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Objects;

import tomer.spivak.androidstudio2dgame.GridView.CustomGridView;
import tomer.spivak.androidstudio2dgame.GridView.TouchHandler;
import tomer.spivak.androidstudio2dgame.R;
import tomer.spivak.androidstudio2dgame.gameObjects.GameObject;

public class GameView extends SurfaceView implements SurfaceHolder.Callback, TouchHandler.TouchHandlerListener {

    private final GameLoop gameLoop;

    private final CustomGridView gridView;

    private final TouchHandler touchHandler;

    private final ArrayList<GameObject> gameObjectsViewsArrayList = new ArrayList<>();

    public GameObject selectedBuilding;

    private Float scale = 1F;
    private Bitmap backgroundBitmap;
    private Paint paint;


    public GameView(Context context) {
        super(context);

        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        gameLoop = new GameLoop(this, surfaceHolder);

        touchHandler = new TouchHandler(context, this);

        gridView = new CustomGridView(context);

        init();
    }
    void init(){
        backgroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.background_game_morning);
        paint = new Paint();
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        gridView.initInstance(10, 10);

        gameLoop.startLoop();
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        performClick();
        return touchHandler.onTouchEvent(event) || super.onTouchEvent(event);
    }

    @Override
    public void onScale(float scaleFactor, float focusX, float focusY) {
        scale = gridView.updateScale(scaleFactor, focusX, focusY);
        updateScaleForGameObjects();
    }

    private void updateScaleForGameObjects() {
        for (GameObject gameObject : gameObjectsViewsArrayList) {
            Log.d("debug", gameObject.getImagePoint().x + ", fuck " + gameObject.getImagePoint().y);
            gameObject.setScale(scale);
        }
    }

    @Override
    public void onScroll(float deltaX, float deltaY) {
        gridView.updatePosition(deltaX, deltaY);
    }


    private boolean isCellEmpty(Point cellCenterPoint) {
        for (GameObject buildingView : gameObjectsViewsArrayList){
            if (buildingView.getImagePoint() != null && buildingView.getImagePoint().equals(cellCenterPoint)){
                return false;
            }
        }
        return true;
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public void drawUPS(Canvas canvas) {
        String averageUPS = Double.toString(gameLoop.getAverageUPS());
        Paint paint = new Paint();
        int color = ContextCompat.getColor(getContext(), R.color.yellow);
        paint.setColor(color);
        paint.setTextSize(30);
        // Draw UI elements at fixed positions
        canvas.drawText("UPS: " + averageUPS, 100, 100, paint);
    }

    public void drawFPS(Canvas canvas) {
        String averageFPS = Double.toString(gameLoop.getAverageFPS());
        Paint paint = new Paint();
        int color = ContextCompat.getColor(getContext(), R.color.red);
        paint.setColor(color);
        paint.setTextSize(30);
        // Draw UI elements at fixed positions
        canvas.drawText("FPS: " + averageFPS, 100, 200, paint);
    }

    public void update() {
        // Add any update logic here
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
    }


    //prepares the building the user picked to be placed by user with a click
    public void setSelectedBuilding(GameObject selectedBuildingView) {
        this.selectedBuilding = selectedBuildingView;
    }

    //the user clicked on a cell, adding the selected building(if there is one) to drawn buildings
    @Override
    public void onBoxClick(float x, float y) {
        Point cellCenterPoint = gridView.getSelectedCell(x, y);
        if (selectedBuilding != null && isCellEmpty(cellCenterPoint) ) {
            selectedBuilding.setImagePoint(cellCenterPoint);
            addBuildingView(selectedBuilding);
            selectedBuilding = null;
        }
    }

    //adds a building to the drawn buildings in order
    public void addBuildingView(GameObject selectedBuildingView) {
        int i = 0;
        int size = gameObjectsViewsArrayList.size();
        while (i < size && gameObjectsViewsArrayList.get(i).getImagePoint().y < selectedBuildingView.getImagePoint().y) {
            i++;
        }
        gameObjectsViewsArrayList.add(i, selectedBuildingView); // Insert at the correct position
        Log.d("debug", "Added building at position: " + selectedBuildingView.getImagePoint() + " | size: " + gameObjectsViewsArrayList.size());
    }

    @Override
    //draw method
    public void draw(Canvas canvas) {
        super.draw(canvas);

        // Get the screen width and height
        int screenWidth = canvas.getWidth();
        int screenHeight = canvas.getHeight();

        Bitmap scaledBackBitmap = Bitmap.createScaledBitmap(
                backgroundBitmap,
                screenWidth,
                screenHeight,
                true
        );

        // Draws the background image
        canvas.drawBitmap(scaledBackBitmap, 0, 0, paint);

        drawFPS(canvas);
        drawUPS(canvas);

        //draws basic grid with grass
        gridView.draw(canvas);

        //draws buildings
        for (GameObject gameObject : gameObjectsViewsArrayList) {
            Log.d("debug", "drawing: " + gameObject.getImagePoint().x + " " + gameObject.getImagePoint().y);
            gameObject.drawView(canvas);
        }
    }

}