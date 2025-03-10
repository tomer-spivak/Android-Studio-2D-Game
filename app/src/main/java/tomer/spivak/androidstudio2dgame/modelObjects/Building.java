package tomer.spivak.androidstudio2dgame.modelObjects;

import java.util.Map;

import tomer.spivak.androidstudio2dgame.model.Position;
import tomer.spivak.androidstudio2dgame.modelEnums.BuildingState;

public abstract class Building extends ModelObject {
    protected BuildingState state;
    public Building(float health, Position pos) {
        super(health, pos);
        this.state = BuildingState.IDLE;
    }

    public BuildingState getState() {
        return state;
    }

    public void setState(BuildingState state) {
        this.state = state;
    }

    @Override
    public Object toMap() {
        Map buildingData = (Map) super.toMap();
        buildingData.put("health", health);
        return buildingData;
    }
}