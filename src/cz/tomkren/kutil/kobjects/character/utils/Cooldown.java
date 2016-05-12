package cz.tomkren.kutil.kobjects.character.utils;

/** Created by user on 7. 8. 2015. */

public class Cooldown {

    private int coolDown;
    private boolean canDo;
    private int canCountdown;

    public Cooldown(int coolDown) {
        this.coolDown = coolDown;
        canDo = true;
        canCountdown = 0;
    }

    public boolean isReady() {
        return canDo;
    }

    public void reset() {
        canDo = false;
        canCountdown = coolDown;
    }

    public void step() {
        if (!canDo) {
            canCountdown--;
            if (canCountdown <= 0) {
                canDo = true;
            }
        }
    }


}
