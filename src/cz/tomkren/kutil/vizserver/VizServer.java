package cz.tomkren.kutil.vizserver;

import cz.tomkren.utils.ResourceLoader;
import cz.tomkren.kutil.core.Kutil;
import org.json.JSONObject;

/** Created by Tom on 12. 5. 2016. */

public class VizServer {

    public static void main(String[] args) {

        JSONObject config = new ResourceLoader().loadJSON("/cz/tomkren/kutil/vizserver/viz-server-config.json");

        Kutil kutil = new Kutil(config);

        kutil.start(Kutil.LoadMethod.JSON_RESOURCE, "/cz/tomkren/kutil/vizserver/viz-server-state.json");
        kutil.stop();

    }

}
