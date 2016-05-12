package cz.tomkren.kutil.kobjects.character.brains;

import cz.tomkren.kutil.items.Int2D;
import cz.tomkren.kutil.kobjects.character.GameCharacter;
import cz.tomkren.kutil.kobjects.character.effectors.Legs;
import cz.tomkren.kutil.kobjects.character.sensors.SimpleVision;

/** Created by user on 8. 8. 2015.*/

public class SimpleBrain {

    // TODO .. sem přesunout funkcionalitu performAction()
    // něco jako ..   void performAction(Sensors , Effectors)

    private GameCharacter character;


    public SimpleBrain(GameCharacter character) {
        this.character = character;
    }

    public void performAction() {

        SimpleVision vision = character.getVision();
        Legs legs = character.getLegs();

        Int2D nearestCoinDirection = vision.getNearestCoinFrom(character.pos());

        if (nearestCoinDirection == null) {
            legs.move(Legs.Action.STOP, Legs.Dir.FRONT);
        } else {
            if (nearestCoinDirection.getX() > 0) {
                legs.move(Legs.Action.WALK, Legs.Dir.RIGHT);
            } else {
                legs.move(Legs.Action.WALK, Legs.Dir.LEFT);
            }
        }
    }
}
