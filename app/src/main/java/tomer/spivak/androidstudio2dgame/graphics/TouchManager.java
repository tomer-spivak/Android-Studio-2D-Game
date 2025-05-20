package tomer.spivak.androidstudio2dgame.graphics;


import android.content.Context;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;


import androidx.annotation.NonNull;


public class TouchManager {
    private final GestureDetector gestureDetector;
    private final ScaleGestureDetector scaleGestureDetector;
    private final TouchListener listener;

    public TouchManager(Context context, TouchListener listener) {
        this.listener = listener;
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
        gestureDetector = new GestureDetector(context, new GestureListener());
    }

    public boolean onTouchEvent(MotionEvent event) {
        if(event.getPointerCount() > 1){
            return scaleGestureDetector.onTouchEvent(event);
        } else {
            return gestureDetector.onTouchEvent(event);
        }
    }

    //this listener is responsible for scrolling
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(@NonNull MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
            listener.onScroll(-distanceX, -distanceY);
            return true;
        }
        @Override
        public boolean onSingleTapUp(@NonNull MotionEvent e) {
            listener.onBoxClick(e.getX(), e.getY());
            return false;
        }
    }

    //this listener is responsible for zoom
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            listener.onScale(detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY());
            return true;
        }
    }

    public interface TouchListener {
        void onScale(float scaleFactor, float focusX, float focusY);
        void onScroll(float deltaX, float deltaY);
        void onBoxClick(float x, float y);
    }
}


