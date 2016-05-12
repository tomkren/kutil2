package cz.tomkren.kutil.x.templates;

import cz.tomkren.helpers.F;
import cz.tomkren.helpers.Log;
import cz.tomkren.helpers.ResourceLoader;
import cz.tomkren.kutil.x.XCompiler;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Created by user on 21. 8. 2015. */

public class TextMedia {

    public static MediumCompiler getMediumCompiler(String mediumName) {
        switch (mediumName) {
            case "book"   : return TextMedia::bookCompile;
            case "scenes" : return TextMedia::scenesCompile;
            case "game"   : return gameCompiler;
            default: throw new Error("Unknown medium name: "+mediumName);
        }
    }

    private static final GameCompiler gameCompiler = new GameCompiler();

    public static String bookCompile(JSONObject args, JSONObject x) {
        StringBuilder sb = new StringBuilder();
        ResourceLoader resourceLoader = new ResourceLoader();

        CompileTemplate<String> template = Templates.getStringTemplate(args);

        boolean isCommented = args.has("commented") && args.getBoolean("commented");

        int scenesPerAct = x.getInt("scenesPerAct");

        JSONArray acts = x.getJSONArray("acts");
        JSONArray scenes = x.getJSONArray("scenes");

        for (int i = 0; i < scenes.length(); i++) {

            int actIndex = i / scenesPerAct;

            if (i % scenesPerAct == 0) {
                String actTitle = acts.getJSONObject(actIndex).getString("title");
                sb.append(template.actTitle(Integer.toString(1+actIndex),actTitle)).append("\n\n");
            }

            JSONObject scene = scenes.getJSONObject(i);
            String title = scene.getString("title");

            String sceneMark = (1+actIndex) + "." + (1 + (i%scenesPerAct));

            sb.append(template.sceneTitle(sceneMark,title)).append("\n\n");

            if (scene.has("pages")) {
                String pagesStr = writePages(scene.get("pages"), template, resourceLoader);
                sb.append(pagesStr);
            }

            if (isCommented && scene.has("notes")) {
                JSONArray notes = scene.getJSONArray("notes");
                if (notes.length() > 0) {

                    String notesStr = writeList(notes, template);
                    notesStr = template.italics(notesStr);

                    sb.append(notesStr).append("\n\n");
                }
            }

            if (isCommented && scene.has("outline")) {
                String outlineStr = writePages(scene.get("outline"), template, resourceLoader);

                outlineStr = template.italics(outlineStr);
                outlineStr = template.newLine(outlineStr);

                sb.append(outlineStr);
            }



            if (i % scenesPerAct == scenesPerAct-1) {
                sb.append("\n");
            }
        }

        String templateStr = resourceLoader.loadString(XCompiler.X_HOME + template.getPath());
        templateStr = templateStr.replace("THE-TITLE",x.getString("title"))
                                 .replace("BOND-HERE", sb.toString());

        if (args.has("script") && args.getBoolean("script")) {

            GameCompiler gameCompiler = new GameCompiler();

            String gameStateJson = gameCompiler.compile(new JSONObject("{\"template\": \"game\"}"), x);

            String scriptStr = "<script> var gameState = "+gameStateJson+"; </script>";

            templateStr = templateStr.replace("<script></script>", scriptStr);

        }

        return templateStr;
    }

    public static String scenesCompile(JSONObject args, JSONObject x) {
        StringBuilder sb = new StringBuilder();
        JSONArray scenes = x.getJSONArray("scenes");
        int basicActLength = 16;
        for (int i = 0; i < scenes.length(); i++) {
            JSONObject scene = scenes.getJSONObject(i);
            sb.append("(").append(i + 1).append(") ").append(i < 10 ? " " : "").append(scene.getString("title")).append('\n');
            if (i % basicActLength == basicActLength-1) {
                sb.append("\n");
            }
        }
        sb.delete(sb.length()-2, sb.length());
        return sb.toString();
    }


    public static String writeList(JSONArray notes, CompileTemplate<String> template) {
        if (notes.length() == 0) {return "";}

        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for (int i = 0; i < notes.length(); i++) {

            Object note = notes.get(i);
            String noteStr = null;

            if (note instanceof String) {
                noteStr = (String) note;
                noteStr = template.listItem(noteStr);
            } else if (note instanceof JSONArray) {
                noteStr = writeList((JSONArray) note, template);
            }

            sb.append("  ").append(noteStr).append('\n');
        }

        return template.list(sb.toString());
    }

    public static String writePages(Object pages, CompileTemplate<String> template, ResourceLoader resourceLoader) {

        StringBuilder sb = new StringBuilder();

        if (pages instanceof String)  {
            String pagesStr = (String) pages;
            sb.append(pagesStr).append("\n\n");
        } else if (pages instanceof JSONObject) {
            JSONObject pagesObj = (JSONObject) pages;
            if (pagesObj.has("resource")) {
                String text = resourceLoader.loadString(XCompiler.X_HOME + pagesObj.getString("resource"));

                text = text.trim();


                //Log.list(F.map(ps, p -> ">" + p + "<\n\n"));

                StringBuilder acc = new StringBuilder();
                for (String paragraph : splitByEmptyLines(text)) {
                    acc.append( template.paragraph(paragraph));
                }
                text = acc.toString();

                text = text.replace("\\\\", template.newLine(""));

                sb.append(text).append("\n");
            }

        } else if (pages instanceof JSONArray) {

            JSONArray pagesArray = (JSONArray) pages;
            if (pagesArray.length() > 0) {
                for (int j = 0; j < pagesArray.length(); j++) {
                    JSONArray page = pagesArray.getJSONArray(j);
                    if (page.length() > 0) {
                        for (int k = 0; k < page.length(); k++) {
                            String line = page.getString(k);
                            sb.append(template.newLine(line)).append("\n");
                        }
                    }
                }
            }
        }

        return sb.toString();
    }




    public static List<String> splitByEmptyLines(String str) {

        //Log.it(str);
        List<String> ret = new ArrayList<>();

        String[] lines = str.trim().split("\\r?\\n");

        StringBuilder acc = new StringBuilder();

        for (String line : lines) {

            line = line.trim();

            if (line.isEmpty()) {
                ret.add(acc.toString().trim());
                acc = new StringBuilder();
            } else {
                acc.append(line).append("\n");
            }

            //Log.it(line +"!!!");
        }

        ret.add(acc.toString().trim());

        return ret;

    }

}
