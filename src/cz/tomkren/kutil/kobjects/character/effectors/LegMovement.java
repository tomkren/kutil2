package cz.tomkren.kutil.kobjects.character.effectors;

/** Created by user on 7. 8. 2015. */

public interface LegMovement {

    String move(Legs.Dir dir);

    void stepInform();

}
