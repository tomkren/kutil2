package cz.tomkren.kutil.core;


import cz.tomkren.helpers.TODO;
import cz.tomkren.kutil.kobjects.*;
import cz.tomkren.kutil.kobjects.character.GameCharacter;
import cz.tomkren.kutil.kobjects.coin.Coin;
import cz.tomkren.kutil.kobjects.frame.Frame;
import cz.tomkren.kutil.kobjects.vincent.Vincent;
import org.json.JSONArray;
import org.json.JSONObject;

public class KObjectFactory {

    public static KObject newKObject(KAtts kAtts, Kutil kutil) {

        String typeName = kAtts.getString("type");

        // registrace typů KObjectů do programu:
        if ("time"     .equals(typeName)) return new Time     (kAtts, kutil);
        if ("coin"     .equals(typeName)) return new Coin     (kAtts, kutil);
        if ("frame"    .equals(typeName)) return new Frame    (kAtts, kutil);
        if ("label"    .equals(typeName)) return new Label    (kAtts, kutil);
        if ("button"   .equals(typeName)) return new Button   (kAtts, kutil);
        if ("character".equals(typeName)) return new GameCharacter(kAtts, kutil);
        if ("vincent"  .equals(typeName)) return new Vincent  (kAtts, kutil);

        return new KObject(kAtts, kutil);
    }



    // Novej přírůstek (#newFish) pro vkládání objektů se znamym targetem z json specifikace
    public static String addNewKObject(String parentId, JSONObject json, Kutil kutil) {

        KObject parent = kutil.getIdDB().get(parentId);
        if (parent == null) {return "ERROR in addNewKObject: Unknown parentId '"+parentId+"'.";}

        JSONObject wrappedJson = new JSONObject().put("kutil",json);
        KAtts kAtts = KAtts.fromJson(wrappedJson, kutil);
        KObject newKObject = kAtts.get("kutil");

        newKObject.setParent(parent);
        parent.add(newKObject);

        return "add ok";
        //throw new TODO();
    }

    public static String addNewKObjects(String parentId, JSONArray jsonArr, Kutil kutil) {
        String msg = "multi-add: ";
        for (int i = 0; i < jsonArr.length(); i++) {
            msg += addNewKObject(parentId, jsonArr.getJSONObject(i), kutil) + "; ";
        }
        return msg;
    }


    public static KObject newKObject(String xmlString, Kutil kutil){
        XmlLoader loader = new XmlLoader();
        KAtts kAtts = loader.load(Kutil.LoadMethod.STRING, "<kutil>"+ xmlString +"</kutil>", kutil);
        return kAtts.get("kutil");
    }

    /**
     * Hojně používaná metoda, která zajišťuje správné vložení nového
     * objektu do systému. TODO (POZN toto je todo ještě ze staré verze !!!): odstranit tuto metodu tak aby vytvoření nového objektu automaticky implikovalo tyto kroky.
     * @param newKObject
     * @param parent
     * @return
     */
    public static KObject insertKObjectToSystem(KObject newKObject, KObject parent, Kutil kutil) {
        newKObject.parentInfo(parent);
        newKObject.init();

        newKObject.resolveCopying();
        kutil.getIdChangeDB().clear();

        return newKObject;
    }
}
