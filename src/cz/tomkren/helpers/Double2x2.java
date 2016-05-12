package cz.tomkren.helpers;

import java.util.function.BiFunction;

/** Created by tom on 30.7.2015. */

public interface Double2x2 {

    AA<Double> apply(double x,double y);

    default AA<Double> apply(AA<Double> arg) {
        return apply(arg._1(),arg._2());
    }


    class ByXY implements Double2x2 {

        private final BiFunction<Double,Double,Double> xTransform;
        private final BiFunction<Double,Double,Double> yTransform;

        public ByXY(BiFunction<Double, Double, Double> xTransform, BiFunction<Double, Double, Double> yTransform) {
            this.xTransform = xTransform;
            this.yTransform = yTransform;
        }

        public AA<Double> apply(double x,double y) {
            return new AA<>(xTransform.apply(x,y), yTransform.apply(x,y));
        }

    }

    Double2x2 ID = new ByXY((x,y)->x,(x,y)->y);



    default Double2x2 dot(Double2x2 g) {
        return (x,y) -> g.apply(apply(x,y));
    }

}
