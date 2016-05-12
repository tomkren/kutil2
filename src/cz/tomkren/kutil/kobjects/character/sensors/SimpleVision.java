package cz.tomkren.kutil.kobjects.character.sensors;

import cz.tomkren.kutil.core.KObject;
import cz.tomkren.kutil.items.Int2D;
import cz.tomkren.kutil.kobjects.coin.Coin;

/** Created by user on 7. 8. 2015.*/

public class SimpleVision {

    private KObject parent;


    public SimpleVision(KObject parent) {
        this.parent = parent;
    }

    public Int2D getNearestCoinFrom(Int2D pos) {
        int  minDist = Integer.MAX_VALUE;
        Coin minCoin = null;

        // TODO asi předělat aby si svět pamatoval své mince, aby se nemuselo projíždět všecko
        for (KObject obj : parent.inside()) {
            if (obj instanceof Coin) {
                int dist = pos.distance2(obj.pos());
                if (dist < minDist) {
                    minDist = dist;
                    minCoin = (Coin) obj;
                }
            }
        }

        if (minCoin == null) {return null;}
        return pos.direction(minCoin.pos());
    }





}
