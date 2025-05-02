package tomer.spivak.androidstudio2dgame.modelObjects;

import java.util.HashMap;
import java.util.Map;

import tomer.spivak.androidstudio2dgame.model.Position;
import tomer.spivak.androidstudio2dgame.modelEnums.DifficultyLevel;

public class ModelObjectFactory {
    private static final Map<String, ModelObjectCreator> typeMap = new HashMap<>();

    static {
        typeMap.put("OBELISK", (position, difficulty) ->
                new Building(getBuildingHealthByDifficulty(200, difficulty), position, 1000));

        typeMap.put("LIGHTNINGTOWER", (position, difficulty) ->
                new Turret(getBuildingHealthByDifficulty(100, difficulty), getTurretDamageByDifficulty(20, difficulty),
                        4, position, 1500, 3000));

        typeMap.put("MONSTER", (position, difficulty) ->
                new Enemy(getEnemyHealthByDifficulty(80, difficulty), getEnemyDamageByDifficulty(30, difficulty),
                      2.5f, position, 500, getRewardByDifficulty(100, difficulty)));

    }




    public static ModelObject create(String type, Position position, DifficultyLevel difficulty) {
        ModelObjectCreator creator = typeMap.get(type);
        if (creator != null) {
            return creator.create(position, difficulty);
        }
        throw new IllegalArgumentException("Unknown type: " + type);
    }

    public static int getPrice(String selectedBuildingType) {
        switch (selectedBuildingType) {
            case "OBELISK": return 1000;
            case "LIGHTNINGTOWER": return 3000;
        }
        return 0;
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
            case EASY: return (int) (base * 1.25);
            case HARD: return (int) (base * 0.75);
            default: return base;
        }
    }

    private static int getRewardByDifficulty(int i, DifficultyLevel difficulty) {
        switch (difficulty) {
            case EASY: return i * 2;
            case HARD: return i / 2;
            default: return i;
        }
    }
}
