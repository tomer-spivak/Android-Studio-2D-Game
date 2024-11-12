package tomer.spivak.androidstudio2dgame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import tomer.spivak.androidstudio2dgame.GridView.CustomGridView;
import tomer.spivak.androidstudio2dgame.GridView.TouchHandler;

public class GameView extends SurfaceView implements SurfaceHolder.Callback, TouchHandler.TouchHandlerListener {
    private GameLoop gameLoop;
    private CustomGridView gridView;
    private TouchHandler touchHandler;



    public GameView(Context context) {
        super(context);

        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        gameLoop = new GameLoop(this, surfaceHolder);

        getUserBuildings();

        setFocusable(true);

        touchHandler = new TouchHandler(context, this);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        float pxWidth = getWidth();
        float pxHeight = getHeight();
        float initScaleFactor = 1f;
        float dpWidth = dpToPx(getWidth()) * initScaleFactor;
        float dpHeight = dpToPx(getHeight()) * initScaleFactor;
        gridView = CustomGridView.initInstance(20, 20, dpHeight, dpWidth, getResources().getDisplayMetrics().density);
        Log.d("debug", gridView.toString());
        gameLoop.startLoop();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return touchHandler.onTouchEvent(event) || super.onTouchEvent(event);
    }

    @Override
    public void onScale(float scaleFactor, float focusX, float focusY) {
        gridView.updateScale(scaleFactor, focusX, focusY);
        Log.d("scalingUpdate", String.valueOf(scaleFactor));
    }

    @Override
    public void onScroll(float deltaX, float deltaY) {
        Log.d("scrollingUpdate", deltaX + " " + deltaY);
        if (deltaX < 100 && deltaY < 100)
            gridView.updatePosition(deltaX, deltaY);
    }


    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        if (canvas != null) {
            // Remove canvas.save() and translate since we're using grid position updates
            drawFPS(canvas);
            drawUPS(canvas);
            gridView.draw(canvas);
        }
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

    private void getUserBuildings() {
        //userBuildings.add(new ArcherTower());
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
    }

    float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

}