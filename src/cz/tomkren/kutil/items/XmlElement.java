package cz.tomkren.kutil.items;


import com.google.common.base.Joiner;
import cz.tomkren.helpers.F;
import cz.tomkren.helpers.TODO;
import cz.tomkren.kutil.core.JSONObjectWrapper;
import cz.tomkren.kutil.core.KObject;
import cz.tomkren.kutil.core.XmlLoader;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class XmlElement implements Xml {

    private String tag;
    private List<Xml> inside;
    private List<StringString> atts;


    public XmlElement(String tag) {
        this.tag = tag;
        inside   = new ArrayList<>();
        atts     = new ArrayList<>();
    }

    public void addInside(Xml xml) {
        inside.add(xml);
    }

    public void add(String key, Object value) {

        if (value == null) return;

        if (value instanceof List) {

            List list = (List) value;

            if (list.isEmpty()) return;

            XmlElement el = new XmlElement(key);

            for (Object o : list) {

                if (o instanceof KObject) {
                    el.addInside(((KObject) o).toXml());
                } else {
                    throw new Error("List should be List<KObject>.");
                }

            }

            addInside(el);


        } else if(value instanceof KObject) {

            XmlElement el = new XmlElement(key);
            el.addInside(((KObject) value).toXml());
            addInside(el);

        } else if (value instanceof JSONObject) {

            atts.add(new StringString(key, JSONObjectWrapper.toString((JSONObject)value)));

        } else {
            atts.add(new StringString(key,value.toString()));
        }

    }

    private class StringString {
        public String s1;
        public String s2;

        public StringString( String att , String val ){
            s1 = att;
            s2 = val;
        }

        @Override
        public String toString() {
            return s1+"=\""+s2+"\"";
        }

        public String toJsonString() {
            return "\""+s1+"\":\""+s2+"\"";
        }
    }


    @Override
    public String toString() {
        return toStringOdsad(0,true);
    }

    private String toStringOdsad(int ods, boolean vypisTag) {

        StringBuilder sb = new StringBuilder();

        if (vypisTag) {

            sb.append(ods(ods)).append("<").append(tag);
            for (StringString ss : atts) {
                sb.append(" ").append(ss.toString());
            }
        }

        if (!inside.isEmpty()) {

            if (vypisTag) {sb.append( ">\n" );}

            for (Xml xml : inside) {
                if (xml instanceof XmlText) {
                    sb.append(ods(ods + 1)).append( xml.toString());
                } else {

                    XmlElement elem = (XmlElement) xml;

                    if(elem.tag.equals("inside")) {
                        sb.append( elem.toStringOdsad(ods, false) );
                    } else {
                        sb.append( elem.toStringOdsad(ods+1, true) );
                    }

                }
            }

            if (vypisTag) {
                sb.append(ods(ods)).append("</").append(tag).append( ">");
            }
        } else if(vypisTag) {
            sb.append("/>");
        }

        if (vypisTag) {sb.append("\n");}

        return sb.toString();
    }

    public String toPrettyJson(int ods) {

        StringBuilder sb = new StringBuilder();

        sb.append(ods(ods)).append("{");

        List<StringString> atts2 = new ArrayList<>();
        if (!tag.equals(XmlLoader.OBJECT_TAG)) {
            atts2.add(new StringString("tag", tag));
        }
        atts2.addAll(atts);

        sb.append(Joiner.on(", ").join(F.map(atts2, StringString::toJsonString)));


        if (!inside.isEmpty()) {

            sb.append(", \"inside\": [\n");

            List<Xml> inside_hax = ((XmlElement) inside.get(0)).inside;

            /*for (Xml xml : inside_hax) {
                sb.append(xml.toPrettyJson(ods + 1));
            }*/

            sb.append( Joiner.on(",\n").join( F.map(inside_hax, xml -> xml.toPrettyJson(ods + 1) ) ) );

            sb.append("\n").append(ods(ods)).append("]");
        }

        sb.append("}");

        return sb.toString();
    }



    public static String ods( int ods ){
        StringBuilder ret = new StringBuilder();
        for( int i=0 ; i<ods; i++ ){
            ret.append("  ");
        }
        return ret.toString();
    }

}
