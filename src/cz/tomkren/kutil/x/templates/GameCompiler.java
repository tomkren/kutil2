package cz.tomkren.kutil.x.templates;

import cz.tomkren.utils.ResourceLoader;
import cz.tomkren.kutil.x.XCompiler;
import org.json.JSONArray;
import org.json.JSONObject;

/** Created by tom on 22. 8. 2015. */

public class GameCompiler implements MediumCompiler {

    @Override
    public String compile(JSONObject args, JSONObject x) {
        ResourceLoader resourceLoader = new ResourceLoader();
        CompileTemplate<JSONObject> template = Templates.getJSONObjectTemplate(args);


        JSONArray scenesJson = new JSONArray();
        if (x.has("scenes")) {
            JSONArray xScenes = x.getJSONArray("scenes");

            int xPos = 22;
            int yPos = 22;
            int dx   = 78;

            for (int i = 0; i < xScenes.length(); i++) {
                JSONObject scene = xScenes.getJSONObject(i);

                if (scene.has("level")) {
                    String levelPath = scene.getJSONObject("level").getString("resource");
                    JSONObject level = resourceLoader.loadJSON(XCompiler.X_HOME + levelPath);

                    level.put("pos", xPos+" "+yPos);
                    xPos += dx;

                    scenesJson.put(level);
                }
            }
        }

        JSONArray svgsJson = new JSONArray();

        if (x.has("svgs")) {
            JSONArray xSvgs = x.getJSONArray("svgs");
            for (int i = 0; i < xSvgs.length(); i++) {
                JSONObject svgDescriptor = xSvgs.getJSONObject(i);

                if (svgDescriptor.has("resource")) {
                    String svgPath = svgDescriptor.getString("resource");
                    JSONObject svgObj = resourceLoader.loadJSON(XCompiler.X_HOME + svgPath);
                    svgsJson.put(svgObj);
                }
            }
        }

        String templateStr = resourceLoader.loadString(XCompiler.X_HOME + template.getPath());
        templateStr = templateStr.replace("THE-TITLE", x.getString("title"));
        templateStr = templateStr.replace("\"THE-SCENES\"", scenesJson.toString(2));
        templateStr = templateStr.replace("\"THE-SVGS\"", svgsJson.toString());
        return templateStr;
    }
}
