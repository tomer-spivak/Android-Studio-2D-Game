package tomer.spivak.androidstudio2dgame.gameManager;

import android.content.Context;
import android.graphics.Bitmap;
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

import tomer.spivak.androidstudio2dgame.GridView.CustomGridView;
import tomer.spivak.androidstudio2dgame.GridView.TouchHandler;
import tomer.spivak.androidstudio2dgame.R;
import tomer.spivak.androidstudio2dgame.buildingHelper.BuildingView;

public class GameView extends SurfaceView implements SurfaceHolder.Callback, TouchHandler.TouchHandlerListener {

    private GameLoop gameLoop;

    private CustomGridView gridView;

    private TouchHandler touchHandler;

    private Context context;

    private ArrayList<BuildingView> buildingsViewsArrayList = new ArrayList<>();;

    public BuildingView selectedBuilding;

    private Float scale = 1F;

    public GameView(Context context) {
        super(context);

        this.context = context;

        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        gameLoop = new GameLoop(this, surfaceHolder);

        touchHandler = new TouchHandler(context, this);

        gridView = new CustomGridView(context);
        Log.d("gridView", gridView + "");
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        gridView.initInstance(20, 20);

        gameLoop.startLoop();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return touchHandler.onTouchEvent(event) || super.onTouchEvent(event);
    }

    @Override
    public void onScale(float scaleFactor, float focusX, float focusY) {
        scale = gridView.updateScale(scaleFactor, focusX, focusY);
    }

    @Override
    public void onScroll(float deltaX, float deltaY) {

        if (deltaX < 100 && deltaY < 100)
            gridView.updatePosition(deltaX, deltaY);
    }

    @Override
    public void onBoxClick(float x, float y) {
        Point cellCenterPoint = gridView.getSelectedCell(x, y);
        if (selectedBuilding != null && isCellEmpty(cellCenterPoint) ) {
            selectedBuilding.setPoint(cellCenterPoint);
            addBuildingView(selectedBuilding);
            selectedBuilding = null;
        }
    }

    private boolean isCellEmpty(Point cellCenterPoint) {
        for (BuildingView buildingView : buildingsViewsArrayList){
            if (buildingView.getPoint() != null && buildingView.getPoint().equals(cellCenterPoint)){
                return false;
            }
        }
        return true;
    }


    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void draw(Canvas canvas) {
        Log.d("canvas", "canvas: " + canvas);
        super.draw(canvas);

        if (canvas != null) {
            // Remove canvas.save() and translate since we're using grid position updates
            drawFPS(canvas);
            drawUPS(canvas);
            gridView.draw(canvas);

            for (BuildingView buildingView : buildingsViewsArrayList) {
                ImageView imageView = buildingView.getView(); // Ensure this returns a valid ImageView

                if (imageView == null || imageView.getDrawable() == null) {
                    continue; // Skip this iteration if imageView or drawable is null
                }

                // Get the drawable and calculate scaled dimensions
                Drawable drawable = imageView.getDrawable();
                int originalWidth = drawable.getIntrinsicWidth();
                int originalHeight = drawable.getIntrinsicHeight();

                // Calculate scaled dimensions
                int scaledWidth = (int) (originalWidth * scale * 1);
                int scaledHeight = (int) (originalHeight * scale * 1);

                // Set the position of the ImageView
                float posX = buildingView.getPoint().x - (float) scaledWidth / 2;
                float posY = buildingView.getPoint().y - (float) scaledHeight / 2;

                // Create a scaled bitmap
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(
                        drawableToBitmap(drawable),
                        scaledWidth,
                        scaledHeight,
                        true // Use filtering for smoother scaling
                );

                // Draw the scaled bitmap on the canvas
                canvas.drawBitmap(scaledBitmap, posX, posY, null);
            }
        }
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


    public void addBuildingView(BuildingView selectedBuildingView) {
        buildingsViewsArrayList.add(selectedBuildingView);
        Log.d("boxClick", buildingsViewsArrayList.toString());


    }
}