package tomer.spivak.androidstudio2dgame.modelObjects;



import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import tomer.spivak.androidstudio2dgame.model.Position;

public abstract class ModelObject implements IDamageable {
    protected Position pos;
    protected float health;

    public ModelObject(float health, Position pos) {
        this.health = health;
        this.pos = pos;
    }

    public Position getPosition() {
        return pos;
    }

    public void setPosition(Position position) {
        this.pos = position;
    }

    @Override
    public void takeDamage(float damage) {
        health -= damage;
        if (health <= 0) {
            onDeath();
        }
    }

    void onDeath() {

    }


    public void setHealth(float health) {
        this.health = health;
    }
    public float getHealth() {
        return health;
    }

    public Object toMap(){
        Map<String, Object> modelObjectData = new HashMap<>();
        modelObjectData.put("type", "modelObject"); // Store the type of object
        // monsterData.put("name", name);
        modelObjectData.put("health", health);
        return modelObjectData;
    }

    @NonNull
    @Override
    public String toString() {
        return "ModelObject{" +
                "pos=" + pos +
                ", health=" + health +
                '}';
    }
}
