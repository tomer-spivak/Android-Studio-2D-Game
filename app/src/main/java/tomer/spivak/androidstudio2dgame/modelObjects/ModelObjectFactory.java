package tomer.spivak.androidstudio2dgame.modelObjects;

import java.util.HashMap;
import java.util.Map;

import tomer.spivak.androidstudio2dgame.logic.Building;
import tomer.spivak.androidstudio2dgame.logic.Enemy;
import tomer.spivak.androidstudio2dgame.logic.Position;
import tomer.spivak.androidstudio2dgame.logic.modelEnums.DifficultyLevel;

public class ModelObjectFactory {
    private static final Map<String, ModelObjectCreator> typeMap = new HashMap<>();

    static {
        typeMap.put("obelisk", new ModelObjectCreator() {
            @Override
            public ModelObject create(Position position, DifficultyLevel difficulty) {
                return new Building(getBuildingHealthByDifficulty(200, difficulty), position, "obelisk");
            }
        });

        typeMap.put("mainbuilding", new ModelObjectCreator() {
            @Override
            public ModelObject create(Position position, DifficultyLevel difficulty) {
                return new Building(getBuildingHealthByDifficulty(500, difficulty), position, "mainbuilding");
            }
        });

        typeMap.put("lightningtower", new ModelObjectCreator() {
            @Override
            public ModelObject create(Position position, DifficultyLevel difficulty) {
                return new Turret(getBuildingHealthByDifficulty(100, difficulty), getBuildingDamageByDifficulty(20, difficulty),
                        4, position, 1500);
            }
        });

        typeMap.put("monster", new ModelObjectCreator() {
            @Override
            public ModelObject create(Position position, DifficultyLevel difficulty) {
                return new Enemy(getEnemyHealthByDifficulty(80, difficulty), getEnemyDamageByDifficulty(30, difficulty),
                        2.5f, position, 500, getRewardByDifficulty(100, difficulty));
            }
        });
        typeMap.put("explodingtower", new ModelObjectCreator(){

            @Override
            public ModelObject create(Position position, DifficultyLevel difficulty) {
                return new ExplodingBuilding(getBuildingHealthByDifficulty(10, difficulty), position,
                        getBuildingDamageByDifficulty(100, difficulty));
            }
        });

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
            case "obelisk": return 1000;
            case "lightningtower": return 3000;
            case "explodingtower": return 2000;
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

    private static float getBuildingDamageByDifficulty(int base, DifficultyLevel difficulty) {
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
