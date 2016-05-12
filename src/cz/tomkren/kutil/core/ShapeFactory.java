package cz.tomkren.kutil.core;


import cz.tomkren.utils.AB;
import cz.tomkren.utils.Log;
import cz.tomkren.kutil.items.Int2D;
import cz.tomkren.kutil.shapes.FunctionShape;
import cz.tomkren.kutil.shapes.RectangleShape;
import cz.tomkren.kutil.shapes.AnimationShape;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ShapeFactory {


    public KShape newKShape(String shapeCmd, Kutil kutil) {

        if (shapeCmd == null) {
            return mkDefaultShape();
        }

        String[] parts = shapeCmd.split( "\\s+" );
        String shapeName = parts[0];

        if ("rectangle".equals(shapeName)) {

            if (parts.length == 4) {

                int x     = Integer.parseInt(parts[1]);
                int y     = Integer.parseInt(parts[2]);
                Color col = Color.PINK;

                try {
                    col = Color.decode(parts[3]);}
                catch(NumberFormatException e){
                    Log.it("Incorrect color code : " + e.getMessage());
                }

                return new RectangleShape( new Int2D( x,y ), col );
            }

            if( parts.length != 3 ) return mkDefaultShape();
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            return new RectangleShape( new Int2D( x,y ), Color.black );
        }

        if ("f".equals(shapeName)) {

            int numInputs  = 1;
            int numOutputs = 1;
            String name    = null;
            List<AB<String,Integer>> targets = null;
            List<Double> weights = null;
            boolean atLeastOneWeight = false;
            Color bodyColor = null;

            if (parts.length > 1) {numInputs  = Integer.parseInt(parts[1]);}
            if (parts.length > 2) {numOutputs = Integer.parseInt(parts[2]);}
            if (parts.length > 3) {
                String[] subParts = parts[3].split(":");
                if (subParts.length == 1) {
                    name = parts[3];
                } else {
                    name = subParts[0];
                    bodyColor = Color.decode(subParts[1]);//("green".equals(subParts[1]) ? Color.green : Color.red  );
                }

            }

            if (parts.length > 4) {
                targets = new ArrayList<>();
                weights = new ArrayList<>();

                for (int i = 4; i<parts.length; i++) {

                    if ("null".equals(parts[i])) {
                        targets.add(null);
                        weights.add(null);
                    } else {
                        String[] subParts = parts[i].split(":");
                        if (subParts.length >= 2) {
                            targets.add(new AB<>(subParts[0], Integer.parseInt(subParts[1])));
                        }

                        if (subParts.length >= 3) {
                            weights.add(Double.parseDouble(subParts[2]));
                            atLeastOneWeight = true;
                        } else {
                            weights.add(null);
                        }
                    }
                }
            }

            if (weights != null && !atLeastOneWeight) {
                weights = null;
            }


            return new FunctionShape(numInputs, numOutputs, name, targets, weights, bodyColor, kutil);
        }

        if ("vincent".equals(shapeName)) {
            return new AnimationShape("/cz/tomkren/kutil/kobjects/vincent/vincent.json");
        }

        /* TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! */

        return mkDefaultShape();
    }

    public static KShape mkDefaultShape() {return new RectangleShape(new Int2D(32,32), Color.black );}

}
