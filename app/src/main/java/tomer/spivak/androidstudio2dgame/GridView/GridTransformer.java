package  tomer.spivak.androidstudio2dgame.GridView;

public class GridTransformer {
    private TransformState state;
    private final float minScale;
    private final float maxScale;
    private final float baseCellHeight;
    private final float angle;
    private final float BaseCellHeight = 150f;// For isometric grid calculations

    // Constructor with all necessary constraints
    public GridTransformer(float initialX, float initialY, float minScale, float maxScale,
                         float baseCellHeight) {
        this.state = new TransformState(initialX, initialY, 1.0f);
        this.minScale = minScale;
        this.maxScale = maxScale;
        this.baseCellHeight = baseCellHeight;
        this.angle = (float)(Math.PI * 0.18); // Approximately 32.4 degrees for isometric view
    }

    public TransformState getTransformState() {
        return state;
    }

    public TransformState translate(float deltaX, float deltaY) {
        // Create new state with updated position
        TransformState newState = state.translate(deltaX, deltaY);
        // Update internal state
        state = newState;
        return state;
    }

    public TransformState scale(float scaleFactor, float pivotX, float pivotY) {
        float currentScale = state.getScale();
        float newScale = currentScale * scaleFactor;

        // Check if new scale would violate constraints
        if (isScaleInvalid(newScale)) {
            return state;
        }

        // Calculate cell dimensions at new scale


        // Calculate position adjustment to maintain pivot point
        float offsetX = (pivotX - state.getPositionX()) * (1 - scaleFactor);
        float offsetY = (pivotY - state.getPositionY()) * (1 - scaleFactor);
        //float offsetX = (pivotX * (1 - scaleFactor));
        //float offsetY = (pivotY * (1 - scaleFactor));

        // Create new state with updated scale and position
        state = new TransformState(
                state.getPositionX() + offsetX,
                state.getPositionY() + offsetY,
                newScale
        );

        return state;
    }

    public TransformState reset() {
        // Reset to initial state
        state = new TransformState(state.getInitialX(), state.getInitialY(), 1.0f);
        return state;
    }

    // Helper methods for validation
    private boolean isScaleInvalid(float scale) {
        return scale < minScale || scale > maxScale;
    }




    // Additional utility methods that might be useful
    public float getCellWidth() {
        return (float) (BaseCellHeight * state.getScale() / Math.tan(angle));
    }

    public float getCellHeight() {
        return BaseCellHeight * state.getScale();
    }
}