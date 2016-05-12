package cz.tomkren.kutil.shapes;

import cz.tomkren.helpers.*;
import cz.tomkren.kutil.core.Animation;
import cz.tomkren.kutil.items.Int2D;
import org.json.JSONArray;
import org.json.JSONObject;

//import java.awt.*;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;

/** Created by tom on 28.7.2015. */

public class AnimationShape extends ConvexPolygonShape {

    public static final Color vincents_purple = new Color(135, 2, 119);
    public static final Color vincents_light_purple = new Color(215, 170, 209);
    public static final Color vincents_very_light_purple = new Color(249, 242, 248);

    private static final Int2D posPoint = new Int2D(0, 0);
    private static final Int2D teziste = new Int2D(0, 0);



    private Map<String,Animation> animations;

    private Animation actualAnimation;
    private int       actualFrame;

    private boolean showPhysicalBody;


    public AnimationShape(String resourcePath) {
        super(null, posPoint, teziste, vincents_very_light_purple);


        JSONObject jsonConfig = new ResourceLoader().loadJSON(resourcePath);

        Int2D[] vs = getPhysicalShape(jsonConfig.getJSONArray("physicalShape"));
        //new Int2D[]{new Int2D(0, 0), new Int2D(50, 0), new Int2D(50, 190), new Int2D(0, 190)};

        showPhysicalBody = jsonConfig.has("showPhysicalBody") && jsonConfig.getBoolean("showPhysicalBody");


        animations = new HashMap<>();

        for (String key : jsonConfig.getJSONObject("animations").keySet()) {
            animations.put(key, new Animation(jsonConfig, key));
        }

        actualAnimation = animations.get(jsonConfig.getString("defaultAnimation"));
        actualFrame = 0;


        //initT(teziste); // todo zjistit co přesně dělá a nastavit na center nebo tak něco
        initVs(vs);

    }

    private Int2D[] getPhysicalShape(JSONArray jsonPoses) {
        int n = jsonPoses.length();
        Int2D[] ret = new Int2D[n];
        for (int i = 0; i < n; i++) {
            ret[i] = Int2D.mk(jsonPoses.getString(i), ",");
        }
        return ret;
    }


    public void setAnimationFrame(String animationName, int frame) {
        Animation newAnimation = animations.get(animationName);
        if (newAnimation != null) {
            actualAnimation = newAnimation;
            actualFrame = frame % actualAnimation.getNumFrames(); // better safe than sorry
        } else {
            Log.err("Trying to set non-existing animation '"+animationName+"'.");
        }
    }



    @Override
    public void draw(Graphics2D g, boolean isSel, String info, Int2D pos, Int2D center, double rot, boolean isRotable, double zoom, Int2D zoomCenter) {
        if (showPhysicalBody || isSel) {
            super.draw(g, isSel, info, pos, center, rot, isRotable, zoom, zoomCenter);
        }

        Int2D drawPos = pos.plus(center);
        actualAnimation.draw(g, actualFrame, drawPos);

    }











}
