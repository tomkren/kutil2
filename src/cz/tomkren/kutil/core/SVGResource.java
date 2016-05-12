package cz.tomkren.kutil.core;

import cz.tomkren.helpers.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;

/** Created by tom on 31. 7. 2015. */

public class SVGResource {

    private String name;
    private AA<Double> from;
    private AA<Double> to;
    private List<List<AA<Double>>> paths;

    public String getName() {return name;}
    public AA<Double> getFrom() {return from;}
    public AA<Double> getTo() {return to;}
    public List<List<AA<Double>>> getPaths() {return paths;}

    public SVGResource(String resourcePath) {
        this(new ResourceLoader().loadJSON(resourcePath));
    }

    public SVGResource(JSONObject svgConfig) {
        name  = svgConfig.getString("name");
        from  = parseJsonDoublePair(svgConfig.get("from"));
        to    = parseJsonDoublePair(svgConfig.get("to"));
        paths = simpleParseSVGPaths(svgConfig.getJSONArray("paths"));
    }



    /*public static void draw(Graphics2D g, GeneralPath drawPath, Color lineColor, Color fillColor) {
        g.setColor(fillColor);
        g.fill(drawPath);
        g.setColor(lineColor);
        g.draw(drawPath);
    }*/

    public static List<List<AA<Double>>> simpleParseSVGPaths(JSONArray svgPaths) {
        int n = svgPaths.length();
        List<List<AA<Double>>> ret = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {

            Object path = svgPaths.get(i);
            List<AA<Double>> parsedPath;

            if (path instanceof String) {
                String strPath = (String) path;
                Log.it("PATH: "+ stringPathToJsonPath(strPath) );
                parsedPath = simpleParseSVGPath(strPath);
            } else if (path instanceof JSONArray) {
                parsedPath = simpleParseSVGPath_json((JSONArray)path);
            } else {
                throw new Error("Svg path is not in a correct form (String or JSONArray).");
            }

            ret.add(parsedPath);
        }
        return ret;
    }

    public static List<AA<Double>> simpleParseSVGPath_json(JSONArray svgPath) {
        List<AA<Double>> ret = new ArrayList<>();

        for (int i = 0; i < svgPath.length(); i++) {
            Object val = svgPath.get(i);

            if (val instanceof String) {
                // todo COMMAND, zatim ignorujeme
            } else if (val instanceof JSONArray) {
                JSONArray pair = (JSONArray) val;
                if (pair.length() != 2) {
                    throw new Error("pair in svg must be PAIR.");
                }
                ret.add(new AA<>(pair.getDouble(0),pair.getDouble(1)));
            } else {
                throw new Error("Incorrect format of svg pair.");
            }
        }

        return ret;
    }

    public static JSONArray stringPathToJsonPath(String svgPath) {
        String[] parts = svgPath.split(" ");

        JSONArray ret = new JSONArray();

        for (String part : parts) {
            if (part.length() == 1) {
                ret.put(part);
            } else {
                AA<Double> pair = parseDoublePair(part);
                JSONArray jsonPair = new JSONArray();
                jsonPair.put(pair._1());
                jsonPair.put(pair._2());
                ret.put(jsonPair);
            }
        }
        return ret;
    }

    public static List<AA<Double>> simpleParseSVGPath(String svgPath) {
        String[] parts = svgPath.split(" ");
        List<AA<Double>> ret = new ArrayList<>();
        for (String part : parts) {
            if (part.length() != 1) { // s dýlkou 1 je to command // TODO lépe fakt reagovat na to co tam je za komand
                                                                  // TODO (!!!) např problem když obsahuje lineto (cmd l) pak se to uplně rozhodí (!!!!!)
                ret.add(parseDoublePair(part));
            }
        }
        return ret;
    }


    private static AA<Double> parseJsonDoublePair(Object dPair) {

        if (dPair instanceof String) {
            return parseDoublePair((String)dPair);
        } else if (dPair instanceof JSONArray) {
            JSONArray dPairArr = (JSONArray) dPair;
            return new AA<>(dPairArr.getDouble(0), dPairArr.getDouble(1));
        }

        throw new Error("Unsupported json double pair format...");
    }

    private static AA<Double> parseDoublePair(String dPair) {
        String[] parts = dPair.split(",");
        return new AA<>(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]));
    }


    public static void main(String[] args) {
        SVGResource svg = new SVGResource("/cz/tomkren/kutil/x/svgs/bone.json");

        F.each(svg.getPaths(), Log::list);

    }

}
