package tomer.spivak.androidstudio2dgame.logic;

import java.util.Map;

import tomer.spivak.androidstudio2dgame.logic.modelEnums.BuildingState;
import tomer.spivak.androidstudio2dgame.modelObjects.ModelObject;

public class Building extends ModelObject {
    protected BuildingState state;
    protected long animationTime = 0;
    protected boolean inAnimation = false;

    public Building(float health, Position pos, String type) {
        super(health, pos);
        this.state = BuildingState.IDLE;
        this.type = type;
    }

    public BuildingState getState() {
        return state;
    }

    public void setState(BuildingState state) {
        this.state = state;
    }

    public void update(long deltaTime){
        if(inAnimation)
            animationTime += deltaTime;
        if(animationTime >= 200){
            inAnimation = false;
            animationTime = 0;
            setState(BuildingState.IDLE);
        }
    }

    @Override
    public void takeDamage(float damage) {
        super.takeDamage(damage);
        inAnimation = true;
        animationTime = 0;
        setState(BuildingState.HURT);
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