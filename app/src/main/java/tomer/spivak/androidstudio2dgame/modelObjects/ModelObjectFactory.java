package tomer.spivak.androidstudio2dgame.modelObjects;


import java.util.HashMap;
import java.util.Map;

import tomer.spivak.androidstudio2dgame.model.Position;
import tomer.spivak.androidstudio2dgame.modelEnums.EnemyType;
import tomer.spivak.androidstudio2dgame.modelEnums.RuinType;
import tomer.spivak.androidstudio2dgame.modelEnums.TurretType;

public class ModelObjectFactory {
    private static final Map<String, ModelObjectCreator> typeMap = new HashMap<>();

    static {
        typeMap.put("OBELISK", (position) -> new Ruin(200, position, RuinType.OBELISK));

        typeMap.put("ARCHERTOWER", (position) -> new AOETurret(100, 20, 2,
                position, TurretType.ARCHERTOWER, 1100));

        typeMap.put("MONSTER", (position) -> new Enemy(80, 30, 3f, position
                , EnemyType.MONSTER, 2000));
    }

    public static ModelObject create(String type, Position position) {
        ModelObjectCreator creator = typeMap.get(type);
        if (creator != null) {
            return creator.create(position);
        }
        throw new IllegalArgumentException("Unknown type: " + type);
    }

    @FunctionalInterface
    interface ModelObjectCreator {
        ModelObject create(Position position);
    }
}
