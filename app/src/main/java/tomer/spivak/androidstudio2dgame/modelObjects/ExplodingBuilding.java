package tomer.spivak.androidstudio2dgame.modelObjects;

import tomer.spivak.androidstudio2dgame.model.Position;

public class ExplodingBuilding extends Building{
    private final float damage;

    public ExplodingBuilding(float health, Position pos, int price, float damage) {
        super(health, pos, price, "explodingtower");
        this.damage = damage;
    }

    @Override
    void onDeath() {
        super.onDeath();
    }

    public float getDamage() {
        return damage;
    }
}
