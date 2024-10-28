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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {
    //enable momuntom
    private float scaleFactor = 1.0f;
    private float lastSpacing = 0f;
    private boolean isScaling = false;
    private boolean isScrolling = true;
    private float lastTouchX, lastTouchY;
    private GestureDetector gestureDetector;
    private GameLoop gameLoop;
    private CustomGridView gridView;
    private final float initScaleFactor = 0.7f;
    private ScaleGestureDetector scaleGestureDetector;



    //ArrayList<Building> userBuildings = new ArrayList<>();

    public GameView(Context context) {
        super(context);

        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        gameLoop = new GameLoop(this, surfaceHolder);

        getUserBuildings();

        setFocusable(true);
        gridView = new CustomGridView(14, 14);

        scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
        gestureDetector = new GestureDetector(getContext(), new GestureListener());
    }

    private float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }
    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        float width = dpToPx(getWidth()) * initScaleFactor;
        float height = dpToPx(getHeight()) * initScaleFactor;
        gridView.updateSize(width, height, (float) getWidth(), (float) getHeight());
        Toast.makeText(getContext(), getWidth() + " " + getHeight(), Toast.LENGTH_LONG).show();

        gameLoop.startLoop();
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);

        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN:
                lastTouchX = event.getX();
                lastTouchY = event.getY();
                return true;

            case MotionEvent.ACTION_MOVE:
                int pointerCount = event.getPointerCount();
                for (int i = 0; i < pointerCount; i++) {
                    int pointerId = event.getPointerId(i);
                    float x = event.getX(i);
                    float y = event.getY(i);
                    Log.d("Pointer " + pointerId, "Moved to: (" + x + ", " + y + ")");
                }
                if (pointerCount == 2){
                    float currentSpacing = getFingerSpacing(event);
                    float scale = (float) Math.pow(currentSpacing / lastSpacing, 1);
                    lastSpacing = currentSpacing;
                    setScaleZoomIn(scale, event.getX(), event.getY());
                    Log.d("scaleFactor", String.valueOf(scale));
                }
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                isScrolling = false;
                lastSpacing = getFingerSpacing(event);
                Log.d("debug", "last spacing: " + lastSpacing);
                break;

            case MotionEvent.ACTION_POINTER_UP:
                isScrolling = true;
                lastTouchX = 0;
                lastTouchY = 0;
                break;
            case MotionEvent.ACTION_UP:
                return true;



        }

        return super.onTouchEvent(event);
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



    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final float SCROLL_SENSITIVITY = 1.0f; // Adjust this value as needed

        @Override
        public boolean onDown(MotionEvent e) {
            if (!isScrolling)
                    return false;
            // Initialize last touch coordinates on down event
            lastTouchX = e.getX();
            lastTouchY = e.getY();
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (!isScrolling)
                return false;

            if (e2.getPointerCount() > 1) {
                // Do not handle the scroll if two or more fingers are detected
                return false;
            }

            Log.d("debug", "scrolling");
            // Handle dragging
            if (e1 != null && e2 != null) {
                float deltaX = (e2.getX() - lastTouchX) * SCROLL_SENSITIVITY;
                float deltaY = (e2.getY() - lastTouchY) * SCROLL_SENSITIVITY;

                // Update grid position based on drag
                Log.d("updatedPosition", "update position: " + deltaX + " " + deltaY);
                if (Math.abs(deltaX) < 50 && Math.abs(deltaY) < 50)
                    updatePosition(deltaX, deltaY);

                // Update last touch coordinates
                lastTouchX = e2.getX();
                lastTouchY = e2.getY();
            }
            return true;
        }

    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            // Get the scale factor from the detector
            float scaleChange = detector.getScaleFactor();
            if (Math.abs(scaleChange - 1.0f) > 0.01f) {
                scaleFactor *= scaleChange;
                scaleFactor = Math.max(0.3f, Math.min(scaleFactor, 2.0f));

            }
            return true;
        }
    }

    public void updatePosition(float deltaX, float deltaY) {
        // Directly update grid position
        gridView.updatePosition(deltaX, deltaY);
    }

    public void setScaleZoomIn(float scaleFactor, float x, float y) {
        //Log.d("debug", "scaleFactor: " + String.valueOf(scaleFactor));
        gridView.updateScale(scaleFactor, x, y);
        //updatePosition(lastTouchX, lastTouchY);
        //Log.d("debug", "update position: " + lastTouchX + " " + lastTouchY);
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