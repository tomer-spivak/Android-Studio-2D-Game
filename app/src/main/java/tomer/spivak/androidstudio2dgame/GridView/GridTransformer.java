package  tomer.spivak.androidstudio2dgame.GridView;

public class GridTransformer {
    private TransformState state;
    private final float minScale;
    private final float maxScale;
    private final float angle;
    private final int baseCellHeight;

    public GridTransformer(float initialX, float initialY, float minScale, float maxScale, int baseCellHeight) {
        this.baseCellHeight = baseCellHeight;
        this.state = new TransformState(initialX, initialY, 1.0f);
        this.minScale = minScale;
        this.maxScale = maxScale;
        this.angle = (float)(Math.PI * 0.18);
    }

    public void translate(float deltaX, float deltaY) {
        state = state.translate(deltaX, deltaY);
    }

    public float scale(float scaleFactor, float pivotX, float pivotY) {
        float currentScale = state.getScale();
        float newScale = currentScale * scaleFactor;

        if (isScaleInvalid(newScale)) {
            return state.getScale();
        }

        float offsetX = (pivotX - state.getPositionX()) * (1 - scaleFactor);
        float offsetY = (pivotY - state.getPositionY()) * (1 - scaleFactor);
         state = new TransformState(
                state.getPositionX() + offsetX,
                state.getPositionY() + offsetY,
                newScale
        );

        return state.getScale();
    }

    //public TransformState reset() {
      //  // Reset to initial state
        //state = new TransformState(state.getInitialX(), state.getInitialY(), 1.0f);
        //return state;
    //}

    private boolean isScaleInvalid(float scale) {
        return scale < minScale || scale > maxScale;
    }




    // Additional utility methods that might be useful
    public int getCellWidth() {
        return (int) (baseCellHeight * state.getScale() / Math.tan(angle));
    }

    public int getCellHeight() {
        return (int) (baseCellHeight * state.getScale());
    }

    public Float[] getCurrentPosition() {
        return new Float[]{state.getPositionX(), state.getPositionY()};
    }

    public float[] updateScale(float scaleFactor, float focusX, float focusY) {
        TransformState oldTransformState = state;
        float oldScale = oldTransformState.getScale();

        float newScale = scale(scaleFactor, focusX, focusY);

        float scaleRatio = newScale / oldScale;
        return new float[] {scaleRatio, newScale};
    }

    public float getScale() {
        return state.getScale();
    }
}