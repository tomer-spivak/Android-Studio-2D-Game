package tomer.spivak.androidstudio2dgame.GridView;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import androidx.annotation.NonNull;

public class TouchManager {
    private final GestureDetector gestureDetector;
    private final ScaleGestureDetector scaleGestureDetector;
    private final TouchListener listener;
    private boolean isScrolling = true;
    private float lastTouchX, lastTouchY;

    public TouchManager(Context context, TouchListener listener) {
        this.listener = listener;
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
        gestureDetector = new GestureDetector(context, new GestureListener());
    }

    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        scaleGestureDetector.onTouchEvent(event);

        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN:

            case MotionEvent.ACTION_MOVE:
                lastTouchX = event.getX();
                lastTouchY = event.getY();
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                isScrolling = false;
                break;

            case MotionEvent.ACTION_POINTER_UP:
                lastTouchX = event.getX();
                lastTouchY = event.getY();
                isScrolling = true;
                break;

        }

        return true;
    }



    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(@NonNull MotionEvent e) {
            if (!isScrolling) return false;
            if (Math.abs(lastTouchX - e.getX()) < 100 && Math.abs(lastTouchY - e.getY()) < 100){
                lastTouchX = e.getX();
                lastTouchY = e.getY();
            }
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
            if (!isScrolling) return false;
            if (e2.getPointerCount() > 1) return false;

            float deltaX = e2.getX() - lastTouchX;
            float deltaY = e2.getY() - lastTouchY;

            listener.onScroll(deltaX, deltaY);

            if (Math.abs(lastTouchX - e2.getX()) < 100 && Math.abs(lastTouchY - e2.getY()) < 100){
                lastTouchX = e2.getX();
                lastTouchY = e2.getY();
            }

            return true;
        }
        @Override
        public boolean onSingleTapUp(@NonNull MotionEvent e) {
            // This method is called when a single tap is detected
            if (isScrolling) {
                // Call a new method in the listener to handle box click
                listener.onBoxClick(e.getX(), e.getY());
                return true;
            }
            return false;
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            listener.onScale(scaleFactor, detector.getFocusX(), detector.getFocusY());

            return true;
        }
    }

    public interface TouchListener {
        void onScale(float scaleFactor, float focusX, float focusY);
        void onScroll(float deltaX, float deltaY);
        void onBoxClick(float x, float y); // New method for handling box clicks

    }
}
