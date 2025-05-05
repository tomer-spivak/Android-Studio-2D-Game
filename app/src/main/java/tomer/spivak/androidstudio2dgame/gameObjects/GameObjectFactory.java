package tomer.spivak.androidstudio2dgame.gameObjects;

import android.content.Context;
import android.graphics.Point;

import java.util.HashMap;
import java.util.Map;

import tomer.spivak.androidstudio2dgame.model.Position;

public class GameObjectFactory {
    private static final Map<String, GameObjectCreator> typeMap = new HashMap<>();

    static {
        typeMap.put("obelisk", new GameObjectCreator() {
            @Override
            public GameObject create(Context context, Point point, float scale, Position pos, String type, String state, String direction, float healthPercentage) {
                return new GameObject(context, point, scale, pos, type, state, direction, healthPercentage);
            }
        });
        typeMap.put("lightningtower", new GameObjectCreator() {
            @Override
            public GameObject create(Context context, Point point, float scale, Position pos, String type, String state, String direction, float healthPercentage) {
                return new GameObject(context, point, scale, pos, type, state, direction, healthPercentage);
            }
        });
        typeMap.put("mainbuilding", new GameObjectCreator() {
            @Override
            public GameObject create(Context context, Point point, float scale, Position pos, String type, String state, String direction, float healthPercentage) {
                return new GameObject(context, point, scale, pos, type, state, direction, healthPercentage);
            }
        });
        typeMap.put("explodingtower", new GameObjectCreator() {
            @Override
            public GameObject create(Context context, Point point, float scale, Position pos, String type, String state, String direction, float healthPercentage) {
                return new GameObject(context, point, scale, pos, type, state, direction, healthPercentage);
            }
        });
    }

    public static GameObject create(
            Context context,
            Point point,
            String type,
            float scale,
            Position pos,
            String state,
            String direction,
            float healthPercentage
    ) {
        if ("monster".equals(type)) {
            return new GameObject(context, point, scale, pos, type, state, direction, healthPercentage);
        }

        GameObjectCreator creator = typeMap.get(type);
        if (creator != null) {
            return creator.create(context, point, scale, pos, type, state, direction, healthPercentage);
        }

        throw new IllegalArgumentException("Unknown GameObject type: " + type);
    }

    public interface GameObjectCreator {
        GameObject create(
                Context context,
                Point point,
                float scale,
                Position pos,
                String type,
                String state,
                String direction,
                float healthPercentage
        );
    }
}
