package cz.tomkren.kutil.core;

import cz.tomkren.utils.AA;
import cz.tomkren.utils.Double2x2;
import cz.tomkren.kutil.items.Int2D;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;

/** Created by tom on 2.8.2015.*/

// TODO ty transformace by šlo dělat nativně drawPath.createTransformedShape(..)
// TODO a minimalně by se asi vyplatilo nekreslit to takle ručně ale posouvat to pomocí toho transformu..


public class SVGBone {

    private final double xBoneFrom;
    private final double yBoneFrom;
    private final List<double[]> paths;

    public SVGBone(SVGResource svgResource, AA<Int2D> bone) {
        xBoneFrom = bone._1().getX();
        yBoneFrom = bone._1().getY();
        paths = posListsToRawArrays(mkPrePaths(svgResource, bone));
    }

    public void draw(Graphics2D g, Int2D drawPos, Color lineColor, Color fillColor) {

        double dx = xBoneFrom + drawPos.getX();
        double dy = yBoneFrom + drawPos.getY();

        for (double[] path : paths) {

            GeneralPath drawPath = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
            drawPath.moveTo(path[0] + dx, path[1] + dy);

            for (int i = 2; i < path.length; i += 6) {
                drawPath.curveTo(path[i] + dx, path[i + 1] + dy,
                        path[i + 2] + dx, path[i + 3] + dy,
                        path[i + 4] + dx, path[i + 5] + dy);

            }

            drawPath.closePath();

            g.setColor(fillColor);
            g.fill(drawPath);
            g.setColor(lineColor);
            g.draw(drawPath);
        }
    }

    private static List<List<AA<Double>>> mkPrePaths(SVGResource svgResource, AA<Int2D> bone) {

        double xSvgVector = svgResource.getTo()._1() - svgResource.getFrom()._1();
        double ySvgVector = svgResource.getTo()._2() - svgResource.getFrom()._2();

        Double2x2 translateToOrigin = new Double2x2.ByXY((x,y)->x-svgResource.getFrom()._1(),(x,y)->y-svgResource.getFrom()._2());

        double xBoneVector = bone._2().getX() - bone._1().getX();
        double yBoneVector = bone._2().getY() - bone._1().getY();
        double boneLength = Math.sqrt(xBoneVector * xBoneVector + yBoneVector * yBoneVector);
        double svgLength  = Math.sqrt(xSvgVector  * xSvgVector  + ySvgVector  * ySvgVector);
        double scale = boneLength / svgLength;

        Double2x2 scaling = new Double2x2.ByXY((x,y)->x*scale,(x,y)->y*scale);

        double cosAlpha = (xSvgVector*xBoneVector + ySvgVector*yBoneVector) / (svgLength * boneLength);
        double sinAlpha = Math.sin(Math.acos(cosAlpha));
        double clockwiseFactor = xSvgVector*yBoneVector - ySvgVector*xBoneVector > 0 ? -1 : 1;

        Double2x2 rotation = new Double2x2.ByXY((x,y)-> cosAlpha*x + clockwiseFactor*sinAlpha*y ,(x,y)-> -clockwiseFactor*sinAlpha*x + cosAlpha*y );

        /* Z piety:
        Int2D backDelta = bone._1().plus(drawPos);
        Double2x2 translateBack = new Double2x2.ByXY((x,y)->x+backDelta.getX(),(x,y)->y+backDelta.getY()); */


        List<List<AA<Double>>> ret = new ArrayList<>(svgResource.getPaths().size());
        for (List<AA<Double>> path : svgResource.getPaths()) {
            ret.add(mkPrePath(path, translateToOrigin.dot(scaling).dot(rotation)));
        }

        return ret;

    }


    private static List<AA<Double>> mkPrePath(List<AA<Double>> resourcePath, Double2x2 transform) {
        List<AA<Double>> ret = new ArrayList<>(resourcePath.size());

        AA<Double> nextStart = resourcePath.get(0);
        ret.add(transform.apply(nextStart));

        for (int i = 1; i < resourcePath.size(); i+=3) {

            AA<Double> x1 = AA.add(nextStart, resourcePath.get(i));
            AA<Double> x2 = AA.add(nextStart, resourcePath.get(i+1));
            AA<Double> x3 = AA.add(nextStart, resourcePath.get(i+2));

            ret.add(transform.apply(x1));
            ret.add(transform.apply(x2));
            ret.add(transform.apply(x3));

            nextStart = x3;
        }

        return ret;
    }

    private static List<double[]> posListsToRawArrays(List<List<AA<Double>>> prePaths) {

        List<double[]> ret = new ArrayList<>(prePaths.size());

        for (List<AA<Double>> prePath : prePaths) {

            double[] subRet = new double[2 * prePath.size()];

            int i = 0;
            for (AA<Double> point : prePath) {
                subRet[i] = point._1();
                subRet[i + 1] = point._2();
                i += 2;
            }

            ret.add(subRet);
        }

        return ret;
    }
}
