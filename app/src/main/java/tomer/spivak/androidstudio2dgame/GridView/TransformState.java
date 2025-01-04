package tomer.spivak.androidstudio2dgame.GridView;

import android.util.Log;

class TransformState {
    private final float positionX;
    private final float positionY;
    private final float scale;
    private final float initialX;
    private final float initialY;

    public TransformState(float positionX, float positionY, float scale) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.scale = scale;
        this.initialX = positionX;
        this.initialY = positionY;
    }

    // Getters
    public float getPositionX() { return positionX; }
    public float getPositionY() { return positionY; }
    public float getScale() { return scale; }
    public float getInitialX() { return initialX; }
    public float getInitialY() { return initialY; }

    // Transformation methods that return new states
    public TransformState translate(float deltaX, float deltaY) {
        Log.d("translate", deltaX + " " + deltaY);
        Log.d("translate", positionX + " " + positionY);
        return new TransformState(
                positionX + deltaX,
                positionY + deltaY,
                scale
        );
    }

    // Utility method for creating new states
    public TransformState with(float newX, float newY, float newScale) {
        return new TransformState(newX, newY, newScale);
    }


}

