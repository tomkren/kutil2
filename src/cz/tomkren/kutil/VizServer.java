package cz.tomkren.kutil;

import cz.tomkren.helpers.Log;
import cz.tomkren.helpers.ResourceLoader;
import cz.tomkren.kutil.core.Kutil;
import org.json.JSONObject;

/** Created by Tom on 12. 5. 2016. */

public class VizServer {


    public static void main(String[] args) {

        JSONObject config = new ResourceLoader().loadJSON(Kutil.DEFAULT_CONFIG);

        Kutil.LoadMethod loadMethod = Kutil.LoadMethod.JSON_RESOURCE;
        String loadInput = "/cz/tomkren/kutil/viz-server-state.json";

        Kutil kutil = new Kutil(config);

        if (config.has("startServer") && config.getBoolean("startServer")) {
            Log.it("Server started!\n");

            Thread serverThread = new Thread() {
                public void run() {

                    int port = config.has("port") ? config.getInt("port") : 8080;

                    kutil.getServerMaster().startServer(port);

                }
            };

            serverThread.start();
        }

        kutil.start(loadMethod, loadInput);

        kutil.getServerMaster().stopServer();

        Log.it("Good bye!");




    }

}
