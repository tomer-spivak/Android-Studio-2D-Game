package tomer.spivak.androidstudio2dgame.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tower extends Building {
    private final float attackDamage;
    private final float attackRange;

    public Tower(float health, float attackDamage, float attackRange, Position pos, String name) {
        super(health, pos, name);
        this.attackDamage = attackDamage;
        this.attackRange = attackRange;
    }

    @Override
    public void takeDamage(float damage) {
        // Implement damage handling logic here
    }

    @Override
    public void dealDamage(Damage target) {
        target.takeDamage(attackDamage);
    }


    public Enemy findTarget(List<Enemy> enemies) {
        for (Enemy enemy : enemies) {
            if (pos.distanceTo(enemy.getPosition()) <= attackRange) {
                return enemy;
            }
        }
        return null;
    }


    @Override
    public Object toMap() {
        Map<String, Object> buildingData = new HashMap<>();
        buildingData.put("type", "Building"); // Store the type of object
        buildingData.put("name", name);
        buildingData.put("health", health);
        buildingData.put("attackDamage", attackDamage);
        buildingData.put("attackRange", attackRange);
        return buildingData;
    }
}