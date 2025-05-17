package tomer.spivak.androidstudio2dgame.modelObjects;
import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import tomer.spivak.androidstudio2dgame.logic.Position;
import tomer.spivak.androidstudio2dgame.music.SoundEffectManager;

public abstract class ModelObject implements IDamageable {
    protected Position pos;
    protected float health;
    protected float maxHealth;
    protected int soundStreamId = -1;
    protected SoundEffectManager soundEffects;
    protected String type;

    public void setSoundStreamId(int soundStreamId) {
        this.soundStreamId = soundStreamId;
    }

    public ModelObject(float health, Position pos) {
        this.maxHealth = health;
        this.health = health;
        this.pos = pos;
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

    void onDeath() {
        soundEffects.stopSound(soundStreamId);
    }

    public void setHealth(float health) {
        this.health = health;
    }

    public float getHealth() {
        return health;
    }

    public Object toMap(){
        Map<String, Object> modelObjectData = new HashMap<>();
        modelObjectData.put("type", "modelObject");
        modelObjectData.put("health", health);
        return modelObjectData;
    }

    @NonNull
    @Override
    public String toString() {
        return "ModelObject{" +
                "pos=" + pos +
                ", health=" + health +
                '}';
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public void setSoundEffects(SoundEffectManager soundEffects) {
        this.soundEffects = soundEffects;
    }

    public void stopSound() {
        soundEffects.stopSound(soundStreamId);
    }

    public String getType() {
        return type;
    }
}
