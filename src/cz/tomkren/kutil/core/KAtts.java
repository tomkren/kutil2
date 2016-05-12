package cz.tomkren.kutil.core;

import cz.tomkren.kutil.items.Int2D;
import cz.tomkren.kutil.kobjects.Text;
import cz.tomkren.kutil.kobjects.Time;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * TODO
 */
public class KAtts {

    private Map<String,List<KAttVal>> atts; // seznam vnitřních objektů indexovaný jménem

    public KAtts() {
        atts = new HashMap<>();
    }


    public static KAtts fromJson(JSONObject json, Kutil kutil) {

        List<Time> times = new ArrayList<>();

        KAtts ret = fromJson(json, times, kutil, true);

        ret.parentInfo();
        ret.init();

        for (Time t : times) {
            ret.add("times", t);
        }

        return ret;
    }

    private static KAtts fromJson(JSONObject json, List<Time> times, Kutil kutil, boolean isRoot) {

        KAtts ret = new KAtts();

        for (String key : json.keySet()) {
            Object val = json.get(key);

            if (isRoot) {
                if (key.equals("svgs")) {
                    continue;
                }
            }

            if (key.equals("args")) {

                JSONObject valObj;

                if (val instanceof JSONObject) {
                    valObj = (JSONObject) val;
                } else if (val instanceof String) {
                    valObj = JSONObjectWrapper.parse((String) val);
                } else {
                    throw new Error("args must be JSONObject or String");
                }

                ret.add(key, new JSONObjectWrapper(valObj));

            } else if (val instanceof JSONObject) {
                JSONObject valObj = (JSONObject) val;
                addNewKObject(valObj, key, ret, times, kutil);

            } else if (val instanceof JSONArray) {
                JSONArray valArr = (JSONArray) val;

                for (int i = 0; i < valArr.length(); i++) {
                    JSONObject elObj = valArr.getJSONObject(i);
                    addNewKObject(elObj, key, ret, times, kutil);
                }

            } else if (val instanceof String) {
                String valStr = (String) val;
                ret.add(key, new Text(valStr));
            }
        }

        return ret;
    }

    private static void addNewKObject(JSONObject description, String key, KAtts parentKAtts, List<Time> times, Kutil kutil) {
        KAtts kAtts = fromJson(description, times, kutil, false);
        KObject newKObject = KObjectFactory.newKObject(kAtts, kutil);

        String id = kAtts.getString("id");
        if (id != null) {
            kutil.getIdDB().put(id, newKObject);
        }

        if (newKObject instanceof Time) {
            times.add((Time) newKObject);
        }

        parentKAtts.add(key, newKObject);
    }




    public KAtts add(String tag , KAttVal o) {
        List<KAttVal> list = atts.get(tag);
        if (list == null) {
            list = new ArrayList<>();
            atts.put(tag, list);
        }
        list.add(o);
        return this;
    }


    public void parentInfo(){
        for (List<KAttVal> list : atts.values()) {
            list.stream().filter(o -> o instanceof KObject).forEach(o -> ((KObject) o).parentInfo(null));
        }
    }

    public void init(){
        for (List<KAttVal> list : atts.values()) {
            list.stream().filter(o -> o instanceof KObject).forEach(o -> ((KObject) o).init());
        }
    }


    // --- GETTERS ---

    public KObject get(String tag) {
        List<KAttVal> list = atts.get(tag);
        if (list == null) {return null;}
        if(list.isEmpty()){return null;}
        KAttVal ret = list.get(0);
        return ret instanceof KObject ? (KObject)ret : null;
    }

    public List<KObject> getList(String tag) {
        List<KAttVal> list = atts.get(tag);
        if (list == null) {return new ArrayList<>(0);}
        return list.stream().filter(o -> o instanceof KObject).map(o -> (KObject) o).collect(Collectors.toList());
    }

    public <T> T get_parseFirst(String key, Function<String,T> parse) {
        List<KAttVal> list = atts.get(key);
        if (list == null || list.isEmpty()) {return null;}
        KAttVal o = list.get(0);
        return (o instanceof Text ? parse.apply(o.toString()) : null);
    }

    public JSONObject get_JSONObject(String key) {
        List<KAttVal> list = atts.get(key);

        if (list == null) {
            return null;
        }

        if (list.size() != 1) {
            throw new Error("expected list size 1 !");
        }

        KAttVal val = list.get(0);

        if (val instanceof JSONObjectWrapper) {
            return ((JSONObjectWrapper) val).getJSONObject();
        } else if (val instanceof Text) {
            return JSONObjectWrapper.parse(val.toString());
        }


        throw new Error("JSONObjectWrapper or Text expected ! but it was... "+key+" : "+val);
    }

    public <T> T get_tryOrDefault(String key, Function<String,T> getter, T defaultVal) {
        T ret = getter.apply(key);
        return (ret == null ? defaultVal : ret);
    }

    public String getString(String key) {return get_parseFirst(key, str -> str);}
    public String getString(String key, String defaultVal) {return get_tryOrDefault(key, this::getString, defaultVal);}

    public Double getDouble(String key) {return get_parseFirst(key, Double::parseDouble);}
    public Double getDouble(String key, Double defaultVal) {return get_tryOrDefault(key, this::getDouble, defaultVal);}

    public Integer getInteger(String key) {return get_parseFirst(key, Integer::parseInt);}
    public Integer getInteger(String key, Integer defaultVal) {return get_tryOrDefault(key, this::getInteger, defaultVal);}

    public Boolean getBoolean(String key) {return get_parseFirst(key, Boolean::parseBoolean);}
    public Boolean getBoolean(String key, Boolean defaultVal) {return get_tryOrDefault(key, this::getBoolean, defaultVal);}

    public Int2D getInt2D(String key) {return get_parseFirst(key, Int2D::parseInt2D);}
    public Int2D getInt2D(String key, Int2D defaultVal) {return get_tryOrDefault(key, this::getInt2D, defaultVal);}

    public JSONObject getJSONObject(String key) {return get_JSONObject(key);}
    public JSONObject getJSONObject(String key, JSONObject defaultVal) {return get_tryOrDefault(key, this::getJSONObject, defaultVal);}



    public String toXMLString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<kutil>\n");
        for (KObject o : getList("kutil")) {
            sb.append( o.toXml().toString() );
        }
        sb.append("</kutil>\n");
        return sb.toString();
    }

    public JSONObject toJson() {

        JSONArray kutilArr = new JSONArray();
        for (KObject o : getList("kutil")) {
            kutilArr.put(o.toJson());
        }

        JSONObject ret = new JSONObject();
        ret.put("kutil", kutilArr);
        return ret;
    }

    public String toPrettyJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"kutil\":\n");
        for (KObject o : getList("kutil")) {
            sb.append( o.toXml().toPrettyJson(1) );
        }
        sb.append("\n}\n");
        return sb.toString();
    }


    public String toMacroStr() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String,List<KAttVal>> e :atts.entrySet()) {
            sb.append( e.getKey() ).append(" -> ").append( e.getValue() ).append('\n');
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return toXMLString();
    }

}
