package cz.tomkren.kutil.kobjects.coin;

import cz.tomkren.kutil.core.KAtts;
import cz.tomkren.kutil.core.KObject;
import cz.tomkren.kutil.core.Kutil;
import cz.tomkren.kutil.shapes.AnimationShape;

/** Created by tom on 3.8.2015. */

public class Coin extends KObject {

    private AnimationShape shape;
    private int step;

    public Coin(KAtts kAtts, Kutil kutil) {
        super(kAtts, kutil);
        create();
    }

    public Coin(KObject b, Kutil kutil) {
        super(b, kutil);
        create();
    }

    @Override
    public KObject copy() {
        return new Coin(this, kutil());
    }

    private void create() {
        setType("coin");
        setIsPhysical(true);
        setIsAttached(true);

        shape = new AnimationShape("/cz/tomkren/kutil/kobjects/coin/coin.json");
        setShape(shape);

        step = 0;
    }

    @Override
    public void step() {
        super.step();

        shape.setAnimationFrame("basicRoll",step/10);

        step++;
    }
}
