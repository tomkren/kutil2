package cz.tomkren.kutil.core;

import org.json.JSONObject;

/** Created by tom on 30.8.2015. */

public class JSONObjectWrapper implements KAttVal {

    private JSONObject obj;

    public JSONObjectWrapper(JSONObject obj) {
        this.obj = obj;
    }

    public JSONObject getJSONObject() {
        return obj;
    }

    @Override
    public String toString() {
        return toString(obj);
    }

    public static String toString(JSONObject jsonObj) {
        return jsonObj.toString().replace("\"", "'");
    }

    public static JSONObject parse(String str) {
        return new JSONObject(str.replace("'", "\""));
    }
}
