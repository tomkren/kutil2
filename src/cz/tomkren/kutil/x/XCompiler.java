package cz.tomkren.kutil.x;

import cz.tomkren.helpers.F;
import cz.tomkren.helpers.Log;
import cz.tomkren.helpers.ResourceLoader;
import cz.tomkren.kutil.x.templates.TextMedia;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Created by tom on 13.8.2015. */

public class XCompiler {

    public static void main(String[] args) {

        try {
            String cmd = "cmd /c start xcompile.bat";
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            throw new Error(e);
        }

        XCompiler xCompiler = new XCompiler();

        if (args.length == 0) {
            xCompiler.compileAllToFiles();
        } else {
            xCompiler.compileToFiles(args);
        }

        xCompiler.compileAllTexFilesToPdfs();




    }

    public static final String X_HOME = "/cz/tomkren/kutil/x/";
    public static final String X_PATH = X_HOME + "x.json";
    public static final Pattern nameAndExt = Pattern.compile("(.+?)(\\.[^.]*$|$)");

    private JSONObject x;
    private List<String> texNames;

    public XCompiler() {
        x = new ResourceLoader().loadJSON(X_PATH);
        texNames = new ArrayList<>();
    }

    public JSONObject x() {return x;}

    public void compileAllToFiles() {
        JSONArray compileConfigs = x.getJSONObject("meta").getJSONArray("outputs");
        for (int i = 0; i < compileConfigs.length(); i++) {
            JSONObject compileConfig = compileConfigs.getJSONObject(i);
            compileToFile(compileConfig);
        }
    }

    public void compileToFiles(String[] args) {
        JSONArray compileConfigs = x.getJSONObject("meta").getJSONArray("outputs");
        for (int i = 0; i < compileConfigs.length(); i++) {
            JSONObject compileConfig = compileConfigs.getJSONObject(i);
            for (String arg : args) {
                String filename = getFilename(compileConfig);
                if (arg.equals(filename)) {
                    compileToFile(compileConfig);
                    break;
                }
            }
        }
    }

    public void compileToFile(JSONObject compileConfig) {
        String filename = getFilename(compileConfig);
        String path = "x/"+ filename;
        String compileOutput = compile(compileConfig);

        Matcher m = nameAndExt.matcher(filename);
        if (m.find()) {
            String nameWithoutExt = m.group(1);
            String extension      = m.group(2);
            if (".tex".equals(extension)) {
                texNames.add(nameWithoutExt);
            }
        }

        Log.it("\n\n" + F.underline("x compiled to " + path + ":"));
        Log.itln(compileOutput);
        F.writeFile(path, compileOutput);
    }

    public String compile(JSONObject compileConfig) {
        if (compileConfig.has("medium")) {
            String mediumName = compileConfig.getString("medium");
            JSONObject args   = compileConfig.has("args") ? compileConfig.getJSONObject("args") : new JSONObject();
            return TextMedia.getMediumCompiler(mediumName).compile(args, x);
        }
        return "ERROR : unimplemented compileConfig: \n"+ compileConfig.toString(2);
    }

    private String getFilename(JSONObject compileConfig) {
        return compileConfig.has("file") ? compileConfig.getString("file") : defaultFilename(compileConfig);
    }

    private String defaultFilename(JSONObject compileConfig) {
        if (compileConfig.has("args") && compileConfig.has("medium")) {
            Object args = compileConfig.get("args");
            if (args instanceof JSONObject && ((JSONObject) args).has("template")) {
                return compileConfig.getString("medium") + "." + ((JSONObject) args).getString("template");
            }
        }
        return "ERROR : unable tu generate default filename from compileConfig: \n"+ compileConfig.toString(2);
    }

    public void compileAllTexFilesToPdfs() {
        Log.it();
        for (String texName : texNames) {
            makePdf("x", texName);
        }
    }

    private void makePdf(String cdDir, String nameWithoutExtension) {
        try {
            String cmd = "cmd /c start mkpdf.bat "+cdDir+" "+nameWithoutExtension;
            Log.it("RUNNING "+cmd);
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            throw new Error(e);
        }
    }

}
