package cz.tomkren.kutil;

import cz.tomkren.helpers.Log;
import cz.tomkren.helpers.ResourceLoader;
import cz.tomkren.kutil.core.Kutil;
import cz.tomkren.kutil.x.XCompiler;
import org.json.JSONObject;

import java.io.File;

/**
 * KUTIL V2
 * V2 started by Tomáš Křen on 31. 10. 2014.
 */

public class KutilMain {

    public static final String VERSION = "alpha 2.1.0";

    public static void main(String[] args) {
        Log.it("KUTIL (v "+VERSION+"), hello!\n");

        String xDirPath = "x";

        if (!(new File(xDirPath).exists())) {
            boolean success = new File(xDirPath).mkdirs();
            if (!success) {
                throw new Error("Unable to create '"+xDirPath+"' directory!");
            }
        }

        new XCompiler().compileToFiles(new String[]{"game.json"});

        boolean isArgsInput = args.length > 0;

        ResourceLoader rl = new ResourceLoader();

        JSONObject config = isArgsInput ? new JSONObject(rl.loadFile(args[0])) : rl.loadJSON(Kutil.DEFAULT_CONFIG);
        Log.it("config = " + config);

        /*
        Kutil.LoadMethod loadMethod = isArgsInput ? Kutil.LoadMethod.FILE : Kutil.LoadMethod.JSON_FILE ;
        String loadInput = isArgsInput ? args[0] : config.getString("defaultStateFile");
        */


        Kutil.LoadMethod loadMethod = Kutil.LoadMethod.JSON_FILE;
        String loadInput = config.getString("defaultStateFile");


        Kutil kutil = new Kutil(config);



        kutil.start(loadMethod, loadInput);

        kutil.getServerMaster().stopServer();

        Log.it("Good bye!");
    }


}
