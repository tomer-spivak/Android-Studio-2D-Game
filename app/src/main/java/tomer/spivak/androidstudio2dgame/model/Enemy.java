package tomer.spivak.androidstudio2dgame.model;


import java.util.Map;

public abstract class Enemy extends ModelObject {
    protected final float damage;
    protected final float movementSpeed;
    protected Direction currentDirection;

    public Enemy(float health, float damage, float movementSpeed, Position pos) {
        super(health, pos); // Call base constructor
        this.damage = damage;
        this.movementSpeed = movementSpeed;
        this.currentDirection = Direction.DOWNRIGHT;
        currentDirection.ordinal();
    }

    @Override
    public void takeDamage(float damage) {

    }

    @Override
    public void dealDamage(Damage target) {
        target.takeDamage(damage); // Attack logic
    }



    // Enemy-specific logic (e.g., movement)
    public void moveTowards(Position target) {
        // ...
    }

    @Override
    public Object toMap() {
        Map enemyData = (Map) super.toMap();
        enemyData.put("damage", damage);
        enemyData.put("movementSpeed", movementSpeed);
        enemyData.put("type", "enemy");
        return enemyData;
    }

    public void setCurrentDirection(Direction currentDirection) {
        this.currentDirection = currentDirection;
    }

    public int getCurrentDirection() {
        return currentDirection.ordinal();
    }

}