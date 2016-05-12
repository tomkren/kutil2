package cz.tomkren.kutil;

import com.google.common.base.Joiner;
import cz.tomkren.kutil.core.Kutil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/** Created by tom on 23.7.2015. */

public class KUtils {


    public static void startLib(String[] lib, String goalType, Integer numTrees) {
        new Kutil().start(Kutil.LoadMethod.STRING, wrapInKutilShower(lib, goalType, numTrees.toString()));
    }

    /*
    public static void showDag(TypedDag dag) {
        new Kutil().start(Kutil.LoadMethod.STRING, wrapInKutilShower(mkFrameWith(dag.toKutilXML(new Int2D(64, 64)))));
    }
    */

    public static void showDag(String dagXml) {
        new Kutil().start(Kutil.LoadMethod.STRING, wrapInKutilShower(mkFrameWith(dagXml)));
    }

    /*
    public static void showDags(List<TypedDag> dags) {
        StringBuilder sb = new StringBuilder();

        int width = 16000;
        int okraj = 20;
        int init  = 3*okraj;

        int x = init;
        int y = init;

        int maxHeight = 0;

        for (TypedDag dag : dags) {
            sb.append( dag.toKutilXML(new Int2D(x,y)) ).append("\n");

            x += dag.getPxWidth() + okraj;

            if (dag.getPxHeight() > maxHeight) {
                maxHeight = dag.getPxHeight();
            }

            if (x > width) {
                x = init;
                y += maxHeight + okraj;

            }
        }

        String xml = wrapInKutilShower(mkFrameWith(sb.toString()));
        new Kutil().start(Kutil.LoadMethod.STRING, xml);
    }
    */

    private static String wrapInKutilShower(String[] lib, String goalType, String numTrees) {
        return wrapInKutilShower(mkLibMacro(lib, goalType, numTrees));
    }

    private static String wrapInKutilShower(String xml) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<kutil>\n" +
                "    <o type=\"time\" ups=\"80\">\n" +
                "\n" +
                "        <o type=\"frame\" title=\"Pokusy s DAGama\" id=\"$window\">\n" +
                "\n" +
                "            <o type=\"frame\" title=\"Pohled na frame okna\" showXML=\"true\" target=\"$window\" pos=\"221 -579\" size=\"640 480\" />\n" +
                "            <o type=\"frame\" title=\"Pohled na frame s resultem\" showXML=\"true\" target=\"$pokus3\" pos=\"-455 -580\" size=\"640 480\" />\n" +
                "\n" +
                "\n" + xml +
                "\n" +
                "        </o>\n" +
                "\n" +
                "\n" +
                "    </o>\n" +
                "</kutil>";
    }

    public static String wrapInMinimal(String title, String xml) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<kutil>\n" +
                "  <o type=\"time\" ups=\"80\">\n"+
                "    <o type=\"frame\" title=\""+title+"\" id=\"$mainFrame\">\n"+
                "      "+xml+"\n"+
                "    </o>\n" +
                "  </o>\n" +
                "</kutil>";
    }

    public static JSONObject mkFunBox(String id, int x, int y, int numIns, int numOuts, String name) {

        String shapeStr = "f "+numIns+" "+numOuts+" "+name;

        JSONObject funBox = new JSONObject();
        funBox.put("id",id);
        funBox.put("pos",x+" "+y);
        funBox.put("shape", shapeStr);

        return funBox;

        /*<o id="$v_41" pos="386 92" shape="f 1 6 copy $v_44:0 $v_45:0 $v_46:0 $v_47:0 $v_42:0 $v_43:0"/>
        <o id="$v_42" pos="482 156" shape="f 1 5 copy $v_50:0 $v_51:0 $v_52:0 $v_48:0 $v_49:0"/>
        <o id="$v_43" pos="674 220" shape="f 1 1 gaussianNB $v_54:5"/>
        <o id="$v_44" pos="98 220" shape="f 1 1 logR $v_54:0"/>
        <o id="$v_45" pos="162 220" shape="f 1 1 DT $v_54:1"/>
        <o id="$v_46" pos="226 220" shape="f 1 1 logR $v_54:2"/>
        <o id="$v_47" pos="290 220" shape="f 1 1 DT $v_54:3"/>
        <o id="$v_48" pos="546 220" shape="f 1 1 DT $v_53:3"/>
        <o id="$v_49" pos="610 220" shape="f 1 1 SVC $v_53:4"/>
        <o id="$v_50" pos="354 220" shape="f 1 1 gaussianNB $v_53:0"/>
        <o id="$v_51" pos="418 220" shape="f 1 1 gaussianNB $v_53:1"/>
        <o id="$v_52" pos="482 220" shape="f 1 1 DT $v_53:2"/>
        <o id="$v_54" pos="386 348" shape="f 6 1 vote"/>
        <o id="$v_53" pos="482 284" shape="f 5 1 vote $v_54:4"/>*/
    }

    public static JSONObject wrapInMinJson(String title, JSONArray frameInsideArr) {
        JSONObject kutilObj = new JSONObject();
        JSONObject timeObj  = new JSONObject();
        JSONObject frameObj = new JSONObject();

        frameObj.put("type","frame");
        frameObj.put("id","$mainFrame");
        frameObj.put("main","true");
        frameObj.put("title", title);
        frameObj.put("inside",frameInsideArr);

        timeObj.put("type","time");
        timeObj.put("id","$mainTime");
        timeObj.put("ups", "80");
        timeObj.put("inside",new JSONArray().put(frameObj));

        kutilObj.put("kutil",timeObj);

        return kutilObj;
    }

    private static String mkLibMacro(String[] lib, String goalType, String numTrees) {
        return  "            <macro type=\"TypedDagGenerator\" id=\"$pokus3\" title=\"Title...\" size=\"2500 2000\" pos=\"4 4\">\n" +
                "                <n>"+numTrees+"</n>\n" +
                "                <goal>"+goalType+"</goal>\n" +
                "                <lib>"+ Joiner.on(";\n").join(lib) +"</lib>\n" +
                "            </macro>\n" ;
    }

    private static String mkFrameWith(String innerXML) {
        return  "<o type=\"frame\" id=\"$pokus3\" title=\"Title...\" size=\"2500 2000\" pos=\"4 4\">\n" +
                innerXML+
                "</o>\n" ;
    }
}
