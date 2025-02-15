package tomer.spivak.androidstudio2dgame.modelObjects;

import java.util.Map;

import tomer.spivak.androidstudio2dgame.model.Position;

public abstract class Building extends ModelObject {
    public Building(float health, Position pos) {
        super(health, pos);

    }

    @Override
    public Object toMap() {
        Map buildingData = (Map) super.toMap();
        buildingData.put("health", health);
        return buildingData;
    }
}