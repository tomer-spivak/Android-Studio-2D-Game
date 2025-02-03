package tomer.spivak.androidstudio2dgame.model;


public abstract class Enemy extends ModelObject {
    protected final float damage;
    protected final float movementSpeed;

    public Enemy(float health, float damage, float movementSpeed, Position pos, String name) {
        super(health, pos, name); // Call base constructor
        this.damage = damage;
        this.movementSpeed = movementSpeed;
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


}