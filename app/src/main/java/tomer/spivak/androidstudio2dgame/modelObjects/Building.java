package tomer.spivak.androidstudio2dgame.modelObjects;

import java.util.Map;

import tomer.spivak.androidstudio2dgame.model.Position;
import tomer.spivak.androidstudio2dgame.modelEnums.BuildingState;

public abstract class Building extends ModelObject {

    protected BuildingState state;
    protected final int price;

    public Building(float health, Position pos, int price) {
        super(health, pos);
        this.state = BuildingState.IDLE;
        this.price = price;
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
    public Object toMap() {
        Map buildingData = (Map) super.toMap();
        buildingData.put("health", health);
        return buildingData;
    }
}