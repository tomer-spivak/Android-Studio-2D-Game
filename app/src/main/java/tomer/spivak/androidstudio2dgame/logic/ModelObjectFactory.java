package tomer.spivak.androidstudio2dgame.logic;

import tomer.spivak.androidstudio2dgame.logic.modelEnums.DifficultyLevel;
import tomer.spivak.androidstudio2dgame.modelObjects.Turret;

public class ModelObjectFactory {
    public static ModelObject create(String type, Position position, DifficultyLevel difficulty) {
        switch (type) {
            case "mainbuilding":
                return new Building(getBuildingValueForDifficulty(500, difficulty), position, "mainbuilding");
            case "lightningtower":
                return new Turret(getBuildingValueForDifficulty(100, difficulty), getBuildingValueForDifficulty(10, difficulty), position, 1500);
            case "monster":
                return new Enemy(getEnemyValueForDifficulty(80, difficulty), getEnemyValueForDifficulty(30, difficulty), 2.5f, position,
                        500, getRewardByDifficulty(difficulty));
            case "explodingtower":
                return new ExplodingBuilding(getBuildingValueForDifficulty(10, difficulty), position, getBuildingValueForDifficulty(100, difficulty));
            default:
                return new Building(getBuildingValueForDifficulty(200, difficulty), position, "obelisk");
        }
    }

    public static int getPrice(String selectedBuildingType) {
        switch (selectedBuildingType) {
            case "lightningtower":
                return 3000;
            case "explodingtower":
                return 2000;
            default:
                return 1000;
        }
    }

    private static float getBuildingValueForDifficulty(int base, DifficultyLevel difficultyLevel) {
        switch (difficultyLevel) {
            case EASY: return base * 1.25f;
            case HARD: return base * 0.75f;
            default: return base;
        }
    }

    private static float getEnemyValueForDifficulty(int base, DifficultyLevel difficultyLevel) {
        switch (difficultyLevel) {
            case EASY: return base * 0.75f;
            case HARD: return base * 1.25f;
            default:   return base;
        }
    }

    private static int getRewardByDifficulty(DifficultyLevel difficultyLevel) {
        switch (difficultyLevel) {
            case EASY: return 200 / 2;
            case HARD: return 200 * 2;
            default:   return 200;
        }
    }
}
