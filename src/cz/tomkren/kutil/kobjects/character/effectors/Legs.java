package cz.tomkren.kutil.kobjects.character.effectors;

import cz.tomkren.helpers.AB;
import net.phys2d.raw.Body;

/** Created by user on 7. 8. 2015.*/

public interface Legs {

    enum Action {WALK, JUMP, STOP}
    enum Dir {LEFT, FRONT, RIGHT}

    String move(Action legsAction, Dir dir);

    void updateDir(Dir dir);
    void setAnimationFrame(String animationName, int frameIndex);

    Dir getCurrentDir();
    Body getBody();

    void stepInform();

    void addMovement(Action action, LegMovement movement);


    default void setAnimationFrame(AB<String,Integer> frame) {
        setAnimationFrame(frame._1(), frame._2());
    }


    static Dir fromString(String str) {
        switch (str) {
            case "right" : return Dir.RIGHT;
            case "left"  : return Dir.LEFT;
            case "front" : return Dir.FRONT;
            default      : return null;
        }
    }


    static float dirToFloat(Dir d) {
        switch (d) {
            case RIGHT: return 1f;
            case LEFT: return -1f;
            case FRONT: return 0f;
        }
        throw new Error("Direction "+d+" is inconvertible to float.");
    }

}
