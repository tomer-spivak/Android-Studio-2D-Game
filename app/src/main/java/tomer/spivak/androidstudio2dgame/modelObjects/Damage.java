package tomer.spivak.androidstudio2dgame.modelObjects;

public interface Damage {
    void takeDamage(float damage); // Called when entity is hit
    void dealDamage(Damage target); // Called when entity attacks
}
