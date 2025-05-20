package tomer.spivak.androidstudio2dgame.logic;

public class ExplodingBuilding extends Building {
    private final float damage;

    public ExplodingBuilding(float health, Position pos, float damage) {
        super(health, pos, "explodingtower");
        this.damage = damage;
    }

    public float getDamage() {
        return damage;
    }
}
