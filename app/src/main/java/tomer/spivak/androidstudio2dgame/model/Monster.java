package tomer.spivak.androidstudio2dgame.model;

import java.util.HashMap;
import java.util.Map;

public class Monster extends Enemy {
    public Monster(int health, int damage, int movementSpeed, Position position, String name) {
        super(health, damage, movementSpeed, position, name);


    }

    @Override
    public Object toMap() {
        Map<String, Object> monsterData = new HashMap<>();
        monsterData.put("type", "Monster"); // Store the type of object
        monsterData.put("name", name);
        monsterData.put("health", health);
        monsterData.put("damage", damage);
        monsterData.put("movementSpeed", movementSpeed);
        return monsterData;
    }
}
