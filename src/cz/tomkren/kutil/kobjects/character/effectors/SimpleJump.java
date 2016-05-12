package cz.tomkren.kutil.kobjects.character.effectors;

import cz.tomkren.helpers.AB;
import cz.tomkren.kutil.kobjects.character.utils.Cooldown;
import net.phys2d.math.Vector2f;
import org.json.JSONObject;

/** Created by user on 7. 8. 2015. */

public class SimpleJump implements LegMovement {

    private Legs legs;
    private Cooldown cooldown;

    private float xJump;
    private float yJump;
    private float upJump;

    private static final AB<String,Integer> RIGHT_FRAME = new AB<>("walkRight",2);
    private static final AB<String,Integer> LEFT_FRAME  = new AB<>("walkLeft", 2);
    private static final AB<String,Integer> FRONT_FRAME = new AB<>("standFront",0);


    public SimpleJump(JSONObject jumpConfig, Legs legs) {
        this.legs = legs;

        cooldown = new Cooldown(jumpConfig.getInt("coolDown"));
        xJump = (float) jumpConfig.getDouble("x");
        yJump = (float) jumpConfig.getDouble("y");

        upJump = (float) Math.sqrt(xJump*xJump + yJump*yJump);
    }

    @Override
    public void stepInform() {
        cooldown.step();
    }

    @Override
    public String move(Legs.Dir dir) {

        // TODO hax : null vzniká pro dirStr = default
        if (dir == null) {
            dir = legs.getCurrentDir();
        } else {
            legs.updateDir(dir);
        }

        if (cooldown.isReady()) {
            cooldown.reset();

            switch (dir) {
                case RIGHT: return jump(xJump, -yJump, RIGHT_FRAME);
                case LEFT:  return jump(-xJump, -yJump, LEFT_FRAME);
                case FRONT: return jump(0, -upJump, FRONT_FRAME);
            }

        }
        return null;
    }

    private String jump(float x, float y, AB<String,Integer> frame) {
        legs.getBody().adjustVelocity(new Vector2f(x,y));
        legs.setAnimationFrame(frame);
        return "JUMP!";
    }

}
