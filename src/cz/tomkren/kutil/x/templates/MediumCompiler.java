package cz.tomkren.kutil.x.templates;

import org.json.JSONObject;

/** Created by tom on 22. 8. 2015.m*/

public interface MediumCompiler {
    String compile(JSONObject args, JSONObject x);
}
