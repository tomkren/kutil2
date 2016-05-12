package cz.tomkren.kutil.x.templates;

import org.json.JSONObject;

/** Created by user on 21. 8. 2015.*/

public class Templates {

    public static CompileTemplate getTemplate(String templateName) {
        switch (templateName) {
            case "txt" : return txtTemplate;
            case "tex" : return texTemplate;
            case "html": return htmlTemplate;
            case "game": return gameTemplate;
            default: throw new Error("Unknown template name: "+templateName);
        }
    }

    public static final CompileTemplate<JSONObject> gameTemplate = new CompileTemplate<>(
            "templates/gameTemplate.json",
            (i,s) -> s,
            (i,s) -> s,
            s -> s,
            s -> s,
            s -> s,
            s -> s,
            s -> s
    );

    public static final CompileTemplate<String> texTemplate = new CompileTemplate<>(
            "templates/texTemplate.tex",
            (i,s) -> "\\chapter{"+s+"}",
            (i,s) -> "\\section{"+s+"}",
            s -> "\\begin{itemize}"+s+"\\end{itemize}",
            s -> "\\item "+s,
            s -> "\\begin{itshape}"+s+"\\end{itshape}", //"\\textit{"+s.trim()+"}",
            s -> s+"\\\\",
            s -> s+"\\\\\\\\"
    );

    public static final CompileTemplate<String> htmlTemplate = new CompileTemplate<>(
            "templates/htmlTemplate.html",
            (i,s) -> "<div class=\"act-label\">Akt "+i+"</div><h2>"+s+"</h2>",
            (i,s) -> "<a name=\""+i+"\"></a><h3>"+i+"&nbsp; "+s+"&nbsp;<a href=\"#up\" class=\"up\">↑</a></h3>",
            s -> "<ul>"+s+"</ul>",
            s -> "<li>"+s+"</li>",
            s -> "<i>"+s+"</i>",
            s -> s+"<br/>",
            s -> "<p>"+s+"</p>"
    );

    public static final CompileTemplate<String> txtTemplate = new CompileTemplate<>(
            "templates/txtTemplate.txt",
            (i,s) -> " ### Akt "+i+": "+s+" ### ",
            (i,s) -> "\n # "+s+" #",
            s -> s,
            s -> "- "+s,
            s -> "<[ "+s+"]>",
            s -> s,
            s -> s
    );

    public static CompileTemplate getTemplate(JSONObject mediaCompilerArgs) {
        String templateName = mediaCompilerArgs.getString("template");
        return getTemplate(templateName);
    }

    @SuppressWarnings("unchecked")
    public static CompileTemplate<String> getStringTemplate(JSONObject mediaCompilerArgs) {
        return getTemplate(mediaCompilerArgs);
    }

    @SuppressWarnings("unchecked")
    public static CompileTemplate<JSONObject> getJSONObjectTemplate(JSONObject mediaCompilerArgs) {
        return getTemplate(mediaCompilerArgs);
    }
}
