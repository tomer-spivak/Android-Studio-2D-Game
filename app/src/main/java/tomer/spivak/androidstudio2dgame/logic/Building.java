package tomer.spivak.androidstudio2dgame.logic;

import java.util.Map;

import tomer.spivak.androidstudio2dgame.logic.modelEnums.BuildingState;
import tomer.spivak.androidstudio2dgame.modelObjects.ModelObject;

public class Building extends ModelObject {
    protected BuildingState state;
    private long timeSinceTookDamage = 0;
    private boolean inAnimation = false;

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
            timeSinceTookDamage += deltaTime;
        if(timeSinceTookDamage >= 200){
            inAnimation = false;
            timeSinceTookDamage = 0;
            setState(BuildingState.IDLE);
        }
    }

    @Override
    public void takeDamage(float damage) {
        super.takeDamage(damage);
        inAnimation = true;
        timeSinceTookDamage = 0;
        setState(BuildingState.HURT);
    }

    public void setAnimationTime(long animationTime) {
        this.timeSinceTookDamage = animationTime;
    }

    public void setInAnimation(boolean inAnimation) {
        this.inAnimation = inAnimation;
    }

    @Override
    public Object toMap() {
        Map buildingData = (Map) super.toMap();
        buildingData.put("type", type);
        buildingData.put("state", state.name());
        buildingData.put("timeSinceTookDamage", timeSinceTookDamage);
        buildingData.put("inAnimation", inAnimation);
        return buildingData;
    }
}