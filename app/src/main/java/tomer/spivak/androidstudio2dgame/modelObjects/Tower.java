package tomer.spivak.androidstudio2dgame.modelObjects;

import java.util.List;
import java.util.Map;

import tomer.spivak.androidstudio2dgame.model.Position;

public class Tower extends Building {
    private final float attackDamage;
    private final float attackRange;

    public Tower(float health, float attackDamage, float attackRange, Position pos) {
        super(health, pos);
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
        Map towerData = (Map) super.toMap();
        towerData.put("type", "tower"); // Store the type of object
        towerData.put("attackDamage", attackDamage);
        towerData.put("attackRange", attackRange);
        return towerData;
    }
}