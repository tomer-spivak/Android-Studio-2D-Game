package tomer.spivak.androidstudio2dgame.modelObjects;

import java.util.Timer;
import java.util.TimerTask;

import tomer.spivak.androidstudio2dgame.logic.Building;
import tomer.spivak.androidstudio2dgame.logic.Position;
import tomer.spivak.androidstudio2dgame.logic.modelEnums.BuildingState;

public class ExplodingBuilding extends Building {
    private final float damage;

    public ExplodingBuilding(float health, Position pos, float damage) {
        super(health, pos, "explodingtower");
        this.damage = damage;
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
