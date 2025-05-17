package tomer.spivak.androidstudio2dgame.logic;
import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import tomer.spivak.androidstudio2dgame.projectManagement.SoundEffectManager;

public abstract class ModelObject {
    protected Position pos;
    protected float health;
    private final float maxHealth;
    protected int soundStreamId = -1;
    protected SoundEffectManager soundEffects;
    protected String type;

    public ModelObject(float health, Position pos) {
        this.maxHealth = health;
        this.health = health;
        this.pos = pos;
    }

    public void takeDamage(float damage) {
        health -= damage;
        if (health <= 0) {
            soundEffects.stopSound(soundStreamId);
        }
    }

    public void stopSound() {
        soundEffects.stopSound(soundStreamId);
    }

    public void setSoundStreamId(int soundStreamId) {
        this.soundStreamId = soundStreamId;
    }

    public Position getPosition() {
        return pos;
    }

    public void setPosition(Position position) {
        this.pos = position;
    }

    public void setHealth(float health) {
        this.health = health;
    }

    public float getHealth() {
        return health;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public void setSoundEffects(SoundEffectManager soundEffects) {
        this.soundEffects = soundEffects;
    }

    public String getType() {
        return type;
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
}
