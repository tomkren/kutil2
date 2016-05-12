package cz.tomkren.kutil.core.masters;

import cz.tomkren.helpers.Log;
import cz.tomkren.kutil.core.KObject;
import cz.tomkren.kutil.core.Kutil;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;

/** Created by tom on 24. 9. 2015. */

public class ServerMaster extends AbstractHandler {

    private Kutil kutil;

    private Server server;
    private boolean isServerRunning = false;
    private JSONObject serverGameState = new JSONObject();

    public ServerMaster(Kutil kutil) {
       this.kutil = kutil;
    }

    private synchronized JSONObject getServerGameState() {
        return serverGameState;
    }

    private synchronized void setServerGameState(JSONObject serverGameState) {
        this.serverGameState = serverGameState;
    }


    public void serverActionOnSchedulerStep() {

        // TODO neukládat asi každej krok, ale jen občas

        if (isServerRunning) {
            saveServerGameState();
        }

    }

    private void saveServerGameState() {
        KObject main = kutil.getSimulationMaster().getMain();
        setServerGameState(main.toJson());
    }

    @Override
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {

        //Log.it("request: "+request.getQueryString());

        String queryString = request.getQueryString();

        String resultFormat = "jsonp"; // TODO udělat pak pořádně

        if (queryString != null) {
            String[] queryParts = queryString.split("&");
            for (String queryPart : queryParts) {
                String[] eqParts = queryPart.split("=");
                String key = eqParts[0];
                String val = URLDecoder.decode(eqParts[1], "UTF-8");

                if (key.equals("cmd")) {
                    Log.it("cmd: " + val);
                    kutil.getCmdMaster().cmd(val);
                } else if (key.equals("resultFormat")) {
                    resultFormat = val;
                }
            }
        }

        response.setContentType("application/javascript;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        JSONObject currentServerGameState = getServerGameState();

        String serverStateStr = currentServerGameState.toString(2);
        String responseStr;

        switch (resultFormat) {
            case "jsonp" : responseStr = "serverUpdate("+serverStateStr+");"; break;
            case "json"  : responseStr = serverStateStr; break;
            default      : responseStr = serverStateStr; break;
        }


        response.getWriter().println(responseStr);
    }


    public void stopServer() {
        if (isServerRunning) {
            try {
                server.stop();
            } catch (Exception e) {
                throw new Error(e);
            }
        }
    }

    public void startServer(int port) {
        isServerRunning = true;

        server = new Server(port);
        server.setHandler(this);

        try {
            server.start();
            server.join();

        } catch (Exception e) {
            throw new Error(e);
        }
    }

}
