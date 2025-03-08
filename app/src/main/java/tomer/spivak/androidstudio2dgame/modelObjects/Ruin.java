package tomer.spivak.androidstudio2dgame.modelObjects;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import tomer.spivak.androidstudio2dgame.model.Position;
import tomer.spivak.androidstudio2dgame.modelEnums.RuinState;
import tomer.spivak.androidstudio2dgame.modelEnums.RuinType;

public class Ruin extends Building{
    private final RuinType type;
    private RuinState ruinState;

    public Ruin(float health, Position pos, RuinType type) {
        super(health, pos);
        this.type = type;
        this.ruinState = RuinState.IDLE;
    }



    @Override
    public void takeDamage(float damage) {
        super.takeDamage(damage);
        setRuinState(RuinState.HURT);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                setRuinState(RuinState.IDLE);
            }
        }, 200);
    }

    public RuinType getType() {
        return type;
    }

    public void setRuinState(RuinState ruinState) {
        this.ruinState = ruinState;
    }

    @Override
    public Object toMap() {
        Map ruinData = (Map) super.toMap();
        ruinData.put("type", type.name());
        return ruinData;
    }


}
