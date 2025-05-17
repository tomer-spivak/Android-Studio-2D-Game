package tomer.spivak.androidstudio2dgame.modelObjects;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import tomer.spivak.androidstudio2dgame.logic.Position;
import tomer.spivak.androidstudio2dgame.modelEnums.BuildingState;

public class Building extends ModelObject {

    protected BuildingState state;
    protected final int price;

    public Building(float health, Position pos, int price, String type) {
        super(health, pos);
        this.state = BuildingState.IDLE;
        this.price = price;
        this.type = type;
    }

    public BuildingState getState() {
        return state;
    }

    public void setState(BuildingState state) {
        this.state = state;
    }

    public int getPrice(){
       return price;
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

    @Override
    public Object toMap() {
        Map buildingData = (Map) super.toMap();
        buildingData.put("health", health);
        buildingData.put("type", type);
        buildingData.put("state", state.name());
        return buildingData;
    }
}