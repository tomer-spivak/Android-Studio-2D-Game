package tomer.spivak.androidstudio2dgame.modelObjects;

public interface IDamageable {
    void takeDamage(float damage); // Called when entity is hit
    float getHealth();
}
