package tomer.spivak.androidstudio2dgame.modelObjects;

import java.util.Timer;
import java.util.TimerTask;

import tomer.spivak.androidstudio2dgame.logic.Position;
import tomer.spivak.androidstudio2dgame.modelEnums.BuildingState;

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
    @Override
    public void takeDamage(float damage) {
        super.takeDamage(damage);
        setState(BuildingState.HURT);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                setState(BuildingState.IDLE);
            }
        }, 200);
    }
}
