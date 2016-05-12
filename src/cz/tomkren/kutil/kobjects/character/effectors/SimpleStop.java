package cz.tomkren.kutil.kobjects.character.effectors;

/** Created by user on 8. 8. 2015.*/

public class SimpleStop implements LegMovement {

    private Legs legs;

    public SimpleStop(Legs legs) {
        this.legs = legs;
    }

    @Override
    public String move(Legs.Dir dir) {
        legs.updateDir(dir);
        return null;
    }

    @Override
    public void stepInform() {

    }
}
