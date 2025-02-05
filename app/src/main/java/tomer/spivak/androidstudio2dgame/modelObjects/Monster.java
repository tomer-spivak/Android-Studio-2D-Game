package tomer.spivak.androidstudio2dgame.modelObjects;


import java.util.Map;

import tomer.spivak.androidstudio2dgame.model.Position;

public class Monster extends Enemy {
    public Monster(int health, int damage, int movementSpeed, Position position) {
        super(health, damage, movementSpeed, position);


    }

    @Override
    public Object toMap() {
        Map monsterData = (Map) super.toMap();
        monsterData.put("type", "monster");
        return monsterData;
    }
}
