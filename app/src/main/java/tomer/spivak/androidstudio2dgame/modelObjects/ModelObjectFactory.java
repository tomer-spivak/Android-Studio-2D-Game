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
        typeMap.put("ARCHERTOWER", (position) -> new Turret(100, 20, 2,
                position, TurretType.ARCHERTOWER, 2000));
        typeMap.put("MONSTER", (position) -> new Enemy(100, 10, 1f, position
                , EnemyType.MONSTER, 1000));
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
