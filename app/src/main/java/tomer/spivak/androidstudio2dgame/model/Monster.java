package tomer.spivak.androidstudio2dgame.model;


import java.util.Map;

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
