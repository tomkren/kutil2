package cz.tomkren.kutil.kobjects.character.effectors;

import cz.tomkren.kutil.kobjects.character.utils.Cooldown;
import net.phys2d.math.Vector2f;
import net.phys2d.raw.Body;
import org.json.JSONObject;

/** Created by user on 7. 8. 2015. */

public class SimpleComboWalk implements LegMovement {

    private Legs legs;
    private Cooldown cooldown;

    private float maxSpeed;
    private float stepSize;

    public SimpleComboWalk(JSONObject walkConfig, Legs legs) {
        this.legs = legs;

        cooldown = new Cooldown(walkConfig.getInt("coolDown"));
        maxSpeed = (float) walkConfig.getDouble("maxSpeed");
        stepSize = (float) walkConfig.getDouble("stepSize");
    }

    @Override
    public void stepInform() {
        cooldown.step();
    }

    @Override
    public String move(Legs.Dir dir) {

        if (cooldown.isReady()) {
            cooldown.reset();

            Legs.Dir currentDir = legs.getCurrentDir();
            if (currentDir != dir && currentDir != Legs.Dir.FRONT) {
                legs.updateDir(Legs.Dir.FRONT);
                return "Jen jsem se otočil.";
            } else {

                Body body = legs.getBody();
                if (body.getVelocity().length() < maxSpeed) {
                    float d = Legs.dirToFloat(dir);
                    body.adjustVelocity(new Vector2f(d * stepSize, -5f));           // todo vytknout
                    body.adjustPosition(new Vector2f(d * stepSize * 0.15f, 0f));    // todo todo
                }


                legs.updateDir(dir);
                return "Walk.";
            }

        } else {
            return null;
        }

    }
}
