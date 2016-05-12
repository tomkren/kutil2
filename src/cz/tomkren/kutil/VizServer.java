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

        kutil.start(loadMethod, loadInput);

        kutil.getServerMaster().stopServer();

        Log.it("Good bye!");




    }

}
