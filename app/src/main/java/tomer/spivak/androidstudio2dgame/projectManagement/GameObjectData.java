package tomer.spivak.androidstudio2dgame.projectManagement;

public class GameObjectData {
    private final String type;
    private final int x, y;
    private final String state, direction;
    private final float healthPercentage;

    public GameObjectData(String type, int x, int y, String state, String direction, float healthPercentage) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.state = state;
        this.direction = direction;
        this.healthPercentage = healthPercentage;
    }

    public String getType() {
        return type;
    }

    public int getY() {
        return y;
    }

    public int getX() {
        return x;
    }

    public String getDirection() {
        return direction;
    }

    public String getState() {
        return state;
    }

    public float getHealthPercentage() {
        return healthPercentage;
    }
}
