package cz.tomkren.kutil.core;

import cz.tomkren.helpers.*;
import cz.tomkren.kutil.items.Int2D;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Created by tom on 30.7.2015. */

// TODO vyčistit od přesunutého kódu (bezírci atd okolo)
// TODO svg resource se dělá pro každou instanci objektu zvlášť, SUPERDEMENTNí !!!!

public class Animation {

    public static final String POS_DELIM = ",";
    public static final String CMD_DELIM = "\\s+";



    public static final Color boneColor = Color.black;
    public static final Color ballColor = Color.black;
    public static final Color nodeColor = Color.black;
    public static final int nodeRadius = 2;

    private int numFrames;
    private List<List<Int2D>> nodes;
    //private List<List<AA<Int2D>>> bones;
    private List<List<SVGBone>> svgBones;
    //private List<List<AB<Int2D,Integer>>> balls;

    // TODO dát do configů !!!
    private Color lineColor = Color.black; // Color.gray;
    private Color fillColor = Color.white; // Color.blue;

    private final Int2D posDelta;

    public Animation(JSONObject config, String animationName) {

        posDelta = Int2D.mk(config.getString("posDelta"), POS_DELIM);

        Object jsonAnimationObj = config.getJSONObject("animations").get(animationName);
        JSONArray jsonAnimation = null;

        if (jsonAnimationObj instanceof JSONArray) {
            jsonAnimation = (JSONArray) jsonAnimationObj;
        } else if (jsonAnimationObj instanceof String) {
            String[] parts = ((String) jsonAnimationObj).split(CMD_DELIM);
            if ("flip".equals(parts[0])) {
                String animNameToFlip = parts[1];
                JSONArray animToFlip = config.getJSONObject("animations").getJSONArray(animNameToFlip);
                jsonAnimation = flipJsonAnimation(animToFlip);
            }
        }

        if (jsonAnimation == null) {
            throw new Error("Wrong format of animation config.");
        }

        initSVGResources(config.getJSONObject("resources"),config.getString("defaultBone"));

        init(
                config.getJSONArray("nodes"),
                config.getJSONArray("skeleton"),
                config.getJSONArray("balls"),
                jsonAnimation
        );
    }

    private Map<String,SVGResource> svgResources;
    private SVGResource defaultSvgResource;


    // TODO dělá se pro každou instanci objektu zvlášť, SUPERDEMENTNí !!!!
    private void initSVGResources(JSONObject jsonResources, String defaultResourceName) {
        svgResources = new HashMap<>();
        for (String resName : jsonResources.keySet()) {
            String resourcePath = jsonResources.getString(resName);
            svgResources.put(resName, new SVGResource(resourcePath));
        }
        defaultSvgResource = svgResources.get(defaultResourceName);
    }

    private SVGResource svgResourceByName(String resourceName) {
        return resourceName == null ? defaultSvgResource : svgResources.get(resourceName);
    }

    private void init(JSONArray jsonNodes, JSONArray jsonSkeleton, JSONArray jsonBalls, JSONArray jsonAnimation) {

        List<String> nodeNames = F.stringsFromJsonArray(jsonNodes);

        List<AAA<String>> edgeNames = new ArrayList<>(jsonSkeleton.length());
        for (int i = 0; i < jsonSkeleton.length(); i++) {
            JSONArray edge = jsonSkeleton.getJSONArray(i);
            edgeNames.add(new AAA<>(edge.getString(0), edge.getString(1), edge.length()<3 ? null : edge.getString(2)));
        }

        List<AB<String,Integer>> ballNames = new ArrayList<>(jsonBalls.length());
        for (int i = 0; i < jsonBalls.length(); i++) {
            JSONObject ball = jsonBalls.getJSONObject(i);
            ballNames.add(new AB<>(ball.getString("center"), ball.getInt("radius")));
        }

        numFrames = jsonAnimation.length();

        nodes = new ArrayList<>(numFrames);
        //bones = new ArrayList<>(numFrames);
        svgBones = new ArrayList<>(numFrames);
        //balls = new ArrayList<>(numFrames);

        for (int i = 0; i < numFrames; i++) {

            JSONObject frame = jsonAnimation.getJSONObject(i);
            Int2D frameCenterPos = Int2D.mk(frame.getString("center"), POS_DELIM);


            List<Int2D> frameNodes = F.map(nodeNames, nodeName -> transformNodePos(frame, nodeName, frameCenterPos));

            frameNodes = F.filter(frameNodes, x -> x != null);

            List<AB<AA<Int2D>,String>> frameBones = F.map(edgeNames, edgeName -> new AB<>(new AA<>(
                transformNodePos(frame, edgeName._1(), frameCenterPos),
                transformNodePos(frame, edgeName._2(), frameCenterPos)
            ), edgeName._3() ));

            frameBones = F.filter(frameBones, x -> x._1()._1() != null && x._1()._2() != null );

            List<SVGBone> frameSvgBones = F.map(frameBones, boneData ->
                new SVGBone(svgResourceByName(boneData._2()),boneData._1())
            );

            List<AB<Int2D,Integer>> frameBalls = F.map(ballNames, ballName -> new AB<>(
                transformNodePos(frame, ballName._1(), frameCenterPos),
                ballName._2() / 2
            ));

            nodes.add(frameNodes);
            //bones.add(frameBones);
            svgBones.add(frameSvgBones);
            //balls.add(frameBalls);
        }
    }




    public static JSONArray flipJsonAnimation(JSONArray jsonAnimation) {
        JSONArray ret = new JSONArray();
        for (int i = 0; i < jsonAnimation.length(); i++) {
            ret.put(flipJsonFrame(jsonAnimation.getJSONObject(i)));
        }
        return ret;
    }

    public static JSONObject flipJsonFrame(JSONObject jsonFrame) {
        JSONObject ret = new JSONObject();

        Int2D centerPos = Int2D.mk(jsonFrame.getString("center"), POS_DELIM);
        int xCenter = centerPos.getX();

        for (String key : jsonFrame.keySet()) {

            Object val = jsonFrame.get(key);

            if ("name".equals(key) || "center".equals(key)) {
                ret.put(key, val);
            } else if (val instanceof String) {
                Int2D nodePos = Int2D.mk((String)val,POS_DELIM);

                int xNew = 2*xCenter - nodePos.getX();

                ret.put(key, xNew +POS_DELIM+ nodePos.getY() );
            }

        }

        return ret;
    }

    public int getNumFrames() {return numFrames;}

    public void draw(Graphics2D g, int frame, Int2D drawPos) {

        for (SVGBone svgBone : svgBones.get(frame)) {
            svgBone.draw(g, drawPos, lineColor, fillColor);
        }

        // TODO volitelně vykreslovat
        /*for (Int2D nodePos : nodes.get(frame)) {
            drawNode(g, nodePos.plus(drawPos));
        }
        for (AA<Int2D> bone : bones.get(frame)) {
            drawBone(g, bone._1().plus(drawPos), bone._2().plus(drawPos));
        }*/


        // BALLS asi už nezavádět....
        /*for (AB<Int2D,Integer> ball : balls.get(frame)) {
            drawBall(g, ball._1().plus(drawPos), ball._2());
        }*/

        /*drawSpine(g, drawPos);*/ // todo dát uplně pryč is metodou - ale z piety odloženo :)
    }

    private Int2D transformNodePos(JSONObject frame, String nodeName, Int2D frameCenterPos) {

        if (!frame.has(nodeName)) {
            return null;
        }

        Int2D nodePos = Int2D.mk(frame.getString(nodeName), POS_DELIM);
        nodePos = nodePos.minus(frameCenterPos);
        int x = nodePos.getX() / 2;
        int y = nodePos.getY() / 2;
        return new Int2D(x, y).plus(posDelta);
    }


    private void drawNode(Graphics2D g, Int2D pos) {

        g.setColor(nodeColor);

        g.drawOval(pos.getX()-nodeRadius,
                pos.getY()-nodeRadius,
                nodeRadius*2,nodeRadius*2);
    }

    private void drawBone(Graphics2D g, Int2D from, Int2D to) {
        g.setColor(boneColor);
        g.drawLine(from.getX(), from.getY(), to.getX(), to.getY());
    }

    private void drawBall(Graphics2D g, Int2D pos, int radius) {
        g.setColor(ballColor);

        g.drawOval(pos.getX() - radius,
                pos.getY() - radius,
                radius * 2, radius * 2);
    }



}
