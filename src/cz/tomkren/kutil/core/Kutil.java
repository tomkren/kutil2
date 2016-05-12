package cz.tomkren.kutil.core;


import cz.tomkren.helpers.*;
import cz.tomkren.kutil.core.masters.*;
import org.json.JSONObject;

import java.util.function.BiFunction;

public class Kutil {

    public static final String DEFAULT_CONFIG = "/cz/tomkren/kutil/default-config.json";

    private JSONObject config;

    private KeyboardMaster keyboardMaster;
    private MouseMaster mouseMaster;
    private PopupMaster popupMaster;
    private CmdMaster cmdMaster;
    private SelectionMaster selectionMaster;
    private ClipboardMaster clipboardMaster;
    private SimulationMaster simulationMaster;
    private PlayerMaster playerMaster;
    private GuiMaster guiMaster;
    private ServerMaster serverMaster;

    private IdDB         idDB;
    private IdChangeDB   idChangeDB;
    private Scheduler    scheduler;
    private ShapeFactory shapeFactory;
    private XmlMacroFactory xmlMacroFactory;

    private Checker checker;
    private boolean isStarted;

    public Kutil() {
        this(new ResourceLoader().loadJSON(DEFAULT_CONFIG), null);
    }

    public Kutil(JSONObject config) {
        this(config, null);
    }

    public Kutil(JSONObject config, Long seed) {
        this.config = config;
        isStarted = false;
        checker = new Checker(seed);
        cmdMaster = new CmdMaster(this);
        serverMaster = new ServerMaster(this);
    }

    private void init() {

        boolean showGUI = !config.has("showGUI") || config.getBoolean("showGUI");

        mouseMaster      = new MouseMaster(this);
        selectionMaster  = new SelectionMaster(this);
        clipboardMaster  = new ClipboardMaster();
        simulationMaster = new SimulationMaster(this);
        playerMaster     = new PlayerMaster(this);
        guiMaster        = new GuiMaster(this, showGUI);

        // poslední, aby už všechny commandy byly registrovaný
        popupMaster    = new PopupMaster(this);
        keyboardMaster = new KeyboardMaster(this);


        idDB            = new IdDB();
        idChangeDB      = new IdChangeDB();
        shapeFactory    = new ShapeFactory();
        xmlMacroFactory = new XmlMacroFactory(this);

        /*  TODO - console, figurky, input-output-čáry handling, in-game file loading ...
        console = null;
        resetFrom();
        fileChooser = new JFileChooser(  );
        try{
            File f = new File(new File(".").getCanonicalPath());
            fileChooser.setCurrentDirectory(f);
        } catch(Exception e){}
        */
    }


    public enum LoadMethod {RESOURCE, FILE, STRING, JSON_STRING, JSON_RESOURCE, JSON_FILE}

    private void start(JSONObject stateJson) {
        init();
        KAtts loaded = KAtts.fromJson(stateJson, this);
        start(loaded);
    }

    public void start(LoadMethod loadMethod, String loadInput) {

        if (config.has("startServer") && config.getBoolean("startServer")) {
            Log.it("Server started!\n");

            Thread serverThread = new Thread() {
                public void run() {
                    int port = config.has("port") ? config.getInt("port") : 8080;
                    getServerMaster().startServer(port);
                }
            };

            serverThread.start();
        }

        if (loadMethod == LoadMethod.JSON_STRING) {
            start(new JSONObject(loadInput));
        } else if (loadMethod == LoadMethod.JSON_RESOURCE) {
            start(new ResourceLoader().loadJSON(loadInput));
        } else if (loadMethod == LoadMethod.JSON_FILE) {
            start(new JSONObject(new ResourceLoader().loadFile(loadInput)));
        } else {

            init();

            XmlLoader loader = new XmlLoader();
            KAtts loaded = loader.load(loadMethod, loadInput, this);

            start(loaded);
        }
    }

    private void start(KAtts loaded) {

        if (loaded == null) {

            Log.it("[XML-ERROR] Chyba při nahrávání, nic se nenahrálo.");

        } else {

            if (config.getBoolean("performXmlLoadTest")) {
                String xmlString1 = loaded.toXMLString();

                Log.it("LOL : \n"+xmlString1);

                init();

                XmlLoader loader = new XmlLoader();
                KAtts loaded2 = loader.load(LoadMethod.STRING, xmlString1, this);
                String xmlString2 = loaded2.toXMLString();
                checker.eqStrSilent(xmlString1,xmlString2);

                Log.it(xmlString2);
                Log.it( loaded2.toJson().toString(2) );
                Log.it( loaded2.toPrettyJson() );


                go(loaded2);

            } else {

                //Log.it(loaded);
                go(loaded);
            }
        }

        checker.results();
    }



    private void go(KAtts kAtts) {
        isStarted = true;
        simulationMaster.onGo();
        new Scheduler(kAtts, this);
    }

    public void offerCmd(String cmd) {cmdMaster.offerCmd(cmd);}
    public void registerCmd(String cmdName, Procedure cmdCode) {cmdMaster.registerCmd(cmdName, cmdCode);}
    public void registerFullCmd(String cmdName, Cmd cmdCode) {cmdMaster.registerFullCmd(cmdName, cmdCode);}
    public void registerFullCmd(String cmdName, BiFunction<String,String,String> cmdCode) {
        cmdMaster.registerFullCmd(cmdName, cmdCode);
    }

    public JSONObject getConfig() {return config;}
    public KeyboardMaster getKeyboardMaster() {return keyboardMaster;}
    public MouseMaster getMouseMaster() {return mouseMaster;}
    public PopupMaster getPopupMaster() {return popupMaster;}
    public CmdMaster getCmdMaster() {return cmdMaster;}
    public SelectionMaster getSelectionMaster() {return selectionMaster;}
    public ClipboardMaster getClipboardMaster() {return clipboardMaster;}
    public GuiMaster getGuiMaster() {return guiMaster;}
    public SimulationMaster getSimulationMaster() {return simulationMaster;}
    public ServerMaster getServerMaster() {return serverMaster;}

    public PlayerMaster getPlayerMaster() {return playerMaster;}
    public IdDB getIdDB() {return idDB;}
    public IdChangeDB getIdChangeDB() {return idChangeDB;}
    public Scheduler getScheduler() {return scheduler;}
    public Checker getChecker() {return checker;}
    public ShapeFactory shapeFactory() {return shapeFactory;}

    public XmlMacroFactory getXmlMacroFactory() {return xmlMacroFactory;}

    public void setScheduler(Scheduler s) {scheduler = s;}

    public boolean isStarted() {return isStarted;}

}
