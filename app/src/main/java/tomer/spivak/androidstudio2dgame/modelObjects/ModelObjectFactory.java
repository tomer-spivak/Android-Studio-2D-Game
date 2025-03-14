package tomer.spivak.androidstudio2dgame.modelObjects;

import java.util.HashMap;
import java.util.Map;

import tomer.spivak.androidstudio2dgame.model.Position;
import tomer.spivak.androidstudio2dgame.modelAnimations.BreatheFire;
import tomer.spivak.androidstudio2dgame.modelEnums.AttackType;
import tomer.spivak.androidstudio2dgame.modelEnums.DifficultyLevel;
import tomer.spivak.androidstudio2dgame.modelEnums.EnemyType;
import tomer.spivak.androidstudio2dgame.modelEnums.RuinType;
import tomer.spivak.androidstudio2dgame.modelEnums.TurretType;

public class ModelObjectFactory {
    private static final Map<String, ModelObjectCreator> typeMap = new HashMap<>();

    static {
        typeMap.put("OBELISK", (position, difficulty) ->
                new Ruin(getBuildingHealthByDifficulty(200, difficulty), position,
                        RuinType.OBELISK));

        typeMap.put("LIGHTNINGTOWER", (position, difficulty) ->
                new AOETurret(getBuildingHealthByDifficulty(100, difficulty),
                        getTurretDamageByDifficulty(2, difficulty), 4, position,
                        TurretType.LIGHTNINGTOWER, 500, AttackType.LIGHTNING));

        typeMap.put("MONSTER", (position, difficulty) ->
                new Enemy(getEnemyHealthByDifficulty(80, difficulty),
                        getEnemyDamageByDifficulty(30, difficulty),
                        getSpeedByDifficulty(3f, difficulty), position, EnemyType.MONSTER,
                        1000, new BreatheFire()));
    }




    public static ModelObject create(String type, Position position, DifficultyLevel difficulty) {
        ModelObjectCreator creator = typeMap.get(type);
        if (creator != null) {
            return creator.create(position, difficulty);
        }
        throw new IllegalArgumentException("Unknown type: " + type);
    }

    @FunctionalInterface
    interface ModelObjectCreator {
        ModelObject create(Position position, DifficultyLevel difficulty);
    }
    private static float getBuildingHealthByDifficulty(int base, DifficultyLevel difficulty) {
        switch (difficulty) {
            case EASY: return base * 2;
            case HARD: return (float) (base * 0.75);
            default: return base;
        }
    }
    private static float getEnemyDamageByDifficulty(int base, DifficultyLevel difficulty) {
        switch (difficulty) {
            case EASY: return (int) (base * 0.75);
            case HARD: return (int) (base * 1.5);
            default: return base;
        }
    }

    private static float getEnemyHealthByDifficulty(int base, DifficultyLevel difficulty) {
        switch (difficulty) {
            case EASY: return (int) (base * 0.75);
            case HARD: return (int) (base * 1.5);
            default: return base;
        }
    }

    private static float getTurretDamageByDifficulty(int base, DifficultyLevel difficulty) {
        switch (difficulty) {
            case EASY: return (int) (base * 1.5);
            case HARD: return (int) (base * 0.75);
            default: return base;
        }
    }

    private static float getSpeedByDifficulty(float base, DifficultyLevel difficulty) {
        switch (difficulty) {
            case EASY: return base * 0.75f;
            case HARD: return base * 1.4f;
            default: return base;
        }
    }
}
