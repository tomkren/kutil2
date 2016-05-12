package cz.tomkren.kutil;

import cz.tomkren.helpers.ResourceLoader;
import cz.tomkren.kutil.core.Kutil;
import org.json.JSONObject;

/** Created by Tom on 12. 5. 2016. */

public class VizServer {

    public static void main(String[] args) {

        JSONObject config = new ResourceLoader().loadJSON("/cz/tomkren/kutil/viz-server-config.json");

        Kutil kutil = new Kutil(config);

        kutil.start(Kutil.LoadMethod.JSON_RESOURCE, "/cz/tomkren/kutil/viz-server-state.json");
        kutil.getServerMaster().stopServer();

    }

}
