package cz.tomkren.kutil.items;

import cz.tomkren.kutil.core.KObject;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class KItem<T> {

    private String  key;
    private T       val;

    private T defaultVal;

    public KItem(String key, T val, T defaultVal) {
        this.key = key;
        this.val = val;
        this.defaultVal = defaultVal;
    }

    public KItem(String key, T val) {
        this(key, val, null);
    }


    public void set(T newVal) {val = newVal;}
    public T get() {return val;}

    public void addToXmlElement(XmlElement xmlElement) {
        if (val == null) {return;}
        if (!val.equals(defaultVal)) {
            xmlElement.add(key, val);
        }
    }

    public void addToJson(JSONObject json) {
        if (val != null && !val.equals(defaultVal)) {
            json.put(key, valToJson(val));
        }
    }

    public static Object valToJson(Object val) {

        if (val instanceof KObject) {

            return ((KObject)val).toJson();

        } else if (val instanceof List) {

            JSONArray retArr = new JSONArray();
            List xs = (List) val;
            for (Object x : xs) {
                retArr.put(valToJson(x));
            }
            return retArr;

        } else {

            return val.toString();

        }
    }


}
