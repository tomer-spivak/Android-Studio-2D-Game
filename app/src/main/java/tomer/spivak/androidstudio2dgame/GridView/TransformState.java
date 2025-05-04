package tomer.spivak.androidstudio2dgame.GridView;


class TransformState {
    private final float positionX;
    private final float positionY;
    private final float scale;

    public TransformState(float positionX, float positionY, float scale) {
        this.positionX = positionX;
        this.positionY = positionY;
        this.scale = scale;
    }

    public float getPositionX() { return positionX; }
    public float getPositionY() { return positionY; }
    public float getScale() { return scale; }

    public TransformState translate(float deltaX, float deltaY) {
        return new TransformState(
                positionX + deltaX,
                positionY + deltaY,
                scale
        );
    }

    public TransformState with(float newX, float newY, float newScale) {
        return new TransformState(newX, newY, newScale);
    }


}

