package tomer.spivak.androidstudio2dgame.modelObjects;


import java.util.HashMap;
import java.util.Map;

import tomer.spivak.androidstudio2dgame.model.Position;

public class ModelObjectFactory {
    private static final Map<String, ModelObjectCreator> typeMap = new HashMap<>();

    static {
        typeMap.put("tower", (position) -> new Tower(100, 20, 3, position));
        typeMap.put("monster", (position) -> new Monster(50, 10, 1, position));
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
