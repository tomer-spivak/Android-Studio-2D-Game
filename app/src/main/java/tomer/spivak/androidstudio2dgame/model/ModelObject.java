package tomer.spivak.androidstudio2dgame.model;


import java.util.HashMap;
import java.util.Map;

public abstract class ModelObject implements Damage {
    protected Position pos;
    protected float health;
    //protected String name;

    public ModelObject(float health, Position pos) {
        this.health = health;
        this.pos = pos;
      //this.name = name;
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

    private void onDeath() {

    }

//    public String getName() {
//        return name;
//    }
//    public void setName(String name) {
//        this.name = name;
//    }

    public void setHealth(float health) {
        this.health = health;
    }




    public Object toMap(){
        Map<String, Object> modelObjectData = new HashMap<>();
        modelObjectData.put("type", "modelObject"); // Store the type of object
        // monsterData.put("name", name);
        modelObjectData.put("health", health);
        return modelObjectData;
    }

    @Override
    public String toString() {
        return "ModelObject{" +
                "pos=" + pos +
                ", health=" + health +
                '}';
    }
}
